package com.company;

public class CachedImportEntry {
    private String name;
    private Integer ordinal;
    private long address; // Virtual address where imported pointer will be saved

    public CachedImportEntry(String Name, Integer Ordinal, long Address) {
        name = Name;
        ordinal = Ordinal;
        address = Address;
    }

    /**
     * The import by ordinal. May be null if no ordinal was used.
     * @return Ordinal as a 16-bit integer (word), or null
     */
    public Integer getOrdinal() { return ordinal; }

    /**
     * The import by name. May be null if no name was used.
     * @return The name, or null
     */
    public String getName() { return name; }

    /**
     * Where the imported pointer will be stored.
     * Likely points at un-initialized memory which is not physically located on file.
     * @return A virtual address to a pointer
     */
    public long getAddress() { return address; }
}
