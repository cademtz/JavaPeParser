package com.company;

import java.io.IOException;

public class CadesBufferStream extends CadesStreamReader {
    private byte[] bytes;
    private int offset;
    private int count;

    public CadesBufferStream(byte[] Bytes)
    {
        bytes = Bytes;
        count = bytes.length;
    }

    public CadesBufferStream(byte[] Bytes, int Offset)
    {
        bytes = Bytes;
        offset = Offset;
        count = bytes.length - offset;
    }

    public CadesBufferStream(byte[] Bytes, int Offset, int Count)
    {
        bytes = Bytes;
        offset = Offset;
        count = Count;
    }

    @Override
    public int read() throws IOException {
        if (pos < 0 || pos >= count)
            throw new IOException();
        return bytes[(int)(pos++ + offset)] & 0xFF;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos < 0 || pos > count)
            throw new IOException();
        this.pos = pos;
    }

    @Override
    public void seekEnd() { pos = count; }

    @Override
    public void seekStart() { pos = 0; }
}
