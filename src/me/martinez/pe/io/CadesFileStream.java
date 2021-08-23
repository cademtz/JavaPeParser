package me.martinez.pe.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CadesFileStream extends CadesStreamReader {
	private final RandomAccessFile stream;

	public CadesFileStream(File file) throws FileNotFoundException {
		stream = new RandomAccessFile(file, "r");
	}

	@Override
	public int read() throws IOException {
		incrementPos(1);
		return stream.read() & 0xFF;
	}

	@Override
	public void seek(long pos) throws IOException {
		setPos(pos);
		stream.seek(pos);
	}

	@Override
	public void seekEnd() throws IOException {
		setPos(stream.length());
		stream.seek(getPos());
	}

	@Override
	public void seekStart() throws IOException {
		setPos(0);
		stream.seek(0);
	}
}
