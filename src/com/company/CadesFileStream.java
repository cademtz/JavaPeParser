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
    public int read() throws IOException {
        ++pos;
        return stream.read() & 0xFF;
    }

    @Override
    public void seek(long pos) throws IOException {
        stream.seek( this.pos = pos );
    }

    @Override
    public void seekEnd() throws IOException {
        stream.seek( pos = stream.length() );
    }

    @Override
    public void seekStart() throws IOException {
        stream.seek( pos = 0 );
    }
}
