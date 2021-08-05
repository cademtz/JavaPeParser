package com.company;

public class CachedImageExports {
    private String name;
    private CachedExportEntry[] entries;

    public CachedImageExports(String Name, CachedExportEntry[] Entries) {
        name = Name;
        entries = Entries;
    }

    public String getName() { return name; }
    public int getNumEntries() { return entries.length; }
    public CachedExportEntry getEntry(int index) { return entries[index]; }
}
