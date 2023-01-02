package me.martinez.pe;

/**
 * Contains the name, the ordinal, and the address of an item being exported.
 * This object is not a file structure but contains the information of multiple linked file structures.
 */
public class CachedExportEntry {
    public final String name;
    public final int ordinal;
    /**
     * Address to be exported
     */
    public final long address;

    public CachedExportEntry(String name, int ordinal, long address) {
        this.name = name;
        this.ordinal = ordinal;
        this.address = address;
    }
}
