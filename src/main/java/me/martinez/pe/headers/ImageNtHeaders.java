package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseError;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageNtHeaders {
    public static final long IMAGE_NT_SIGNATURE = 0x00004550;
    public long signature;
    public ImageFileHeader fileHeader;
    public ImageOptionalHeader optionalHeader;

    public static ParseResult<ImageNtHeaders> read(LittleEndianReader r) {
        ImageNtHeaders nt = new ImageNtHeaders();
        try {
            nt.signature = r.readDword();
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read NT signature", e);
        }
        if (nt.signature != IMAGE_NT_SIGNATURE)
            return ParseResult.err("Invalid NT header signature");

        ParseError err = ImageFileHeader.read(r).ifOk(hdr -> nt.fileHeader = hdr).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        err = ImageOptionalHeader.read(r).ifOk(hdr -> nt.optionalHeader = hdr).getErrOrDefault(null);
        if (err != null)
            return ParseResult.err(err);

        return ParseResult.ok(nt);
    }

    public boolean is64bit() {
        return optionalHeader.is64bit();
    }

    public ImageDataDirectory getDataDirectory(int index) {
        return optionalHeader.dataDirectory[index];
    }
}
