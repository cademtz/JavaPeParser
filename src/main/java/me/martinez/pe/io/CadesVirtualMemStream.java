package me.martinez.pe.io;

import me.martinez.pe.PeImage;
import me.martinez.pe.headers.ImageSectionHeader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

/**
 * Read and navigate the data in a PE file by using virtual addresses
 */
public class CadesVirtualMemStream extends CadesStreamReader {
    private final PeImage pe;
    private final CadesStreamReader fdata;

    /**
     * @param fdata Input stream where {@link #pe} file data starts at position 0
     */
    public CadesVirtualMemStream(PeImage pe, CadesStreamReader fdata) {
        this.pe = pe;
        this.fdata = fdata;
        setPos(pe.ntHeaders.optionalHeader.imageBase);
    }

    @Override
    public int read() throws IOException {
        incrementPos(1);
        return fdata.read() & 0xFF;
    }

    @Override
    public void seek(long pos) throws IOException {
        long real = virtualToReal(pos);
        if (real < 0)
            throw new IOException("Virtual address does not point to readable memory page");

        setPos(pos);
        fdata.seek(real);
    }

    @Override
    public void seekEnd() throws IOException {
        for (ParseResult<ImageSectionHeader> parsedSec : pe.sectionHeaders) {
            if (parsedSec.isErr())
                continue;

            ImageSectionHeader sec = parsedSec.getOk();
            long vaddr = pe.ntHeaders.optionalHeader.imageBase + sec.virtualAddress + sec.sizeOfRawData;
            if (vaddr > getPos())
                setPos(vaddr);
        }

        seek(getPos()); // Use existing seek() to find real address and safety check
    }

    @Override
    public void seekStart() throws IOException {
        seek(pe.ntHeaders.optionalHeader.imageBase);
    }

    /**
     * Translates virtual address to file address
     *
     * @param pos Virtual address
     * @return File address
     */
    private long virtualToReal(long pos) throws IOException {
        if (pos < 0) // User-space memory on Windows does not exceed max signed 64-bit
            return -1;

        for (ParseResult<ImageSectionHeader> parsedSec : pe.sectionHeaders) {
            if (parsedSec.isErr())
                continue;

            ImageSectionHeader sec = parsedSec.getOk();
            long vaddr = pe.ntHeaders.optionalHeader.imageBase + sec.virtualAddress;
            if (pos >= vaddr && pos < vaddr + sec.sizeOfRawData)
                // Relative to start of section, then add section's file offset
                return sec.pointerToRawData + (pos - vaddr);
        }

        return -1;
    }
}
