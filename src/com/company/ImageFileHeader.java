package com.company;

import java.io.IOException;

public class ImageFileHeader {
    public int machine;
    public int numberOfSections;
    public long timeDateStamp;
    public long pointerToSymbolTable;
    public long numberOfSymbols;
    public int sizeOfOptionalHeader;
    public int characteristics;

    static ImageFileHeader read(LittleEndianReader r)
    {
        ImageFileHeader hdr = new ImageFileHeader();

        try {
            hdr.machine = r.readWord();
            hdr.numberOfSections = r.readWord();
            hdr.timeDateStamp = r.readDword();
            hdr.pointerToSymbolTable = r.readDword();
            hdr.numberOfSymbols = r.readDword();
            hdr.sizeOfOptionalHeader = r.readWord();
            hdr.characteristics = r.readWord();
        }
        catch (IOException e) { return null; }

        return hdr;
    }
}
