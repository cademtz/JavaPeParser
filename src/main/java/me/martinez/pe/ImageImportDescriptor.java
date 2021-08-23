package me.martinez.pe;

import me.martinez.pe.io.LittleEndianReader;

import java.io.IOException;

public class ImageImportDescriptor {
	private long chara_origFirstThunk; // union
	/**
	 * <ul>
	 *     <li>0 if not bound</li>
	 *     <li>-1 if bound, and real date\time stamp   in IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT (new BIND)</li>
	 *     <li>O.W. date/time stamp of DLL bound to (Old BIND)</li>
	 * </ul>
	 */
	public long timeDateStamp;
	public long forwarderChain; // -1 if no forwarders
	public long name;
	public long firstThunk;

	public static ImageImportDescriptor read(LittleEndianReader r) {
		ImageImportDescriptor desc = new ImageImportDescriptor();

		try {
			desc.chara_origFirstThunk = r.readDword();
			desc.timeDateStamp = r.readDword();
			desc.forwarderChain = r.readDword();
			desc.name = r.readDword();
			desc.firstThunk = r.readDword();
		} catch (IOException e) {
			// TODO: Error handling
			return null;
		}

		return desc;
	}

	public long getCharacteristics() {
		return chara_origFirstThunk;
	}

	public void setCharacteristics(long rva) {
		chara_origFirstThunk = rva;
	}

	public long getOriginalFirstThunk() {
		return chara_origFirstThunk;
	}

	public void setOriginalFirstThunk(long rva) {
		chara_origFirstThunk = rva;
	}
}
