package me.martinez.pe.io;

import java.io.IOException;

public class LittleEndianReader extends EndianReader {
    public LittleEndianReader(CadesStreamReader stream) {
        super(stream);
    }

    public int readWord() throws IOException {
        return stream.read() | (stream.read() << 8);
    }

    public long readDword() throws IOException {
        return (long) readWord() | ((long) readWord() << 16);
    }

    public long readQword() throws IOException {
        return readDword() | (readDword() << 32);
    }
}
