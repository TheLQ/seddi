/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.stackexchange.dbimport.sources;

import java.io.InputStream;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import lombok.Data;
import org.thelq.stackexchange.dbimport.DatabaseWriter;
import org.thelq.stackexchange.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Data
public abstract class DumpEntry {
	protected DumpParser parser;
	protected DatabaseWriter databaseWriter;
	protected JLabel guiName;
	protected JLabel guiSize;
	protected JLabel guiLog;
	protected JSeparator guiSeparator;

	public abstract String getLocation();

	public abstract String getName();

	public abstract InputStream getInput();

	public abstract void close();

	public abstract long getSizeBytes();
}
