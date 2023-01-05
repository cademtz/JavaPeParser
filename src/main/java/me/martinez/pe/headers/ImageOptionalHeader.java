package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageOptionalHeader {
    public static final long IMAGE_NT_OPTIONAL_HDR32_MAGIC = 0x10b;
    public static final long IMAGE_NT_OPTIONAL_HDR64_MAGIC = 0x20b;
    public static final int IMAGE_NUMBEROF_DIRECTORY_ENTRIES = 16;
    // Directory Entries
    public static final int IMAGE_DIRECTORY_ENTRY_EXPORT = 0; // Export Directory
    public static final int IMAGE_DIRECTORY_ENTRY_IMPORT = 1; // Import Directory
    public static final int IMAGE_DIRECTORY_ENTRY_RESOURCE = 2; // Resource Directory
    public static final int IMAGE_DIRECTORY_ENTRY_EXCEPTION = 3; // Exception Directory
    public static final int IMAGE_DIRECTORY_ENTRY_SECURITY = 4; // Security Directory
    public static final int IMAGE_DIRECTORY_ENTRY_BASERELOC = 5; // Base Relocation Table
    public static final int IMAGE_DIRECTORY_ENTRY_DEBUG = 6; // Debug Directory
    //IMAGE_DIRECTORY_ENTRY_COPYRIGHT       7   // (X86 usage)
    public static final int IMAGE_DIRECTORY_ENTRY_ARCHITECTURE = 7; // Architecture Specific Data
    public static final int IMAGE_DIRECTORY_ENTRY_GLOBALPTR = 8; // RVA of GP
    public static final int IMAGE_DIRECTORY_ENTRY_TLS = 9; // TLS Directory
    public static final int IMAGE_DIRECTORY_ENTRY_LOAD_CONFIG = 10;// Load Configuration Directory
    public static final int IMAGE_DIRECTORY_ENTRY_BOUND_IMPORT = 11;// Bound Import Directory in headers
    public static final int IMAGE_DIRECTORY_ENTRY_IAT = 12;// Import Address Table
    public static final int IMAGE_DIRECTORY_ENTRY_DELAY_IMPORT = 13;// Delay Load Import Descriptors
    public static final int IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR = 14;// COM Runtime descriptor
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

    public final ImageDataDirectory[] dataDirectory = new ImageDataDirectory[IMAGE_NUMBEROF_DIRECTORY_ENTRIES];

    public static ParseResult<ImageOptionalHeader> read(LittleEndianReader r) {
        boolean is64bit;
        ImageOptionalHeader hdr = new ImageOptionalHeader();
        try {
            hdr.magic = r.readWord();
            if (hdr.magic != IMAGE_NT_OPTIONAL_HDR32_MAGIC && hdr.magic != IMAGE_NT_OPTIONAL_HDR64_MAGIC)
                return ParseResult.err("Invalid optional header signature");
            is64bit = hdr.is64bit();

            hdr.majorLinkerVersion = r.readByte();
            hdr.minorLinkerVersion = r.readByte();
            hdr.sizeOfCode = r.readDword();
            hdr.sizeOfInitializedData = r.readDword();
            hdr.sizeOfUninitializedData = r.readDword();
            hdr.addressOfEntryPoint = r.readDword();
            hdr.baseOfCode = r.readDword();
            if (!is64bit)
                hdr.baseOfData = r.readDword();
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
            for (int i = 0; i < hdr.dataDirectory.length; ++i) {
                ParseResult<ImageDataDirectory> result = ImageDataDirectory.read(r);
                if (result.isErr())
                    return ParseResult.err(result.getErr());
                hdr.dataDirectory[i] = result.getOk();
            }
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageOptionalHeader", e);
        }

        return ParseResult.ok(hdr);
    }

    public boolean is64bit() {
        return magic == IMAGE_NT_OPTIONAL_HDR64_MAGIC;
    }
}
