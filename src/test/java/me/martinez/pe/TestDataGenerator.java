package me.martinez.pe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.GenericError;
import me.martinez.pe.util.HexOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TestDataGenerator {
    /*@ParameterizedTest
    @MethodSource("paths")
    public void test(String path) {
        try {
            run(path);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }*/

    public static void main(String[] args) {
        for (String path : paths()) {
            try {
                run(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String[] paths() {
        return TestSettings.sampleBinaries;
    }

    private static void run(String filePath) throws IOException {
        System.out.println("Parsing " + filePath);

        CadesFileStream stream = new CadesFileStream(new File(TestSettings.basePath + filePath));
        LittleEndianReader reader = new LittleEndianReader(stream);
        ImagePeHeaders pe = ImagePeHeaders.read(reader).ifErr(err -> {
            System.out.println("Error: " + err);
        }).ifOk(val -> {
            for (GenericError warning : val.warnings)
                System.out.println("Warning: " + warning);
        }).getOkOrDefault(null);

        File outFile = new File(TestSettings.basePath + filePath + "_test.json");
        if (outFile.exists())
            return;

        try (FileWriter w = new FileWriter(outFile)) {
            JsonObject testData = new JsonObject();

            testData.add("isInvalid", pe == null);

            if (pe != null) {
                ImageDataDirectory imp = pe.ntHeaders.getDataDirectory(ImageOptionalHeader.IMAGE_DIRECTORY_ENTRY_IMPORT);

                System.out.println("Import directory RVA: 0x" + HexOutput.dwordToString(imp.virtualAddress));
                System.out.println("Import directory Size: 0x" + HexOutput.dwordToString(imp.size));

                appendImportData(testData, pe);
                appendExportData(testData, pe);
            }

            testData.writeTo(w);
            w.close();

            System.out.println("I survived!!");
        }
    }

    private static void appendImportData(JsonObject testData, ImagePeHeaders pe) {
        if (pe.cachedImps.isErr())
            return;

        JsonArray jsonImports = new JsonArray();
        testData.add("imports", jsonImports);

        ArrayList<CachedLibraryImports> cachedImps = pe.cachedImps.getOk();
        for (CachedLibraryImports lib : cachedImps) {
            JsonObject jsonLib = new JsonObject();
            JsonArray json_entries = new JsonArray();

            jsonLib.add("name", lib.name);
            jsonLib.add("entries", json_entries);
            jsonImports.add(jsonLib);

            System.out.println(lib.name);

            for (CachedImportEntry entry : lib.entries) {
                JsonObject jsonEntry = new JsonObject();

                jsonEntry.add("thunkAddress", entry.thunkAddress);
                json_entries.add(jsonEntry);

                System.out.print('\t');

                if (entry.name != null) {
                    jsonEntry.add("name", entry.name);
                    System.out.println("Name: " + entry.name);
                }
                if (entry.ordinal != null) {
                    jsonEntry.add("ordinal", entry.ordinal);
                    System.out.println("Ordinal: " + entry.ordinal);
                }
            }
        }
    }

    public static void appendExportData(JsonObject testData, ImagePeHeaders pe) {
        if (pe.exportDirectory.isErr())
            return;

        JsonArray jsonExports = new JsonArray();
        testData.add("exports", jsonExports);

        ImageExportDirectory expDir = pe.exportDirectory.getOk();
        System.out.println(expDir.numberOfNames + " exports");

        if (pe.cachedExps.isOk()) {
            CachedImageExports exports = pe.cachedExps.getOk();

            for (CachedExportEntry entry : exports.entries) {
                JsonObject jsonEntry = new JsonObject();

                jsonEntry.add("name", entry.name);
                jsonEntry.add("ordinal", entry.ordinal);
                jsonEntry.add("address", entry.address);
                jsonExports.add(jsonEntry);

                System.out.print("\tRVA: " +
                        HexOutput.dwordToString(entry.address - pe.ntHeaders.optionalHeader.imageBase));
                System.out.println(" Ordinal: " + entry.ordinal + ", Name: " + entry.name);
            }
        }
    }
}
