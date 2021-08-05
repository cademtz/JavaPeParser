package com.company;

public class CachedExportEntry {
    private String name;
    private int ordinal;
    private long address; // Pointer being exported

    public CachedExportEntry(String Name, int Ordinal, long Address) {
        name = Name;
        ordinal = Ordinal;
        address = Address;
    }

    public String getName() { return name; }
    public int getOrdinal() { return ordinal; }
    public long getAddress() { return address; }
}
