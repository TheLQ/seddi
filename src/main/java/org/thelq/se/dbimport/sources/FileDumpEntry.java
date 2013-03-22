package org.thelq.se.dbimport.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	@Override
	public String getLocation() {
		return file.getAbsolutePath();
	}

	@Override
	public InputStream getInput() {
		try {
			return new FileInputStream(file);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get input stream from file " + file.getAbsolutePath());
		}
	}

	@Override
	public void close() {
		parser.close();
		databaseWriter.close();
	}
}
