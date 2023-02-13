package me.martinez.pe.io;

import me.martinez.pe.PeImage;
import me.martinez.pe.headers.ImageSectionHeader;
import me.martinez.pe.util.ParseResult;
import me.martinez.pe.util.VirtualAddressException;

import java.io.IOException;

/**
 * Read and navigate the data in a PE file by using virtual addresses
 */
public class CadesVirtualMemStream extends CadesStreamReader {
    private final PeImage pe;
    private final CadesStreamReader fdata;

    /**
     * @param fdata Input stream where {@link #pe} file data starts at position 0
     */
    public CadesVirtualMemStream(PeImage pe, CadesStreamReader fdata) {
        this.pe = pe;
        this.fdata = fdata;
        setPos(pe.ntHeaders.optionalHeader.imageBase);
    }

    @Override
    public int read() throws IOException {
        // FIXME: Be careful to not read past the available segment
        incrementPos(1);
        return fdata.read() & 0xFF;
    }
    
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int readLen = fdata.read(buffer, offset, length);
        incrementPos(readLen);
        return readLen;
    }

    @Override
    public void seek(long pos) throws IOException {
        long fileAddress;
        try {
            fileAddress = pe.getFileAddress(pos);
        } catch (VirtualAddressException e) {
            throw new IOException(e);
        }
        setPos(pos);
        fdata.seek(fileAddress);
    }

    @Override
    public void seekEnd() throws IOException {
        for (ParseResult<ImageSectionHeader> parsedSec : pe.sectionHeaders) {
            if (parsedSec.isErr())
                continue;

            ImageSectionHeader sec = parsedSec.getOk();
            long vaddr = pe.ntHeaders.optionalHeader.imageBase + sec.virtualAddress + sec.getVirtualSize();
            if (vaddr > getPos())
                setPos(vaddr);
        }

        seek(getPos()); // Use existing seek() to find real address and safety check
    }

    @Override
    public void seekStart() throws IOException {
        seek(pe.ntHeaders.optionalHeader.imageBase);
    }
}
