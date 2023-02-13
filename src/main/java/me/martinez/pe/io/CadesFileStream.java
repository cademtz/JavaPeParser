package me.martinez.pe.io;

import java.io.*;

public class CadesFileStream extends CadesStreamReader {
    private final RandomAccessFile file;
    private final byte cache[] = new byte[256]; // This is a magic number based on benchmarks
    private long cachePos = 0;
    private int cacheLen = 0;

    public CadesFileStream(File file) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "r");
    }

    @Override
    public int read() throws IOException {
        if (!isCacheHit(getPos(), 1))
            fillCache();
        return readCache();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        file.seek(getPos());
        int read = file.read(buffer, offset, length);
        if (read < 0)
            throw new EOFException();
        incrementPos(read);
        return read;
    }

    @Override
    public void seek(long pos) throws IOException {
        setPos(pos);
        file.seek(pos);
    }

    @Override
    public void seekEnd() throws IOException {
        setPos(file.length());
        file.seek(getPos());
    }

    @Override
    public void seekStart() throws IOException {
        setPos(0);
        file.seek(0);
    }

    private boolean isCacheHit(long pos, int len) {
        return pos >= cachePos && pos + len <= cachePos + cacheLen;
    }

    private int readCache() {
        int value = (int)cache[(int)posToCacheIndex()];
        incrementPos(1);
        return value & 0xFF;
    }

    private long posToCacheIndex() {
        return getPos() - cachePos;
    }

    private int readCache(byte[] buffer, int offset, int length) {
        // FIXME: Reads bytes as integers with sign extension left on
        long cacheIndex = posToCacheIndex();
        long remainingCache = cacheLen - cacheIndex;
        if (remainingCache == -1)
            return 0;
        long readLen = Long.min(length, remainingCache);
        
        System.arraycopy(cache, (int)cacheIndex, buffer, offset, (int)readLen);
        incrementPos(readLen);
        return (int)readLen;
    }

    private void fillCache() throws IOException {
        file.seek(getPos());
        cachePos = getPos();
        cacheLen = read(cache);
        setPos(cachePos);
    }
}
