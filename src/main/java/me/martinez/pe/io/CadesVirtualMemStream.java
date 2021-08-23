package me.martinez.pe.io;

import me.martinez.pe.ImagePeHeaders;
import me.martinez.pe.ImageSectionHeader;

import java.io.IOException;

public class CadesVirtualMemStream extends CadesStreamReader {
	private final ImagePeHeaders pe;
	private final CadesStreamReader rawFile;

	public CadesVirtualMemStream(ImagePeHeaders pe, CadesStreamReader rawFile) {
		this.pe = pe;
		this.rawFile = rawFile;
		setPos(pe.ntHeader.optionalHeader.imageBase);
	}

	@Override
	public int read() throws IOException {
		incrementPos(1);
		return rawFile.read() & 0xFF;
	}

	@Override
	public void seek(long pos) throws IOException {
		long real = virtualToReal(pos);
		if (real < 0)
			throw new IOException("Virtual address does not point to readable memory page");

		setPos(pos);
		rawFile.seek(real);
	}

	@Override
	public void seekEnd() throws IOException {
		for (ImageSectionHeader sec : pe.sectionHeaders) {
			long vaddr = pe.ntHeader.optionalHeader.imageBase + sec.virtualAddress + sec.sizeOfRawData;
			if (vaddr > getPos())
				setPos(vaddr);
		}

		seek(getPos()); // Use existing seek() to find real address and safety check
	}

	@Override
	public void seekStart() throws IOException {
		seek(pe.ntHeader.optionalHeader.imageBase);
	}

	/**
	 * Translates virtual address to file address
	 *
	 * @param pos
	 * 		Virtual address
	 *
	 * @return File address
	 */
	private long virtualToReal(long pos) throws IOException {
		if (pos < 0) // User-space memory on Windows does not exceed max signed 64-bit
			return -1;

		for (ImageSectionHeader sec : pe.sectionHeaders) {
			long vaddr = pe.ntHeader.optionalHeader.imageBase + sec.virtualAddress;
			if (pos >= vaddr && pos < vaddr + sec.sizeOfRawData)
				// Relative to start of section, then add section's file offset
				return sec.pointerToRawData + (pos - vaddr);
		}

		return -1;
	}
}
