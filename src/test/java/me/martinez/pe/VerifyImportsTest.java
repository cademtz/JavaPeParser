package me.martinez.pe;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileReader;

public class VerifyImportsTest {
    @ParameterizedTest
    @MethodSource("paths")
    void test(String path) {
        try {
            run(path);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    static String[] paths() {
        return TestSettings.sampleBinaries;
    }

    private static void run(String filePath) throws Exception {
        CadesFileStream stream = new CadesFileStream(new File(TestSettings.basePath + filePath));
        LittleEndianReader reader = new LittleEndianReader(stream);

        ImagePeHeaders pe = ImagePeHeaders.read(reader);
        JsonObject test_data = Json.parse(new FileReader(TestSettings.basePath + filePath + "_test.json")).asObject();

        JsonArray json_imports = null;
        {
            JsonValue j = test_data.get("imports");
            if (j != null)
                json_imports = j.asArray();
        }
        CheckImports(pe, json_imports);

        JsonArray json_exports = null;
        {
            JsonValue j = test_data.get("exports");
            if (j != null)
                json_exports = j.asArray();
        }
        CheckExports(pe, json_exports);
    }

    private static void CheckImports(ImagePeHeaders pe, JsonArray json_imports) throws Exception {
        if ((pe.importDescriptors == null) != (json_imports == null))
            throw new Exception("Test data shows different absence or existence of import table");

        if (json_imports != null) {
            if (json_imports.size() != pe.getNumCachedImports())
                throw new Exception("Bad number of imported libraries");
        }

        for (int i = 0; i < pe.getNumCachedImports(); ++i) {
            CachedLibraryImports lib = pe.getCachedLibraryImport(i);
            JsonObject json_lib = json_imports.get(i).asObject();
            JsonArray json_entries = json_lib.get("entries").asArray();

            if (!lib.getName().equals(json_lib.get("name").asString()))
                throw new Exception("Imported library has incorrect name or order");
            if (lib.getNumEntries() != json_entries.size())
                throw new Exception("Imported library has bad number of entries");

            for (int c = 0; c < lib.getNumEntries(); ++c) {
                CachedImportEntry entry = lib.getEntry(c);
                JsonObject json_entry = json_entries.get(c).asObject();
                JsonValue json_name = json_entry.get("name");

                if ((entry.getName() == null) != (json_name == null))
                    throw new Exception("Import entry uses name where ordinal should be, or vice versa");
                if (entry.getName() != null) {
                    if (!entry.getName().equals(json_name.asString()))
                        throw new Exception("Import entry has incorrect name or order");
                } else if (entry.getOrdinal() != json_entry.get("ordinal").asLong())
                    throw new Exception("Import entry has incorrect order or ordinal");

                if (entry.getAddress() != json_entry.get("address").asLong())
                    throw new Exception("Import entry has incorrect address");
            }
        }
    }

    private static void CheckExports(ImagePeHeaders pe, JsonArray json_exports) throws Exception {
        if ((pe.exportDirectory == null) != (json_exports == null))
            throw new Exception("Test data shows different absence or existence of export table");

        if (json_exports != null) {
            CachedImageExports exports = pe.getCachedExports();

            if (json_exports.size() != exports.getNumEntries())
                throw new Exception("Bad number of exported entries");

            for (int i = 0; i < exports.getNumEntries(); ++i) {
                CachedExportEntry entry = exports.getEntry(i);
                JsonObject json_entry = json_exports.get(i).asObject();

                if (!entry.getName().equals(json_entry.get("name").asString()))
                    throw new Exception("Export entry has incorrect name or order");
                if (entry.getOrdinal() != json_entry.get("ordinal").asInt())
                    throw new Exception("Export entry has incorrect ordinal or order");
                if (entry.getAddress() != json_entry.get("address").asLong())
                    throw new Exception("Export entry has incorrect address");
            }
        }
    }
}