package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageImportDescriptor {
    /**
     * The RVA of the import lookup table. This table contains a name or ordinal for each import.
     * (The name "Characteristics" is used in Winnt.h, but no longer describes this field.)
     */
    public long chara_origFirstThunk;
    /**
     * <ul>
     *     <li>0 if not bound</li>
     *     <li>-1 if bound, and real date\time stamp   in IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT (new BIND)</li>
     *     <li>O.W. date/time stamp of DLL bound to (Old BIND)</li>
     * </ul>
     */
    public long timeDateStamp;
    public long forwarderChain; // -1 if no forwarders
    public long name;
    public long firstThunk;

    public static ParseResult<ImageImportDescriptor> read(LittleEndianReader r) {
        ImageImportDescriptor desc = new ImageImportDescriptor();

        try {
            desc.chara_origFirstThunk = r.readDword();
            desc.timeDateStamp = r.readDword();
            desc.forwarderChain = r.readDword();
            desc.name = r.readDword();
            desc.firstThunk = r.readDword();
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageImportDescriptor", e);
        }

        return ParseResult.ok(desc);
    }

    public long getCharacteristics() {
        return chara_origFirstThunk;
    }

    public long getOriginalFirstThunk() {
        return chara_origFirstThunk;
    }
}
