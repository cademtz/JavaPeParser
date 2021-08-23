package me.martinez.pe;

public class CachedImportEntry {
	private final String name;
	private final Integer ordinal;
	private final long address; // Virtual address where imported pointer will be saved

	public CachedImportEntry(String name, Integer ordinal, long address) {
		this.name = name;
		this.ordinal = ordinal;
		this.address = address;
	}

	/**
	 * @return Import by ordinal <i>(16-bit integer (word))</i>.
	 * May be {@code null} if no ordinal was used.
	 */
	public Integer getOrdinal() {
		return ordinal;
	}

	/**
	 * @return Import by name. May be {@code null} if no name was used.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Where the imported pointer will be stored.
	 * Likely points at un-initialized memory which is not physically located on file.
	 *
	 * @return Virtual address to a pointer.
	 */
	public long getAddress() {
		return address;
	}
}
