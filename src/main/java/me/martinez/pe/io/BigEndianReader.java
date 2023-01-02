package me.martinez.pe.io;

import java.io.IOException;

public class BigEndianReader extends EndianReader {
    public BigEndianReader(CadesStreamReader stream) {
        super(stream);
    }

    public int readWord() throws IOException {
        return (stream.read() << 8) | stream.read();
    }

    public long readDword() throws IOException {
        return ((long) readWord() << 16) | (long) readWord();
    }

    public long readQword() throws IOException {
        return (readDword() << 32) | readDword();
    }
}
