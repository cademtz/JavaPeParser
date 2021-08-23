package me.martinez.pe;

import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.HexOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileNotFoundException;

public class ImportPrintingTests {
	private static final String basePath = "src/test/resources/";

	@ParameterizedTest
	@ValueSource(strings = {
			"paint-10-x32+.exe",
			"paint-XP-x32.exe",
			"java-zip.dll"
	})
	void test(String path) {
		try {
			run(path);
		} catch (Exception ex) {
			Assertions.fail(ex);
		}
	}

	private static void run(String filePath) throws FileNotFoundException {
		CadesFileStream stream = new CadesFileStream(new File(basePath + filePath));
		LittleEndianReader reader = new LittleEndianReader(stream);

		ImagePeHeaders pe = ImagePeHeaders.read(reader);
		ImageDataDirectory imp = pe.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

		System.out.println("Import directory RVA: 0x" + HexOutput.dwordToString(imp.virtualAddress));
		System.out.println("Import directory Size: 0x" + HexOutput.dwordToString(imp.size));

		// Warning: ALWAYS null check the import and export directories,
		// as directories are OPTIONAL in the PE format.

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
