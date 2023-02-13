package me.martinez.pe.io;

import java.io.IOException;

/**
 * Reads memory like a stream. Uses long addressing to allow implementations with virtual addresses.
 *
 * @author Cade Martinez
 */
public abstract class CadesStreamReader {
    private long pos;

    public long getPos() {
        return pos;
    }

    /**
     * Reads the next byte in the stream
     * @return The next byte in the stream
     * @throws IOException Nothing was read
     */
    public abstract int read() throws IOException;

    /**
     * @param buffer Array to store the read bytes
     * @return Number of bytes actually read
     * @throws IOException
     */
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    /**
     * 
     * @param buffer Array to store the read bytes 
     * @param offset Index of the first byte to be stored
     * @param length Maximum number of bytes to read
     * @return Number of bytes actually read
     * @throws IOException
     */
    public abstract int read(byte[] buffer, int offset, int length) throws IOException;

    public abstract void seek(long pos) throws IOException;

    public abstract void seekEnd() throws IOException;

    public abstract void seekStart() throws IOException;

    protected void setPos(long pos) {
        this.pos = pos;
    }

    /**
     * Offsets {@link #pos}
     * @return Last value of {@link #pos}
     */
    protected long incrementPos(long offset) {
        return (pos += offset) - offset;
    }
}
