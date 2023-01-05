package me.martinez.pe;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import me.martinez.pe.headers.ImageDataDirectory;
import me.martinez.pe.headers.ImageExportDirectory;
import me.martinez.pe.headers.ImageOptionalHeader;
import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.io.CadesStreamReader;
import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.HexOutput;
import me.martinez.pe.util.ParseError;

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

    static String[] paths() {
        return TestSettings.sampleBinaries;
    }

    public static void main(String[] args) {
        for (String path : paths()) {
            try {
                run(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void example(String[] args) {
        CadesStreamReader stream;
        try {
            stream = new CadesFileStream(new File(TestSettings.basePath + "ctest.exe"));
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        PeImage pe = PeImage.read(stream).ifErr(err -> {
            System.out.println("Error: " + err);
        }).ifOk(val -> {
            for (ParseError warning : val.warnings)
                System.out.println("Warning: " + warning);
        }).getOkOrDefault(null);

        if (pe != null) {
            System.out.println("is64bit: " + pe.ntHeaders.is64bit());

            pe.imports.ifOk(imports -> {
                for (LibraryImports lib : imports) {
                    System.out.printf("%s imports from %s:%n", lib.entries.size(), lib.name);
                    for (ImportEntry entry : lib.entries)
                        System.out.printf("\tname=%s, ordinal=%s%n", entry.name, entry.ordinal);
                }
            }).ifErr(err -> System.out.printf("No imports: %s%n", err.toString()));

            pe.exports.ifOk(exports -> {
                System.out.printf("This file exports under the library name \"%s\"%n", exports.name);
                for (ExportEntry entry : exports.entries)
                    System.out.printf("\tname=%s, ordinal=%s%n", entry.name, entry.ordinal);
            }).ifErr(err -> System.out.printf("No exports: %s%n", err.toString()));
        }
    }

    private static void run(String filePath) throws IOException {
        System.out.println("Parsing " + filePath);

        CadesFileStream stream = new CadesFileStream(new File(TestSettings.basePath + filePath));
        PeImage pe = PeImage.read(new LittleEndianReader(stream)).ifErr(err -> {
            System.out.println("Error: " + err);
        }).ifOk(val -> {
            for (ParseError warning : val.warnings)
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

    private static void appendImportData(JsonObject testData, PeImage pe) {
        if (pe.imports.isErr())
            return;

        JsonArray jsonImports = new JsonArray();
        testData.add("imports", jsonImports);

        ArrayList<LibraryImports> cachedImps = pe.imports.getOk();
        for (LibraryImports lib : cachedImps) {
            JsonObject jsonLib = new JsonObject();
            JsonArray json_entries = new JsonArray();

            jsonLib.add("name", lib.name);
            jsonLib.add("entries", json_entries);
            jsonImports.add(jsonLib);

            System.out.println(lib.name);

            for (ImportEntry entry : lib.entries) {
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

    public static void appendExportData(JsonObject testData, PeImage pe) {
        if (pe.exportDirectory.isErr())
            return;

        JsonArray jsonExports = new JsonArray();
        testData.add("exports", jsonExports);

        ImageExportDirectory expDir = pe.exportDirectory.getOk();
        System.out.println(expDir.numberOfNames + " exports");

        if (pe.exports.isOk()) {
            LibraryExport exports = pe.exports.getOk();

            for (ExportEntry entry : exports.entries) {
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
