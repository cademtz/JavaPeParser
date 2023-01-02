package me.martinez.pe;

import me.martinez.pe.io.CadesStreamReader;
import me.martinez.pe.io.CadesVirtualMemStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.GenericError;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImagePeHeaders {
    public ArrayList<GenericError> warnings = new ArrayList<>();
    public ImageDosHeader dosHeader;
    public ImageNtHeaders ntHeaders;
    public ArrayList<ParseResult<ImageSectionHeader>> sectionHeaders;
    public ParseResult<ArrayList<ImageImportDescriptor>> importDescriptors;
    public ParseResult<ImageExportDirectory> exportDirectory;
    public ParseResult<ArrayList<CachedLibraryImports>> cachedImps;
    public ParseResult<CachedImageExports> cachedExps;

    private ImagePeHeaders() {
    }

    /**
     * Reads required and optional PE headers.
     * Collects warnings about any optional data that is missing, unreadable, or invalid.
     *
     * @param fmem A data stream positioned at the start of a PE file/PE file data
     * @return A new {@link ImagePeHeaders} or an error
     * @see #warnings
     */
    public static ParseResult<ImagePeHeaders> read(LittleEndianReader fmem) {
        ImagePeHeaders pe = new ImagePeHeaders();

        GenericError err = ImageDosHeader.read(fmem).ifOk(dos -> pe.dosHeader = dos).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        try {
            // Seek to provided address of NT header
            fmem.getStream().seek(pe.dosHeader.lfanew);
        } catch (IOException e) {
            return ParseResult.err("IOException, the address to the NT headers is not readable", e);
        }

        // Try parsing the NT headers
        err = ImageNtHeaders.read(fmem).ifOk(hdrs -> pe.ntHeaders = hdrs).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        // Validate number of sections.
        // "Note that the Windows loader limits the number of sections to 96."
        // https://learn.microsoft.com/en-us/windows/win32/debug/pe-format#coff-file-header-object-and-image
        int numSections = pe.ntHeaders.fileHeader.numberOfSections;
        if (numSections > ImageFileHeader.MAX_WIN_LOADER_SECTIONS) {
            pe.warnings.add(new GenericError(String.format(
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
        LittleEndianReader vmem = new LittleEndianReader(pe.makeVirtualMemStream(fmem.getStream()));

        pe.sectionHeaders = new ArrayList<>();
        for (int i = 0; i < pe.ntHeaders.fileHeader.numberOfSections; ++i)
            pe.sectionHeaders.add(ImageSectionHeader.read(vmem));

        // Try reading raw import descriptor headers
        pe.importDescriptors = pe.readImportDescriptors(vmem);
        if (pe.importDescriptors.isOk()) {
            // If raw import descriptor headers parsed ok, try reading all their listed entries
            pe.cachedImps = pe.readImportDescEntries(vmem, pe.importDescriptors.getOk());
            if (pe.cachedImps.isErr())
                pe.warnings.add(pe.cachedImps.getErr());
        } else {
            pe.warnings.add(pe.importDescriptors.getErr());
            pe.cachedImps = ParseResult.err(pe.importDescriptors.getErr());
        }

        // Try reading raw export headers. Don't emit warnings because it's often unused.
        pe.exportDirectory = pe.readExportDirectory(vmem);

        // If raw export headers parsed ok, try reading all the listed entries
        if (pe.exportDirectory.isOk()) {
            pe.cachedExps = pe.readExportEntries(vmem, pe.exportDirectory.getOk());
            if (pe.cachedExps.isErr())
                pe.warnings.add(pe.cachedExps.getErr());
        } else {
            pe.cachedExps = ParseResult.err(pe.exportDirectory.getErr());
        }
        return ParseResult.ok(pe);
    }

    public boolean is64bit() {
        return ntHeaders.is64bit();
    }

    public CadesVirtualMemStream makeVirtualMemStream(CadesStreamReader rawFile) {
        return new CadesVirtualMemStream(this, rawFile);
    }

    /**
     * Reads (or re-reads) the import table.
     *
     * @param r Virtual memory stream of this PE's data
     * @see ImagePeHeaders#makeVirtualMemStream
     * @see ImagePeHeaders#importDescriptors
     */
    public ParseResult<ArrayList<ImageImportDescriptor>> readImportDescriptors(LittleEndianReader r) {
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

    public ParseResult<ArrayList<CachedLibraryImports>> readImportDescEntries(
            LittleEndianReader r,
            Collection<ImageImportDescriptor> descList
    ) {
        long baseVaddr = ntHeaders.optionalHeader.imageBase;

        ArrayList<CachedLibraryImports> newImps = new ArrayList<>();

        try {
            for (ImageImportDescriptor desc : descList) {
                String name;
                List<Long> rvaTable;
                List<ImageImportByName> nameTable;
                List<CachedImportEntry> entries;

                // Read library name
                r.getStream().seek(desc.name + baseVaddr);
                name = r.readNullTerminatedString(-1);

                // Read null-terminated array of pointers (RVAs) to ImageImportByName
                r.getStream().seek(desc.getOriginalFirstThunk() + baseVaddr);
                rvaTable = r.readTerminatedValues(is64bit() ? 64 : 32, 0, -1);

                // Read each ImageImportByName pointed to in the table
                nameTable = new ArrayList<>();
                for (Long rva : rvaTable) {
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

                // Read null-terminated array of pointers (RVAs) to destination address of import
                r.getStream().seek(desc.firstThunk + baseVaddr);
                rvaTable = r.readTerminatedValues(is64bit() ? 64 : 32, 0, -1);

                if (rvaTable.size() != nameTable.size()) {
                    return ParseResult.err("Import descriptor's name and address table sizes are mismatched");
                }

                // Putting it all together
                entries = new ArrayList<>();
                for (int i = 0; i < rvaTable.size(); ++i) {
                    ImageImportByName entry = nameTable.get(i);
                    entries.add(new CachedImportEntry(entry.name, entry.ordinal, rvaTable.get(i)));
                }

                newImps.add(new CachedLibraryImports(name, desc.timeDateStamp, desc.forwarderChain, entries));
            }
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageImportByName entry", e);
        }

        return ParseResult.ok(newImps);
    }

    public ParseResult<ImageExportDirectory> readExportDirectory(LittleEndianReader r) {
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

    public ParseResult<CachedImageExports> readExportEntries(LittleEndianReader r, ImageExportDirectory expDir) {
        String name;
        long[] funcsTable;
        long[] namesTable;
        long[] ordsTable;
        CachedExportEntry[] entries;
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

            entries = new CachedExportEntry[namesTable.length];
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

                entries[i] = new CachedExportEntry(entryName, entryOrd, entryVa);
            }
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read export table(s)", e);
        }

        return ParseResult.ok(new CachedImageExports(name, entries));
    }
}
