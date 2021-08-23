package com.company.io;

import java.io.IOException;

/**
 * Reads memory like a stream. Uses long addressing to allow implementations with virtual addresses.
 *
 * @author Cade Martinez
 */
public abstract class CadesStreamReader {
	private long pos;

	public void setPos(long pos) {
		this.pos = pos;
	}

	public long incrementPos(long offset) {
		return (pos += offset);
	}

	public long getPos() {
		return pos;
	}

	public abstract int read() throws IOException;

	public abstract void seek(long pos) throws IOException;

	public abstract void seekEnd() throws IOException;

	public abstract void seekStart() throws IOException;
}
