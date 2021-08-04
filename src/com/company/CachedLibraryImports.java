package com.company;

import java.util.ArrayList;

/**
 * Contains a list of all imports from this library
 */
public class CachedLibraryImports {
    private String name; // Library name
    public long timeStamp;
    public long forwarderChain;
    private ArrayList<CachedImportEntry> entries;

    public CachedLibraryImports(String Name, long TimeStamp, long ForwarderChain, ArrayList<CachedImportEntry> Entries) {
        name = Name;
        timeStamp = TimeStamp;
        forwarderChain=  ForwarderChain;
        entries = Entries;
    }

    public String getName() { return name; }
    public long getTimeStamp() { return timeStamp; }
    public long getForwarderChain() { return forwarderChain; }
    public int getNumEntries() { return entries.size(); }
    public CachedImportEntry getEntry(int index) { return entries.get(index); }
}
