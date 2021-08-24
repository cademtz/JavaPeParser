package me.martinez.pe;

import me.martinez.pe.io.LittleEndianReader;

import java.io.IOException;

public class ImageExportDirectory {
	public long characteristics;
	public long timeDateStamp;
	public int majorVersion;
	public int minorVersion;
	public long name;
	public long base;
	public long numberOfFunctions;
	public long numberOfNames;
	public long addressOfFunctions;
	public long addressOfNames;
	public long addressOfNameOrdinals;

	public static ImageExportDirectory read(LittleEndianReader r) {
		ImageExportDirectory exp = new ImageExportDirectory();
		try {
			exp.characteristics = r.readDword();
			exp.timeDateStamp = r.readDword();
			exp.majorVersion = r.readWord();
			exp.minorVersion = r.readWord();
			exp.name = r.readDword();
			exp.base = r.readDword();
			exp.numberOfFunctions = r.readDword();
			exp.numberOfNames = r.readDword();
			exp.addressOfFunctions = r.readDword();
			exp.addressOfNames = r.readDword();
			exp.addressOfNameOrdinals = r.readDword();
		} catch (IOException e) {
			// TODO: Error handling
			return null;
		}

		return exp;
	}
}
