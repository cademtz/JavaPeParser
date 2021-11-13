package me.martinez.pe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.HexOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestDataGenerator {
	public static void main() throws IOException {
		for (String path : TestSettings.sampleBinaries) {
			run(path);
		}
	}

	private static void run(String filePath) throws IOException {
		CadesFileStream stream = new CadesFileStream(new File(TestSettings.basePath + filePath));
		LittleEndianReader reader = new LittleEndianReader(stream);

		ImagePeHeaders pe = ImagePeHeaders.read(reader);
		ImageDataDirectory imp = pe.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

		JsonObject test_data = new JsonObject();

		System.out.println("Import directory RVA: 0x" + HexOutput.dwordToString(imp.virtualAddress));
		System.out.println("Import directory Size: 0x" + HexOutput.dwordToString(imp.size));

		// Warning: ALWAYS null check the import and export directories,
		// as directories are OPTIONAL in the PE format.

		// Print all imported libraries and name(s) included from them
		if (pe.importDescriptors != null) {
			JsonArray json_imports = new JsonArray();
			test_data.add("imports", json_imports);

			System.out.println(pe.importDescriptors.size() + " Import tables");

			for (int i = 0; i < pe.getNumCachedImports(); ++i) {
				CachedLibraryImports lib = pe.getCachedLibraryImport(i);
				JsonObject json_lib = new JsonObject();
				JsonArray json_entries = new JsonArray();

				json_lib.add("name", lib.getName());
				json_lib.add("entries", json_entries);
				json_imports.add(json_lib);

				System.out.println(lib.getName());

				for (int c = 0; c < lib.getNumEntries(); c++) {
					CachedImportEntry entry = lib.getEntry(c);
					JsonObject json_entry = new JsonObject();

					json_entry.add("address", entry.getAddress());
					json_entries.add(json_entry);

					System.out.print('\t');

					if (entry.getName() != null) {
						json_entry.add("name", entry.getName());
						System.out.println("Name: " + entry.getName());
					} else {
						json_entry.add("ordinal", entry.getOrdinal());
						System.out.println("Ordinal: " + entry.getOrdinal());
					}
				}
			}
		}

		// Print all name(s) exported
		if (pe.exportDirectory != null) {
			JsonArray json_exports = new JsonArray();
			test_data.add("exports", json_exports);

			System.out.println(pe.exportDirectory.numberOfNames + " exports");

			for (int i = 0; i < pe.getCachedExports().getNumEntries(); ++i) {
				CachedExportEntry entry = pe.getCachedExports().getEntry(i);
				JsonObject json_entry = new JsonObject();

				json_entry.add("name", entry.getName());
				json_entry.add("ordinal", entry.getOrdinal());
				json_entry.add("address", entry.getAddress());
				json_exports.add(json_entry);

				System.out.print("\tRVA: " +
						HexOutput.dwordToString(entry.getAddress() - pe.ntHeader.optionalHeader.imageBase));
				System.out.println(" Ordinal: " + entry.getOrdinal() + ", Name: " + entry.getName());
			}
		}

		FileWriter w = new FileWriter(TestSettings.basePath + filePath + "_test.json");
		test_data.writeTo(w);
		w.close();

		System.out.println("I survived!!");
	}
}
