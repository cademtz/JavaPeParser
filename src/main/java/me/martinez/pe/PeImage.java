package me.martinez.pe;

import me.martinez.pe.headers.*;
import me.martinez.pe.io.CadesStreamReader;
import me.martinez.pe.io.CadesVirtualMemStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseError;
import me.martinez.pe.util.ParseResult;
import me.martinez.pe.util.VirtualAddressException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parse PE headers, optional data, and provide {@link #warnings}.
 * Optional data is stored in a {@link ParseResult} object
 *
 * @see #warnings
 */
public class PeImage {
    public ArrayList<ParseError> warnings = new ArrayList<>();
    public ImageDosHeader dosHeader;
    public ImageNtHeaders ntHeaders;
    public ArrayList<ParseResult<ImageSectionHeader>> sectionHeaders;
    public ParseResult<ArrayList<ImageImportDescriptor>> importDescriptors;
    public ParseResult<ImageExportDirectory> exportDirectory;
    public ParseResult<ArrayList<LibraryImports>> imports;
    public ParseResult<LibraryExport> exports;

    private PeImage() {
    }

    /**
     * Reads required and optional PE headers.
     * Collects warnings about any optional data that is missing, unreadable, or invalid.
     *
     * @param fdata A stream positioned at the start of a PE file/PE file data
     * @return A new {@link PeImage} or an error
     * @see #warnings
     */
    public static ParseResult<PeImage> read(CadesStreamReader fdata) {
        return read(new LittleEndianReader(fdata));
    }

    /**
     * Reads required and optional PE headers.
     * Collects warnings about any optional data that is missing, unreadable, or invalid.
     *
     * @param fdata A stream positioned at the start of a PE file/PE file data
     * @return A new {@link PeImage} or an error
     * @see #warnings
     */
    public static ParseResult<PeImage> read(LittleEndianReader fdata) {
        PeImage pe = new PeImage();

        ParseError err = ImageDosHeader.read(fdata).ifOk(dos -> pe.dosHeader = dos).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        try {
            // Seek to provided address of NT header
            fdata.getStream().seek(pe.dosHeader.lfanew);
        } catch (IOException e) {
            return ParseResult.err("IOException, the address to the NT headers is not readable", e);
        }

        // Try parsing the NT headers
        err = ImageNtHeaders.read(fdata).ifOk(hdrs -> pe.ntHeaders = hdrs).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        // Validate number of sections.
        // "Note that the Windows loader limits the number of sections to 96."
        // https://learn.microsoft.com/en-us/windows/win32/debug/pe-format#coff-file-header-object-and-image
        int numSections = pe.ntHeaders.fileHeader.numberOfSections;
        if (numSections > ImageFileHeader.MAX_WIN_LOADER_SECTIONS) {
            pe.warnings.add(new ParseError(String.format(
                    "ImageFileHeader.numberOfSections (%d) exceeds the Windows loader's max number of sections (%d)",
                    numSections, ImageFileHeader.MAX_WIN_LOADER_SECTIONS
            )));
        }

        // TODO: Validate more stuff:
        // TODO: Is entry point set? Does it point to a valid, executable section?
        // TODO: Is SizeOfCode/SizeOfInitializedData/SizeOfUninitializedData too large?
        // TODO: Is BaseOfCode valid?
        // TODO: Is ImageBase valid?
        // TODO: For each used directory, do they point to valid sections (with appropriate read/write/exec access)?

        // From here, everything must be read by converting virtual addresses to file addresses.
        // Construct a virtual memory stream and make sure it reads in little endian.
        CadesVirtualMemStream vmem = pe.makeVirtualMemStream(fdata.getStream());

        pe.sectionHeaders = new ArrayList<>();
        for (int i = 0; i < pe.ntHeaders.fileHeader.numberOfSections; ++i) {
            ParseResult<ImageSectionHeader> section = ImageSectionHeader.read(vmem);
            if (section.isErr())
                return ParseResult.err("Invalid section: " + section.getErr());
            pe.sectionHeaders.add(section);
        }

        // Try reading raw import descriptor headers
        pe.importDescriptors = pe.readImportDescriptors(vmem);
        if (pe.importDescriptors.isOk()) {
            // If raw import descriptor headers parsed ok, try reading all their listed entries
            pe.imports = pe.readImportDescEntries(vmem, pe.importDescriptors.getOk());
            if (pe.imports.isErr())
                pe.warnings.add(pe.imports.getErr());
        } else {
            pe.warnings.add(pe.importDescriptors.getErr());
            pe.imports = ParseResult.err(pe.importDescriptors.getErr());
        }

        // Try reading raw export headers. Don't emit warnings because it's often unused.
        pe.exportDirectory = pe.readExportDirectory(vmem);

        // If raw export headers parsed ok, try reading all the listed entries
        if (pe.exportDirectory.isOk()) {
            pe.exports = pe.readExportEntries(vmem, pe.exportDirectory.getOk());
            if (pe.exports.isErr())
                pe.warnings.add(pe.exports.getErr());
        } else {
            pe.exports = ParseResult.err(pe.exportDirectory.getErr());
        }
        return ParseResult.ok(pe);
    }

    public boolean is64bit() {
        return ntHeaders.is64bit();
    }

    /**
     * @param fdata Input stream where PE file data starts at position 0
     * @return {@link CadesVirtualMemStream}
     * @see CadesVirtualMemStream
     */
    public CadesVirtualMemStream makeVirtualMemStream(CadesStreamReader fdata) {
        return new CadesVirtualMemStream(this, fdata);
    }

    /**
     * Find which section a virtual address is inside of
     *
     * @param virtualAddr Any virtual address
     * @return Section information, or {@code null}
     */
    public ImageSectionHeader getSection(long virtualAddr) {
        for (ParseResult<ImageSectionHeader> parsedSec : sectionHeaders) {
            if (parsedSec.isErr())
                continue;

            ImageSectionHeader section = parsedSec.getOk();
            long secAddr = ntHeaders.optionalHeader.imageBase + section.virtualAddress;
            if (virtualAddr >= secAddr && virtualAddr < secAddr + section.getVirtualSize())
                return section;
        }
        return null;
    }

    /**
     * Find the file address of initial data for a virtual address.
     * <br>
     * Some sections only have partial initial data or none at all.
     * For those sections, use {@link #getSection(long)}
     *
     * @param virtualAddr Any virtual address
     * @return File address
     * @throws VirtualAddressException Address is not within a section's initialized memory
     * @see #getSection(long)
     */
    public long getFileAddress(long virtualAddr) throws VirtualAddressException {
        ImageSectionHeader section = getSection(virtualAddr);
        if (section == null)
            throw new VirtualAddressException("Address is not inside a section", virtualAddr);

        boolean isInitialized = section.sizeOfRawData > 0;
        if (!isInitialized)
            throw new VirtualAddressException("Address is inside a section with no file data (uninitialized section)", virtualAddr);

        long secAddr = ntHeaders.optionalHeader.imageBase + section.virtualAddress;
        long secOffset = virtualAddr - secAddr;
        if (secOffset > section.sizeOfRawData)
            throw new VirtualAddressException("Address is beyond the section's initialized data on file", virtualAddr);

        return section.pointerToRawData + secOffset;
    }

    /**
     * Reads (or re-reads) the import table.
     *
     * @param vmem Virtual memory stream of this PE's data
     * @see PeImage#makeVirtualMemStream
     * @see PeImage#importDescriptors
     */
    public ParseResult<ArrayList<ImageImportDescriptor>> readImportDescriptors(CadesVirtualMemStream vmem) {
        LittleEndianReader r = new LittleEndianReader(vmem);
        ImageDataDirectory imp = ntHeaders.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

        if (imp == null || imp.virtualAddress == 0)
            return ParseResult.err("Missing or empty import table");

        try {
            r.getStream().seek(ntHeaders.optionalHeader.imageBase + imp.virtualAddress);
        } catch (IOException e) {
            return ParseResult.err("IOException, the address to the import directory is not readable", e);
        }

        ArrayList<ImageImportDescriptor> importDescs = new ArrayList<>();
        ParseResult<ImageImportDescriptor> desc = ImageImportDescriptor.read(r);
        // Read until null-terminated characteristics value
        while (desc.isOk() && desc.getOk().getCharacteristics() != 0) {
            importDescs.add(desc.getOk());
            desc = ImageImportDescriptor.read(r);
        }

        if (!desc.isOk())
            return ParseResult.err(desc.getErr());

        return ParseResult.ok(importDescs);
    }

    public ParseResult<ArrayList<LibraryImports>> readImportDescEntries(
            CadesVirtualMemStream vmem,
            Collection<ImageImportDescriptor> descList
    ) {
        LittleEndianReader r = new LittleEndianReader(vmem);
        long baseVaddr = ntHeaders.optionalHeader.imageBase;

        ArrayList<LibraryImports> newImps = new ArrayList<>();

        try {
            for (ImageImportDescriptor desc : descList) {
                String name;
                List<Long> lookupTable;
                List<ImageImportByName> nameTable;
                List<ImportEntry> entries;

                // Read library name
                r.getStream().seek(desc.name + baseVaddr);
                name = r.readNullTerminatedString(-1);

                // Read null-terminated array of pointers (RVAs) to ImageImportByName
                r.getStream().seek(desc.getOriginalFirstThunk() + baseVaddr);
                lookupTable = r.readTerminatedValues(is64bit() ? 64 : 32, 0, -1);

                // Read each ImageImportByName pointed to in the table
                nameTable = new ArrayList<>();
                for (Long rva : lookupTable) {
                    // Check if rva should be interpreted as an ordinal
                    ParseResult<ImageImportByName> imp = ImageImportByName.read(rva, is64bit());

                    if (imp.isErr()) { // Nope. Try interpreting as a string address then.
                        r.getStream().seek(rva + baseVaddr);
                        imp = ImageImportByName.read(r, is64bit());
                    }

                    if (imp.isErr())
                        return ParseResult.err(imp.getErr());

                    nameTable.add(imp.getOk());
                }

                if (nameTable.size() != lookupTable.size())
                    return ParseResult.err("Import descriptor's name and address table sizes are mismatched");

                long firstThunkAddress = baseVaddr + desc.firstThunk;
                long thunkSize = is64bit() ? 8 : 4;

                // Putting it all together
                entries = new ArrayList<>();
                for (int i = 0; i < lookupTable.size(); ++i) {
                    ImageImportByName entry = nameTable.get(i);
                    entries.add(new ImportEntry(entry.name, entry.ordinal, firstThunkAddress + i * thunkSize));
                }

                newImps.add(new LibraryImports(name, desc.timeDateStamp, desc.forwarderChain, entries));
            }
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageImportByName entry", e);
        }

        return ParseResult.ok(newImps);
    }

    public ParseResult<ImageExportDirectory> readExportDirectory(CadesVirtualMemStream vmem) {
        LittleEndianReader r = new LittleEndianReader(vmem);
        long baseVaddr = ntHeaders.optionalHeader.imageBase;
        ImageDataDirectory exp = ntHeaders.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_EXPORT);

        if (exp == null || exp.virtualAddress == 0)
            return ParseResult.err("Missing or empty export table");

        try {
            r.getStream().seek(exp.virtualAddress + baseVaddr);
        } catch (IOException e) {
            return ParseResult.err("IOException, the address to the export directory is invalid", e);
        }

        return ImageExportDirectory.read(r);
    }

    public ParseResult<LibraryExport> readExportEntries(CadesVirtualMemStream vmem, ImageExportDirectory expDir) {
        LittleEndianReader r = new LittleEndianReader(vmem);
        String name;
        long[] funcsTable;
        long[] namesTable;
        long[] ordsTable;
        ExportEntry[] entries;
        long baseVaddr = ntHeaders.optionalHeader.imageBase;

        try {
            r.getStream().seek(expDir.name + baseVaddr);
            name = r.readNullTerminatedString(-1);

            r.getStream().seek(expDir.addressOfFunctions + baseVaddr);
            funcsTable = r.readValues(32, (int) expDir.numberOfFunctions);

            r.getStream().seek(expDir.addressOfNames + baseVaddr);
            namesTable = r.readValues(32, (int) expDir.numberOfNames);

            r.getStream().seek(expDir.addressOfNameOrdinals + baseVaddr);
            ordsTable = r.readValues(16, (int) expDir.numberOfNames);

            entries = new ExportEntry[namesTable.length];
            for (int i = 0; i < (int) expDir.numberOfNames; ++i) {
                int entryOrd;
                long entryVa;
                String entryName;

                try {
                    entryOrd = (int) ordsTable[i];
                    entryVa = funcsTable[entryOrd] + baseVaddr;

                    r.getStream().seek(namesTable[i] + baseVaddr);
                    entryName = r.readNullTerminatedString(-1);
                } catch (IndexOutOfBoundsException e) { // wholesome 100
                    return ParseResult.err("IndexOutOfBoundsException, invalid export ordinal to name or function table", e);
                }

                entries[i] = new ExportEntry(entryName, entryOrd, entryVa);
            }
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read export table(s)", e);
        }

        return ParseResult.ok(new LibraryExport(name, entries));
    }
}
