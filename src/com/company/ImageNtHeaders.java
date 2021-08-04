package com.company;

import java.io.IOException;

public class ImageNtHeaders {
    public static final long IMAGE_NT_SIGNATURE = 0x00004550;

    public long signature;
    public ImageFileHeader fileHeader;
    public ImageOptionalHeader optionalHeader;

    public static ImageNtHeaders read(LittleEndianReader r)
    {
        ImageNtHeaders nt = new ImageNtHeaders();

        try { nt.signature = r.readDword(); }
        catch (IOException e) { return null; }

        if (nt.signature != IMAGE_NT_SIGNATURE)
            return null; // Invalid header

        nt.fileHeader = ImageFileHeader.read(r);
        if (nt.fileHeader == null)
            return null;

        nt.optionalHeader = ImageOptionalHeader.read(r);
        if (nt.optionalHeader == null)
            return null;

        return nt;
    }

    public boolean is64bit() { return optionalHeader.is64bit(); }
    public ImageDataDirectory getDataDirectory(int index) { return optionalHeader.dataDirectory[index]; }
}
