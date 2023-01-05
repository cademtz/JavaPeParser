package me.martinez.pe.headers;

import me.martinez.pe.io.LittleEndianReader;
import me.martinez.pe.util.ParseResult;

import java.io.IOException;

public class ImageDataDirectory {
    public final long virtualAddress;
    public final long size;

    private ImageDataDirectory(long virtualAddress, long size) {
        this.virtualAddress = virtualAddress;
        this.size = size;
    }

    public static ParseResult<ImageDataDirectory> read(LittleEndianReader r) {
        try {
            long virtualAddress = r.readDword();
            long size = r.readDword();
            return ParseResult.ok(new ImageDataDirectory(virtualAddress, size));
        } catch (IOException e) {
            return ParseResult.err("IOException, cannot read ImageDataDirectory", e);
        }
    }
}
