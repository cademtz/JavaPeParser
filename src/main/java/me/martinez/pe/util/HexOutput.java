package me.martinez.pe.util;

public class HexOutput {
    private static String bitsToString(long value, int bitCount) {
        StringBuilder str = new StringBuilder();
        for (int bit = bitCount - 4; bit >= 0; bit -= 4) {
            char nibble = (char) ((value >> bit) & 0xF);
            if (nibble >= 0xA)
                str.append((char) ('A' + (nibble - 0xA)));
            else
                str.append((char) ('0' + nibble));
        }

        return str.toString();
    }

    public static String qwordToString(long qword) {
        return bitsToString(qword, 64);
    }

    public static String dwordToString(long dword) {
        return bitsToString(dword, 32);
    }

    public static String wordToString(int word) {
        return bitsToString(word, 16);
    }

    public static String byteToString(byte byte_) {
        return bitsToString(byte_, 8);
    }
}
