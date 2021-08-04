package com.company;

import java.io.IOException;

/**
 * Reads memory like a stream. Uses long addressing to allow implementations with virtual addresses.
 */
public abstract class CadesStreamReader {
    protected long pos;

    long getPos() { return pos; }

    abstract int read() throws IOException;
    abstract void seek(long pos) throws IOException;
    abstract void seekEnd() throws IOException;
    abstract void seekStart() throws IOException;
}
