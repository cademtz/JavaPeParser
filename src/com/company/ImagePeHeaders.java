package com.company;

import java.io.IOException;

public class ImagePeHeaders {
    public ImageDosHeader dosHeader;
    public ImageNtHeaders ntHeader;
    public ImageSectionHeader[] sectionHeaders;

    public static ImagePeHeaders read(LittleEndianReader r) {
        ImagePeHeaders pe = new ImagePeHeaders();

        try {
            pe.dosHeader = ImageDosHeader.read(r);
            if (pe.dosHeader == null)
                return null;

            r.getStream().seek(pe.dosHeader.lfanew);

            pe.ntHeader = ImageNtHeaders.read(r);
            if (pe.ntHeader == null)
                return null;

            pe.sectionHeaders = new ImageSectionHeader[pe.ntHeader.fileHeader.numberOfSections];
            for (int i = 0; i < pe.sectionHeaders.length; ++i)
                pe.sectionHeaders[i] = ImageSectionHeader.read(r);
        }
        catch (IOException e) { return null; }

        return pe;
    }

    public boolean is64bit() { return ntHeader.is64bit(); }
}
