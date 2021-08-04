package com.company;

import java.io.IOException;

public class ImageNtHeaders {
    public final long IMAGE_NT_SIGNATURE = 0x00004550;

    long signature;
    ImageFileHeader fileHeader;
    ImageOptionalHeader optionalHeader;

    static ImageNtHeaders read(LittleEndianReader r)
    {
        ImageNtHeaders nt = new ImageNtHeaders();

        try { nt.signature = r.readDword(); }
        catch (IOException e) { return null; }

        nt.fileHeader = ImageFileHeader.read(r);
        if (nt.fileHeader == null)
            return null;

        nt.optionalHeader = ImageOptionalHeader.read(r);
        if (nt.optionalHeader == null)
            return null;

        return nt;
    }

    boolean is64bit() { return optionalHeader.is64bit(); }
}
