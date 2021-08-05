package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public abstract class EndianReader {
    private static final int MAX_DEFAULT = ~(1 << 31) - 1;

    protected CadesStreamReader stream;

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
        int nextChar;
        int i = 0;
        String str = new String();

        if (maxChars < 0)
            maxChars = MAX_DEFAULT;

        for (; i <= maxChars && (nextChar = stream.read()) != 0; ++i)
            str += (char)nextChar;

        if (i > maxChars)
            return null;

        return str;
    }

    public String readString(int length) throws IOException {
        String str = new String();
        for (int i = 0; i < length; ++i)
            str += (char)stream.read();
        return str;
    }

    /**
     * Reads a terminated (usually null-terminated) array of values in to an ArrayList
     * @param bits Size of one array element in bits. Rounded up to the nearest int type (byte, word, dword, qword)
     * @param term Value signifying the end of an array. Typically 0 (null-terminated)
     * @param maxLength Maxmium number of values before the array is considered invalid
     * @return The resulting ArrayList, or null
     * @throws IOException
     */
    public ArrayList<Long> readTerminatedValues(int bits, long term, int maxLength) throws IOException
    {
        int i = 0;
        ArrayList<Long> list = new ArrayList<>();

        if (maxLength < 0)
            maxLength = MAX_DEFAULT;

        for (; i <= maxLength; ++i)
        {
            long val;

            if (bits < 16) val = readByte();
            else if (bits < 32) val = readWord();
            else if (bits < 64) val = readDword();
            else val = readQword();

            if (val == term)
                break;

            list.add(val);
        }

        if (i > maxLength)
            return null;
        return list;
    }

    /**
     * Reads an array of values
     * @param bits Size of one array element in bits. Rounded up to the nearest int type (byte, word, dword, qword)
     * @param count Number of values to read
     * @return The resulting array, or null
     * @throws IOException
     */
    public long[] readValues(int bits, int count) throws IOException
    {
        long[] arr = new long[count];

        for (int i = 0; i < count; ++i)
        {
            long val;

            if (bits < 16) val = readByte();
            else if (bits < 32) val = readWord();
            else if (bits < 64) val = readDword();
            else val = readQword();

            arr[i] = val;
        }

        return arr;
    }

    public abstract int readWord() throws IOException;
    public abstract long readDword() throws IOException;
    public abstract long readQword() throws IOException;

    public long readNative(boolean is64bit) throws IOException {
        return is64bit ? readQword() : readDword();
    }
}
