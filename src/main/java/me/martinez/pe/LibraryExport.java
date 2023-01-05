package me.martinez.pe;

/**
 * Contains the name of a library and the details of each listed export.
 * This object is not a file structure but contains the information of multiple linked file structures.
 */
public class LibraryExport {
    public final String name;
    public final ExportEntry[] entries;

    public LibraryExport(String name, ExportEntry[] entries) {
        this.name = name;
        this.entries = entries;
    }
}
