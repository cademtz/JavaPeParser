package com.company;

import java.util.List;

/**
 * Contains a list of all imports from this library
 */
public class CachedLibraryImports {
	private final String name; // Library name
	public final long timeStamp;
	public final long forwarderChain;
	private final List<CachedImportEntry> entries;

	public CachedLibraryImports(String name, long timeStamp, long forwarderChain, List<CachedImportEntry> entries) {
		this.name = name;
		this.timeStamp = timeStamp;
		this.forwarderChain = forwarderChain;
		this.entries = entries;
	}

	public String getName() {
		return name;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getForwarderChain() {
		return forwarderChain;
	}

	public int getNumEntries() {
		return entries.size();
	}

	public CachedImportEntry getEntry(int index) {
		return entries.get(index);
	}
}
