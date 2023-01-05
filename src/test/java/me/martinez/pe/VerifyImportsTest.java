package me.martinez.pe;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class VerifyImportsTest {
    @ParameterizedTest
    @MethodSource("paths")
    public void test(String path) {
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
        JsonObject testData;
        boolean testIsInvalid;

        try (FileReader fr = new FileReader(TestSettings.basePath + filePath + "_test.json")) {
            testData = Json.parse(fr).asObject();
            JsonValue j = testData.get("isInvalid");
            if (j != null)
                testIsInvalid = j.asBoolean();
            else
                throw new Exception("Test data is missing the \"isInvalid\" field");
        } catch (FileNotFoundException e) {
            throw new Exception("Test data was not found! Be sure to run TestDataGenerator when adding/updating binaries!");
        }

        CadesFileStream stream = new CadesFileStream(new File(TestSettings.basePath + filePath));
        LittleEndianReader reader = new LittleEndianReader(stream);

        PeImage pe = PeImage.read(reader).ifErr(err -> {
            System.out.println("Error: " + err);
        }).ifOk(val -> {
            for (ParseError warning : val.warnings)
                System.out.println("Warning: " + warning.toString());
        }).getOkOrDefault(null);

        if (pe == null != testIsInvalid)
            throw new Exception(String.format("Expected isInvalid=%s, got %s", testIsInvalid, pe == null));

        if (pe != null) {
            checkImports(testData, pe);
            checkExports(testData, pe);
        }
    }

    private static void checkImports(JsonObject testData, PeImage pe) throws Exception {
        JsonArray jsonImports = null;
        {
            JsonValue j = testData.get("imports");
            if (j != null)
                jsonImports = j.asArray();
        }

        boolean testHasImports = jsonImports != null;
        boolean hasImports = pe.imports.isOk();

        if (hasImports != (testHasImports))
            throw new Exception(String.format("Expected hasImports=%s, got %s", testHasImports, hasImports));

        if (!hasImports)
            return; // No imports to check here!

        ArrayList<LibraryImports> cachedImps = pe.imports.getOk();

        if (jsonImports.size() != cachedImps.size())
            throw new Exception(String.format("Expected imports.size=%s, got %s", jsonImports.size(), cachedImps.size()));

        for (int i = 0; i < cachedImps.size(); ++i) {
            LibraryImports lib = cachedImps.get(i);
            JsonObject json_lib = jsonImports.get(i).asObject();
            JsonArray json_entries = json_lib.get("entries").asArray();

            if (!lib.name.equals(json_lib.get("name").asString()))
                throw new Exception("Imported library has incorrect name or order");
            if (lib.entries.size() != json_entries.size())
                throw new Exception("Imported library has incorrect number of entries");

            for (int c = 0; c < lib.entries.size(); ++c) {
                ImportEntry entry = lib.entries.get(c);
                JsonObject json_entry = json_entries.get(c).asObject();
                JsonValue json_name = json_entry.get("name");
                JsonValue json_ordinal = json_entry.get("ordinal");

                if ((entry.name == null) != (json_name == null)) {
                    throw new Exception("Import entry uses name where ordinal should be, or vice versa");
                } else if (entry.name != null) {
                    if (!entry.name.equals(json_name.asString()))
                        throw new Exception("Import entry has incorrect name or order");
                }

                if ((entry.ordinal == null) != (json_ordinal == null)) {
                    throw new Exception("Import entry uses name where ordinal should be, or vice versa");
                } else if (entry.ordinal != null) {
                    if (entry.ordinal != json_ordinal.asLong())
                        throw new Exception("Import entry has incorrect ordinal or order");
                }

                if (entry.thunkAddress != json_entry.get("thunkAddress").asLong())
                    throw new Exception("Import entry has incorrect address");
            }
        }
    }

    private static void checkExports(JsonObject testData, PeImage pe) throws Exception {
        JsonArray jsonExports = null;
        {
            JsonValue j = testData.get("exports");
            if (j != null)
                jsonExports = j.asArray();
        }

        boolean testHasExports = jsonExports != null;
        boolean hasExports = pe.exports.isOk();

        if (hasExports != testHasExports)
            throw new Exception(String.format("Expected hasExports=%s, got %s", testHasExports, hasExports));

        if (!hasExports)
            return; // No exports to check here!

        LibraryExport exports = pe.exports.getOk();

        if (jsonExports.size() != exports.entries.length)
            throw new Exception(String.format("Expected exports.size=%s, got %s", jsonExports.size(), exports.entries.length));

        for (int i = 0; i < exports.entries.length; ++i) {
            ExportEntry entry = exports.entries[i];
            JsonObject json_entry = jsonExports.get(i).asObject();

            if (!entry.name.equals(json_entry.get("name").asString()))
                throw new Exception("Export entry has incorrect name or order");
            if (entry.ordinal != json_entry.get("ordinal").asInt())
                throw new Exception("Export entry has incorrect ordinal or order");
            if (entry.address != json_entry.get("address").asLong())
                throw new Exception("Export entry has incorrect address");
        }
    }
}