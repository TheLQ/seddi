
package org.thelq.se.dbimport.sources;

import java.util.List;

/**
 *
 * @author Leon
 */
public interface DumpContainer {
	public List<? extends DumpEntry> getEntries();
	public String getLocation();
	public String getType();
}
