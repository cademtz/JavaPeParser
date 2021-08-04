package com.company;

import java.io.IOException;

public class CadesBufferStream extends CadesStreamReader {
    byte[] bytes;
    int offset;
    int count;

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
    int read() throws IOException {
        if (pos < 0 || pos >= count)
            throw new IOException();
        return bytes[(int)(pos++ + offset)] & 0xFF;
    }

    @Override
    void seek(long pos) throws IOException {
        if (pos < 0 || pos >= count)
            throw new IOException();
        this.pos = pos;
    }

    @Override
    void seekEnd() { pos = count - 1; }

    @Override
    void seekStart() { pos = 0; }
}
