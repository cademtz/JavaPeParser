package com.company;

public class CachedImageExports {
	private final String name;
	private final CachedExportEntry[] entries;

	public CachedImageExports(String name, CachedExportEntry[] entries) {
		this.name = name;
		this.entries = entries;
	}

	public String getName() {
		return name;
	}

	public int getNumEntries() {
		return entries.length;
	}

	public CachedExportEntry getEntry(int index) {
		return entries[index];
	}
}
