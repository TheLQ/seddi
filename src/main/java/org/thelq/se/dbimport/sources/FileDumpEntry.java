package org.thelq.se.dbimport.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Data
public class FileDumpEntry implements DumpEntry {
	protected final File file;
	protected DumpParser parser;
	protected DatabaseWriter databaseWriter;
	protected FileInputStream inputStream;

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
		if(inputStream != null)
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
			parser.close();
			databaseWriter.close();
			inputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot fully close File", ex);
		}
	}

	public long getSizeBytes() {
		return file.length();
	}
}
