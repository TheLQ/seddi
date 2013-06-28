package org.thelq.stackexchange.dbimport.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Leon
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileDumpEntry extends DumpEntry {
	protected final File file;
	protected FileInputStream inputStream;

	public FileDumpEntry(File file) {
		this.file = file;
	}

	@Override
	public String getLocation() {
		return file.getAbsolutePath();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public InputStream getInput() {
		if (inputStream != null)
			throw new RuntimeException("Already generated an input stream");
		try {
			return inputStream = new FileInputStream(file);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get input stream from file " + file.getAbsolutePath());
		}
	}

	@Override
	public void close() {
		try {
			inputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot fully close File", ex);
		}
	}

	public long getSizeBytes() {
		return file.length();
	}
}
