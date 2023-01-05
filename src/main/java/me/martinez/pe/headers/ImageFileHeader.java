package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageFileHeader {
    public static final int MAX_WIN_LOADER_SECTIONS = 96;

    public int machine;
    public int numberOfSections;
    public long timeDateStamp;
    public long pointerToSymbolTable;
    public long numberOfSymbols;
    public int sizeOfOptionalHeader;
    public int characteristics;

    public static ParseResult<ImageFileHeader> read(LittleEndianReader r) {
        ImageFileHeader hdr = new ImageFileHeader();

        try {
            hdr.machine = r.readWord();
            hdr.numberOfSections = r.readWord();
            hdr.timeDateStamp = r.readDword();
            hdr.pointerToSymbolTable = r.readDword();
            hdr.numberOfSymbols = r.readDword();
            hdr.sizeOfOptionalHeader = r.readWord();
            hdr.characteristics = r.readWord();
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageFileHeader", e);
        }

        return ParseResult.ok(hdr);
    }
}
