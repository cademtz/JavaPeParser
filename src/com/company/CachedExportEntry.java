package com.company;

public class CachedExportEntry {
	private final String name;
	private final int ordinal;
	private final long address; // Pointer being exported

	public CachedExportEntry(String name, int ordinal, long address) {
		this.name = name;
		this.ordinal = ordinal;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public long getAddress() {
		return address;
	}
}
