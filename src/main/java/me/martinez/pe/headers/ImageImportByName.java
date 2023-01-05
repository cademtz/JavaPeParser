package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageImportByName {
    public static final long IMAGE_ORDINAL_FLAG64 = (long) 1 << 63; // 0x8000000000000000, but java dumb lol
    public static final int IMAGE_ORDINAL_FLAG32 = 0x80000000;

    /*
    // Either ordinal or name and hint can be set
    union {
        Integer ordinal;
        struct {
            Integer hint;
            String name;
        }
    }
     */
    public final Integer ordinal;
    public final Integer hint;
    public final String name;

    public ImageImportByName(int hint, String name) {
        this.ordinal = null;
        this.hint = hint;
        this.name = name;
    }

    public ImageImportByName(int ordinal) {
        this.ordinal = ordinal;
        this.hint = null;
        this.name = null;
    }

    public static ParseResult<ImageImportByName> read(long addrAsOrdinal, boolean is64bit) {
        // Check if addr is actually meant to be an ordinal
        if ((addrAsOrdinal & getOrdinalFlag(is64bit)) != 0)
            return ParseResult.ok(new ImageImportByName((int) (addrAsOrdinal & 0xFFFF)));
        else
            return ParseResult.err("Invalid import ordinal (does not have ordinal flag)");
    }

    public static ParseResult<ImageImportByName> read(LittleEndianReader r, boolean is64bit) {
        try {
            int hint = r.readWord();
            String name = r.readNullTerminatedString(-1);
            return ParseResult.ok(new ImageImportByName(hint, name));
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageImportByName", e);
        }
    }

    public static long getOrdinalFlag(boolean is64bit) {
        return is64bit ? IMAGE_ORDINAL_FLAG64 : IMAGE_ORDINAL_FLAG32;
    }
}
