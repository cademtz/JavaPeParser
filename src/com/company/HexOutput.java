package com.company;

public class HexOutput {

    private static String bitsToString(long value, int bitCount)
    {
        String str = "";

        for (int bit = bitCount - 4; bit >= 0; bit -= 4)
        {
            char nibble = (char)((value >> bit) & 0xF);
            if (nibble >= 0xA)
                str += (char)('A' + (nibble - 0xA));
            else
                str += (char)('0' + nibble);
        }

        return str;
    }

    public static String qwordToString(long qword) { return bitsToString(qword, 64); }
    public static String dwordToString(long dword) { return bitsToString(dword, 32); }
    public static String wordToString(int word) { return bitsToString(word, 16); }
    public static String byteToString(byte bite) { return bitsToString(bite, 8); }
}
