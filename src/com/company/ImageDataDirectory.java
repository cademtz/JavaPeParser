package com.company;

import java.io.IOException;

public class ImageDataDirectory {
    public long virtualAddress;
    public long size;

    public static ImageDataDirectory read(LittleEndianReader r)
    {
        ImageDataDirectory dir = new ImageDataDirectory();

        try {
            dir.virtualAddress = r.readDword();
            dir.size = r.readDword();
        }
        catch (IOException e) { return null; }

        return dir;
    }
}
