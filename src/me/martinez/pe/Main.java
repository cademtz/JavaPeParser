package me.martinez.pe;

import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.HexOutput;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {

		//CadesFileStream file = new CadesFileStream("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
		File path = new File("C:\\Program Files (x86)\\Steam\\steam.exe");
		path = new File("C:\\Windows\\System32\\NotificationController.dll");
		CadesFileStream file = new CadesFileStream(path);
		LittleEndianReader r = new LittleEndianReader(file);

		ImagePeHeaders pe = ImagePeHeaders.read(r);
		ImageDataDirectory imp = pe.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

		System.out.println("Import directory RVA: 0x" + HexOutput.dwordToString(imp.virtualAddress));
		System.out.println("Import directory Size: 0x" + HexOutput.dwordToString(imp.size));

		// Warning: ALWAYS null check the import and export directories, as directories are OPTIONAL in the PE format.

		// Print all imported libraries and name(s) included from them
		if (pe.importDescriptors != null) {
			System.out.println(pe.importDescriptors.size() + " Import tables");

			for (int i = 0; i < pe.getNumCachedImports(); ++i) {
				CachedLibraryImports lib = pe.getCachedLibraryImport(i);
				System.out.println(lib.getName());

				for (int c = 0; c < lib.getNumEntries(); c++) {
					CachedImportEntry entry = lib.getEntry(c);
					System.out.print('\t');

					if (entry.getName() != null)
						System.out.println("Name: " + entry.getName());
					else
						System.out.println("Ordinal: " + entry.getOrdinal());
				}
			}
		}

		// Print all name(s) exported
		if (pe.exportDirectory != null) {
			System.out.println(pe.exportDirectory.numberOfNames + " exports");

			for (int i = 0; i < pe.getCachedExports().getNumEntries(); ++i) {
				CachedExportEntry entry = pe.getCachedExports().getEntry(i);
				System.out.print("\tRVA: " +
						HexOutput.dwordToString(entry.getAddress() - pe.ntHeader.optionalHeader.imageBase));
				System.out.println(" Ordinal: " + entry.getOrdinal() + ", Name: " + entry.getName());
			}
		}

		System.out.println("I survived!!");
	}
}
