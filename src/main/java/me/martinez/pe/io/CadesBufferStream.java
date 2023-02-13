package me.martinez.pe.io;

import java.io.EOFException;
import java.io.IOException;

public class CadesBufferStream extends CadesStreamReader {
    private final byte[] bytes;
    private final int count;
    private final int offset;

    public CadesBufferStream(byte[] bytes) {
        this(bytes, 0);
    }

    public CadesBufferStream(byte[] bytes, int offset) {
        this(bytes, offset, bytes.length - offset);
    }

    public CadesBufferStream(byte[] bytes, int offset, int count) {
        this.bytes = bytes;
        this.offset = offset;
        this.count = count;
    }

    @Override
    public int read() throws IOException {
        if (getPos() < 0 || getPos() >= count)
            throw new IOException();
        return bytes[(int) (incrementPos(1) + offset)] & 0xFF;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int remaining = bytes.length - (int)getPos();
        if (remaining <= 0)
            throw new EOFException();
            
        int readLen = Integer.min(length, remaining);
        System.arraycopy(bytes, (int)getPos(), buffer, offset, readLen);
        incrementPos(readLen);
        return readLen;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos < 0 || pos > count)
            throw new IOException();
        setPos(pos);
    }

    @Override
    public void seekEnd() {
        setPos(count);
    }

    @Override
    public void seekStart() {
        setPos(0);
    }
}
