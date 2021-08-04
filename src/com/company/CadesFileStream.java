package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CadesFileStream extends CadesStreamReader {
    RandomAccessFile stream;

    public CadesFileStream(File file) throws FileNotFoundException {
        stream = new RandomAccessFile(file, "r");
    }
    public CadesFileStream(String file) throws FileNotFoundException {
        stream = new RandomAccessFile(file, "r");
    }

    @Override
    int read() throws IOException {
        ++pos;
        return stream.read() & 0xFF;
    }

    @Override
    void seek(long pos) throws IOException {
        stream.seek(pos);
        this.pos = pos;
    }

    @Override
    void seekEnd() throws IOException {
        pos = stream.length() - 1;
        stream.seek(pos);
    }

    @Override
    void seekStart() throws IOException {
        pos = 0;
        stream.seek(pos);
    }
}
