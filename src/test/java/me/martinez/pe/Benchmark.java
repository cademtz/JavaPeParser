package me.martinez.pe;

import java.io.File;
import java.io.IOException;

import me.martinez.pe.io.CadesFileStream;
import me.martinez.pe.util.ParseResult;

public class Benchmark {
    private static final int REPETITIONS = 100;

    public static void main(String[] args) throws IOException {
        for (String file : TestSettings.sampleBinaries) {
            System.out.printf("%s:%n", file);
            run(TestSettings.basePath + file);
        }
        //run(TestSettings.basePath + "ctest_mangled_nt.exe");
    }

    public static void run(String path) throws IOException {
        CadesFileStream fstream = new CadesFileStream(new File(path));

        boolean isValid = true;
        long totalBeginMs = System.currentTimeMillis();
        for (int i = 0; i < REPETITIONS; ++i) {
            fstream.seek(0);
            ParseResult<PeImage> result = PeImage.read(fstream);
            if (result.isErr())
                isValid = false;
        }
        long totalEndMs = System.currentTimeMillis();

        if (!isValid)
            System.out.println("\t(Invalid image)");
        
        System.out.printf("\tTotal time: %d ms%n", totalEndMs - totalBeginMs);
        System.out.printf("\tAverage time: %d ms%n", (totalEndMs - totalBeginMs) / REPETITIONS);
    }
}
