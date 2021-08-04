package com.company;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        //CadesFileStream file = new CadesFileStream("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
        CadesFileStream file = new CadesFileStream("C:\\Program Files (x86)\\Steam\\steam.exe");
        LittleEndianReader r = new LittleEndianReader(file);

        ImagePeHeaders pe = ImagePeHeaders.read(r);
        ImageDataDirectory imp = pe.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

        System.out.println("Import directory RVA: 0x" + HexOutput.dwordToString(imp.virtualAddress));
        System.out.println("Import directory Size: 0x" + HexOutput.dwordToString(imp.size));

        System.out.println(pe.importDescriptors.size() + " Import tables");

        for (int i = 0; i < pe.getNumCachedImports(); ++i)
        {
            CachedLibraryImports lib = pe.getCachedLibraryImport(i);
            System.out.println(lib.getName());

            for (int c = 0; c < lib.getNumEntries(); c++)
            {
                CachedImportEntry entry = lib.getEntry(c);
                System.out.print('\t');

                if (entry.getName() != null)
                    System.out.println("Name: " + entry.getName());
                else
                    System.out.println("Ordinal: " + entry.getOrdinal());
            }
        }

        System.out.println("I survived!!");
    }
}
