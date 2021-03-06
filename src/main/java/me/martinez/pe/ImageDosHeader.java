package me.martinez.pe;

import me.martinez.pe.io.LittleEndianReader;

import java.io.IOException;

public class ImageDosHeader {
	public static final int IMAGE_DOS_SIGNATURE = 0x5A4D;
	public int magic;
	public int cblp;
	public int cp;
	public int crlc;
	public int cparhdr;
	public int minalloc;
	public int maxalloc;
	public int ss;
	public int sp;
	public int csum;
	public int ip;
	public int cs;
	public int lfarlc;
	public int ovno;
	public final int[] res = new int[4];
	public int oemid;
	public int oeminfo;
	public final int[] res2 = new int[10];
	public long lfanew;

	public static ImageDosHeader read(LittleEndianReader r) {
		ImageDosHeader hdr = new ImageDosHeader();
		try {
			hdr.magic = r.readWord();
			if (hdr.magic != IMAGE_DOS_SIGNATURE)
				return null; // Invalid header
			hdr.cblp = r.readWord();
			hdr.cp = r.readWord();
			hdr.crlc = r.readWord();
			hdr.cparhdr = r.readWord();
			hdr.minalloc = r.readWord();
			hdr.maxalloc = r.readWord();
			hdr.ss = r.readWord();
			hdr.sp = r.readWord();
			hdr.csum = r.readWord();
			hdr.ip = r.readWord();
			hdr.cs = r.readWord();
			hdr.lfarlc = r.readWord();
			hdr.ovno = r.readWord();
			for (int i = 0; i < hdr.res.length; ++i)
				hdr.res[i] = r.readWord();
			hdr.oemid = r.readWord();
			hdr.oeminfo = r.readWord();
			for (int i = 0; i < hdr.res2.length; ++i)
				hdr.res2[i] = r.readWord();
			hdr.lfanew = r.readDword();
		} catch (IOException e) {
		    // TODO: Error handling
			return null;
		}
		return hdr;
	}
}
