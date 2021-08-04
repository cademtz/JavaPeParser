package com.company;

import java.io.IOException;
import java.util.ArrayList;

public class ImagePeHeaders {
    public ImageDosHeader dosHeader;
    public ImageNtHeaders ntHeader;
    public ImageSectionHeader[] sectionHeaders;
    /**
     * Null value means invalid import table.
     * Empty ArrayList means import table is unused or empty.
     */
    public ArrayList<ImageImportDescriptor> importDescriptors;
    private ArrayList<CachedLibraryImports> cachedImps;

    public static ImagePeHeaders read(LittleEndianReader r) {
        CadesVirtualMemStream vmem;
        ImagePeHeaders pe = new ImagePeHeaders();

        try {
            pe.dosHeader = ImageDosHeader.read(r);
            if (pe.dosHeader == null)
                return null;

            // Seek to provided address of NT header
            r.getStream().seek(pe.dosHeader.lfanew);

            pe.ntHeader = ImageNtHeaders.read(r);
            if (pe.ntHeader == null)
                return null;

            pe.sectionHeaders = new ImageSectionHeader[pe.ntHeader.fileHeader.numberOfSections];
            for (int i = 0; i < pe.sectionHeaders.length; ++i)
                pe.sectionHeaders[i] = ImageSectionHeader.read(r);
        }
        catch (IOException e) { return null; }

        vmem = pe.makeVirtualMemStream(r.getStream());
        pe.updateImports(vmem);

        return pe;
    }

    public boolean is64bit() { return ntHeader.is64bit(); }

    public ImageDataDirectory getDataDirectory(int index) { return ntHeader.getDataDirectory(index); }

    public int getNumCachedImports() { return cachedImps.size(); }
    public CachedLibraryImports getCachedLibraryImport(int index) { return cachedImps.get(index); }

    public CadesVirtualMemStream makeVirtualMemStream(CadesStreamReader rawFile) {
        return new CadesVirtualMemStream(this, rawFile);
    }

    /**
     * Reads (or re-reads) the import table.
     * @param vmem Virtual memory stream of this PE's data
     * @see ImagePeHeaders#makeVirtualMemStream
     * @see ImagePeHeaders#importDescriptors
     */
    public void updateImports(CadesVirtualMemStream vmem)
    {
        ImageImportDescriptor desc;
        LittleEndianReader r = new LittleEndianReader(vmem);
        ImageDataDirectory imp = getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

        importDescriptors = null; // Null-initialize. Will be set if read is successful.
        cachedImps = null;

        if (imp == null || imp.virtualAddress == 0)
            return;

        try {
            r.getStream().seek(ntHeader.optionalHeader.imageBase + imp.virtualAddress);
            importDescriptors = new ArrayList<>();
            desc = ImageImportDescriptor.read(r);

            // Read until null-terminated characteristics value
            while (desc != null && desc.getCharacteristics() != 0) {
                importDescriptors.add(desc);
                desc = ImageImportDescriptor.read(r);
            }

            if (desc == null)
            {
                importDescriptors = null;
                return;
            }

            updateImportCache(r);
        }
        catch (IOException e) {
            importDescriptors = null;
            cachedImps = null;
        }
    }

    private void updateImportCache(LittleEndianReader r) throws IOException
    {
        long baseVaddr = ntHeader.optionalHeader.imageBase;

        if (cachedImps == null)
            cachedImps = new ArrayList<>();
        else
            cachedImps.clear();

        for (ImageImportDescriptor desc : importDescriptors)
        {
            String name;
            ImageImportByName imp;
            ArrayList<Long> rvaTable;
            ArrayList<ImageImportByName> nameTable;
            ArrayList<CachedImportEntry> entries;

            // Read library name
            r.getStream().seek(desc.name + baseVaddr);
            name = r.readNullTerminatedString(-1);

            // Read null-terminated array of pointers (RVAs) to ImageImportByName
            r.getStream().seek(desc.getOriginalFirstThunk() + baseVaddr);
            rvaTable = r.readTerminatedValues(is64bit() ? 64 : 32, 0, -1);

            // Read each ImageImportByName pointed to in the table
            nameTable = new ArrayList<>();
            for (Long rva : rvaTable)
            {
                // Check if rva is actually read as an ordinal
                imp = ImageImportByName.read(rva, is64bit());

                if (imp == null) { // Nope. Points to extra ImageImportByName then.
                    r.getStream().seek(rva + baseVaddr);
                    imp = ImageImportByName.read(r, is64bit());
                }

                // Failed to read? Then cause an exception in updateImports to delete EVARYTHING!!!
                if (imp == null)
                    throw new IOException("Failed to read ImageImportByName");

                nameTable.add(imp);
            }

            // Read null-terminated array of pointers (RVAs) to destination address of import
            r.getStream().seek(desc.firstThunk + baseVaddr);
            rvaTable = r.readTerminatedValues(is64bit() ? 64 : 32, 0, -1);

            if (rvaTable.size() != nameTable.size())
                throw new IOException("Import descriptor's name and address table don't match");

            // Putting it all together
            entries = new ArrayList<>();
            for (int i = 0; i < rvaTable.size(); ++i)
            {
                ImageImportByName entry = nameTable.get(i);
                entries.add(new CachedImportEntry(entry.getName(), entry.getOrdinal(), rvaTable.get(i)));
            }

            cachedImps.add(new CachedLibraryImports(name, desc.timeDateStamp, desc.forwarderChain, entries));
        }
    }
}