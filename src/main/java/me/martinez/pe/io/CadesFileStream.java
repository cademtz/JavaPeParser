package me.martinez.pe.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CadesFileStream extends CadesStreamReader {
	private final RandomAccessFile file;

	public CadesFileStream(File file) throws FileNotFoundException {
		this.file = new RandomAccessFile(file, "r");
	}

	@Override
	public int read() throws IOException {
		incrementPos(1);
		return file.read() & 0xFF;
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
}
