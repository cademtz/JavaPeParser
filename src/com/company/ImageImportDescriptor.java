package com.company;

import java.io.IOException;

public class ImageImportDescriptor {
    private long chara_origFirstThunk;  // union
    public long timeDateStamp;          // 0 if not bound,
                                        // -1 if bound, and real date\time stamp
                                        //     in IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT (new BIND)
                                        // O.W. date/time stamp of DLL bound to (Old BIND)
    public long forwarderChain;         // -1 if no forwarders
    public long name;
    public long firstThunk;

    public static ImageImportDescriptor read(LittleEndianReader r)
    {
        ImageImportDescriptor desc = new ImageImportDescriptor();

        try {
            desc.chara_origFirstThunk = r.readDword();
            desc.timeDateStamp = r.readDword();
            desc.forwarderChain = r.readDword();
            desc.name = r.readDword();
            desc.firstThunk = r.readDword();
        }
        catch (IOException e) { return null; }

        return desc;
    }

    public long getCharacteristics() { return chara_origFirstThunk; }
    public void setCharacteristics(long Rva) { chara_origFirstThunk = Rva; }

    public long getOriginalFirstThunk() { return chara_origFirstThunk; }
    public void setOriginalFirstThunk(long Rva) { chara_origFirstThunk = Rva; }
}
