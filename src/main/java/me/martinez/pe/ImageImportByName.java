package me.martinez.pe;

import me.martinez.pe.io.LittleEndianReader;

import java.io.IOException;

public class ImageImportByName {
	public static final long IMAGE_ORDINAL_FLAG64 = (long) 1 << 63; // 0x8000000000000000, but java dumb lol
	public static final int IMAGE_ORDINAL_FLAG32 = 0x80000000;

	/* union: */
	private Integer ordinal;
	/* struct: */
	private Integer hint;
	private String name;

	public ImageImportByName() {}

	public ImageImportByName(int ordinal) {
		this.ordinal = ordinal;
	}

	public static ImageImportByName read(long addrAsOrdinal, boolean is64bit) {
		// Check if addr is actually meant to be an ordinal
		if ((is64bit && ((addrAsOrdinal & IMAGE_ORDINAL_FLAG64) != 0)) ||
				(!is64bit && ((addrAsOrdinal & IMAGE_ORDINAL_FLAG32) != 0))) {
			return new ImageImportByName((int) (addrAsOrdinal & 0xFFFF));
		}
		return null;
	}

	public static ImageImportByName read(LittleEndianReader r, boolean is64bit) {
		long pos = r.getStream().getPos();
		ImageImportByName imp = new ImageImportByName();

		try {
			// Check if import is by ordinal
			if ((is64bit && ((pos & IMAGE_ORDINAL_FLAG64) != 0)) ||
					(!is64bit && ((pos & IMAGE_ORDINAL_FLAG32) != 0))) {
				imp.ordinal = (int) (pos & 0xFFFF);
			} else {
				r.getStream().seek(pos); // Set position back and re-read as name
				imp.hint = r.readWord();
				imp.name = r.readNullTerminatedString(-1);
			}
		} catch (IOException e) {
			// TODO: Error handling
			return null;
		}

		return imp;
	}

	public Integer getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ord) {
		ordinal = ord;
		hint = null;
		name = null;
	}

	public int getHint() {
		return hint;
	}

	public void setHint(int Hint) {
		hint = Hint;
		ordinal = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String Name) {
		name = Name;
		ordinal = null;
	}
}
