package me.martinez.pe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestSettings {
    public static final String basePath = "src/test/resources/";
    public static final String[] sampleBinaries = {
            "paint-10-x32+.exe",
            "paint-XP-x32.exe",
            "java-zip.dll",
            "ctest.exe",
            "ctest_mangled_dos.exe",
            "ctest_mangled_nt.exe",
            "ctest_mangled_import.exe"
    };

    public static List<Path> paths() throws IOException {
        return Files.walk(Paths.get("src/test/resources"))
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());
    }
}
