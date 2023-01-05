package me.martinez.pe;

import java.util.List;

/**
 * Contains the name of a library and the details of each import from that library.
 * This object is not a file structure but contains the information of multiple linked file structures.
 */
public class LibraryImports {
    public final String name; // Library name
    public final long timeStamp;
    public final long forwarderChain;
    public final List<ImportEntry> entries;

    public LibraryImports(String name, long timeStamp, long forwarderChain, List<ImportEntry> entries) {
        this.name = name;
        this.timeStamp = timeStamp;
        this.forwarderChain = forwarderChain;
        this.entries = entries;
    }
}
