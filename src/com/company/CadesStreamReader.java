package com.company;

import java.io.IOException;

/**
 * Reads memory like a stream. Uses long addressing to allow implementations with virtual addresses.
 */
public abstract class CadesStreamReader {
    protected long pos;

    public long getPos() { return pos; }

    public abstract int read() throws IOException;
    public abstract void seek(long pos) throws IOException;
    public abstract void seekEnd() throws IOException;
    public abstract void seekStart() throws IOException;
}
