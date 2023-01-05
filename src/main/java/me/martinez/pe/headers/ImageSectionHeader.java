package me.martinez.pe.headers;

import me.martinez.pe.io.CadesVirtualMemStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageSectionHeader {
    public static final int IMAGE_SIZEOF_SHORT_NAME = 8;

    private String name;
    private long physicalAddr_vSize; // union
    public long virtualAddress; // Relative virtual address
    public long sizeOfRawData;
    public long pointerToRawData;
    public long pointerToRelocations;
    public long pointerToLinenumbers;
    public int numberOfRelocations;
    public int numberOfLinenumbers;
    public long characteristics;

    public static ParseResult<ImageSectionHeader> read(CadesVirtualMemStream vmem) {
        LittleEndianReader r = new LittleEndianReader(vmem);
        ImageSectionHeader hdr = new ImageSectionHeader();

        try {
            hdr.name = r.readString(IMAGE_SIZEOF_SHORT_NAME);
            hdr.physicalAddr_vSize = r.readDword();
            hdr.virtualAddress = r.readDword();
            hdr.sizeOfRawData = r.readDword();
            hdr.pointerToRawData = r.readDword();
            hdr.pointerToRelocations = r.readDword();
            hdr.pointerToLinenumbers = r.readDword();
            hdr.numberOfRelocations = r.readWord();
            hdr.numberOfLinenumbers = r.readWord();
            hdr.characteristics = r.readDword();
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageSectionHeader", e);
        }

        return ParseResult.ok(hdr);
    }

    public String getName() {
        return name;
    }

    public long getPhysicalAddress() {
        return physicalAddr_vSize;
    }

    public long getVirtualSize() {
        return physicalAddr_vSize;
    }
}
