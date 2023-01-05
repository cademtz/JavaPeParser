package me.martinez.pe;

/**
 * Contains the name of an import and either its ordinal, or its name, or both.
 * This object is not a file structure but contains the information of multiple linked file structures.
 */
public class ImportEntry {
    /**
     * Import by name. May be {@code null} if no name was used.
     */
    public final String name;
    /**
     * Import by ordinal <i>(16-bit integer (word))</i>.
     * May be {@code null} if no ordinal was used.
     */
    public final Integer ordinal;
    /**
     * Virtual address to a pointer, which will be pointed at the resolved import by the Windows loader.
     * Typically an address to un-initialized memory not physically located in the file.
     */
    public final long thunkAddress;

    public ImportEntry(String name, Integer ordinal, long thunkAddress) {
        this.name = name;
        this.ordinal = ordinal;
        this.thunkAddress = thunkAddress;
    }
}
