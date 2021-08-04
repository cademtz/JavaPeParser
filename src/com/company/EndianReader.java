package com.company;

import java.io.IOException;
import java.io.InputStream;

public abstract class EndianReader {
    CadesStreamReader stream;

    public EndianReader(CadesStreamReader Stream) { stream = Stream; }

    public CadesStreamReader getStream() { return stream; }

    public int readByte() throws IOException {
        return stream.read() & 0xFF; // Prevent Java's sign-extension (0xAB to 32 bits becomes 0xFFFFFFAB)
    }

    /**
     * Reads a null-terminated string up to maxChars before it is considered invalid
     * @param maxChars Maximum number of non-null chars before string is considered invalid
     * @return The resulting string, or null
     * @throws IOException
     */
    public String readNullTerminatedString(int maxChars) throws IOException {
        String str = new String();

        int nextChar;
        for (int i = 0; i < maxChars && (nextChar = stream.read()) != 0; ++i)
            str += (char)nextChar;
        return str;
    }

    public String readString(int length) throws IOException {
        String str = new String();
        for (int i = 0; i < length; ++i)
            str += (char) stream.read();
        return str;
    }

    abstract int readWord() throws IOException;
    abstract long readDword() throws IOException;
    abstract long readQword() throws IOException;

    long readNative(boolean is64bit) throws IOException {
        return is64bit ? readQword() : readDword();
    }
}
