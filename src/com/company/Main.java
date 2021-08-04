package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        /*byte bytes[] = { 0x12, 0x34, (byte) 0xAB, (byte) 0xCD};

        EndianReader r = new BigEndianReader(new BufferReader(bytes));
        System.out.println("Sample Dword");
        System.out.println(HexOutput.dwordToString(r.readDword()));*/

        CadesFileStream file = new CadesFileStream("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
        LittleEndianReader r = new LittleEndianReader(file);
        /*ImageDosHeader dos = ImageDosHeader.read(r);

        if (dos == null)
        {
            System.out.println("Couldn't parse DOS header");
            return;
        }

        System.out.println("lfanew: " + HexOutput.dwordToString(dos.lfanew));

        // Parse NT header

        r.getStream().seek(dos.lfanew);
        ImageNtHeaders nt = ImageNtHeaders.read(r);*/

        ImagePeHeaders pe = ImagePeHeaders.read(r);

        System.out.println("I survived!!");
    }
}
