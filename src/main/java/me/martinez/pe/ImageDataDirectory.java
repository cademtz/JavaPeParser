package me.martinez.pe;

import me.martinez.pe.io.LittleEndianReader;

import java.io.IOException;

public class ImageDataDirectory {
	public final long virtualAddress;
	public final long size;

	private ImageDataDirectory(long virtualAddress, long size) {
		this.virtualAddress = virtualAddress;
		this.size = size;
	}

	public static ImageDataDirectory read(LittleEndianReader r) {
		try {
			long virtualAddress = r.readDword();
			long size = r.readDword();
			return new ImageDataDirectory(virtualAddress, size);
		} catch (IOException e) {
		    // TODO: Error handling
			return null;
		}
	}
}
