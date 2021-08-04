package com.company;

import java.io.IOException;

public class ImageOptionalHeader {
    public final long IMAGE_NT_OPTIONAL_HDR32_MAGIC = 0x10b;
    public final long IMAGE_NT_OPTIONAL_HDR64_MAGIC = 0x20b;
    public final int IMAGE_NUMBEROF_DIRECTORY_ENTRIES = 16;

    public int magic;
    public int majorLinkerVersion; // byte
    public int minorLinkerVersion; // byte
    public long sizeOfCode;
    public long sizeOfInitializedData;
    public long sizeOfUninitializedData;
    public long addressOfEntryPoint;
    public long baseOfCode;
    public long baseOfData; // Exists only on 32-bit
    public long imageBase; // long on x64
    public long sectionAlignment;
    public long fileAlignment;
    public int majorOperatingSystemVersion;
    public int minorOperatingSystemVersion;
    public int majorImageVersion;
    public int minorImageVersion;
    public int majorSubsystemVersion;
    public int minorSubsystemVersion;
    public long win32VersionValue;
    public long sizeOfImage;
    public long sizeOfHeaders;
    public long checkSum;
    public int subsystem;
    public int dllCharacteristics;
    public long sizeOfStackReserve; // long on x64
    public long sizeOfStackCommit; // long on x64
    public long sizeOfHeapReserve; // long on x64
    public long sizeOfHeapCommit; // long on x64
    public long loaderFlags;
    public long numberOfRvaAndSizes;

    ImageDataDirectory[] dataDirectory = new ImageDataDirectory[IMAGE_NUMBEROF_DIRECTORY_ENTRIES];

    static ImageOptionalHeader read(LittleEndianReader r)
    {
        boolean is64bit;
        ImageOptionalHeader hdr = new ImageOptionalHeader();

        try {
            System.out.println("Reading NtOptional magic at off " + r.getStream().getPos());
            hdr.magic = r.readWord();
            is64bit = hdr.is64bit();

            hdr.majorLinkerVersion = r.readByte();
            hdr.minorLinkerVersion = r.readByte();
            hdr.sizeOfCode = r.readDword();
            hdr.sizeOfInitializedData = r.readDword();
            hdr.sizeOfUninitializedData = r.readDword();
            hdr.addressOfEntryPoint = r.readDword();
            hdr.baseOfCode = r.readDword();
            if (!is64bit)
                hdr.baseOfData = r.readWord();
            hdr.imageBase = r.readNative(is64bit);
            hdr.sectionAlignment = r.readDword();
            hdr.fileAlignment = r.readDword();
            hdr.majorOperatingSystemVersion = r.readWord();
            hdr.minorOperatingSystemVersion = r.readWord();
            hdr.majorImageVersion = r.readWord();
            hdr.minorImageVersion = r.readWord();
            hdr.majorSubsystemVersion = r.readWord();
            hdr.minorSubsystemVersion = r.readWord();
            hdr.win32VersionValue = r.readDword();
            hdr.sizeOfImage = r.readDword();
            hdr.sizeOfHeaders = r.readDword();
            hdr.checkSum = r.readDword();
            hdr.subsystem = r.readWord();
            hdr.dllCharacteristics = r.readWord();
            hdr.sizeOfStackReserve = r.readNative(is64bit);
            hdr.sizeOfStackCommit = r.readNative(is64bit);
            hdr.sizeOfHeapReserve = r.readNative(is64bit);
            hdr.sizeOfHeapCommit = r.readNative(is64bit);
            hdr.loaderFlags = r.readDword();
            hdr.numberOfRvaAndSizes = r.readDword();

            for (int i = 0; i < hdr.dataDirectory.length; ++i)
            {
                ImageDataDirectory dir = ImageDataDirectory.read(r);
                if (dir == null)
                    return null;

                hdr.dataDirectory[i] = dir;
            }
        }
        catch (IOException e) { return null; }

        return hdr;
    }

    boolean is64bit() { return magic == IMAGE_NT_OPTIONAL_HDR64_MAGIC; }
}
