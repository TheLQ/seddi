
package org.thelq.se.dbimport.sources;

import java.io.InputStream;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
public interface DumpEntry {
	public String getLocation();
	public InputStream getInput();
	public DumpParser getParser();
	public DatabaseWriter getDatabaseWriter();
	public void close();
	public long getSizeBytes();
}
