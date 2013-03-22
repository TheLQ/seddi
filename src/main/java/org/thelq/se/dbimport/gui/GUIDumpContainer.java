package org.thelq.se.dbimport.gui;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.JTextField;
import lombok.Data;
import org.thelq.se.dbimport.sources.DumpContainer;
import org.thelq.se.dbimport.sources.DumpEntry;

/**
 *
 * @author Leon
 */
@Data
public class GUIDumpContainer {
	protected final DumpContainer dumpContainer;
	protected JTable table;
	protected JTextField tablePrefix;
	protected Map<DumpEntry, String> logParserMap = new HashMap();
	protected Map<DumpEntry, String> logDatabaseMap = new HashMap();
	
	public void setLogParser(DumpEntry dumpEntry, String logParser) {
		logParserMap.put(dumpEntry, logParser);
		table.setValueAt(logParser, dumpContainer.getEntries().indexOf(dumpEntry), DumpContainerColumn.PARSER.getId());
	}
	
	public void setLogDatabase(DumpEntry dumpEntry, String logDatabase) {
		logDatabaseMap.put(dumpEntry, logDatabase);
		table.setValueAt(logDatabase, dumpContainer.getEntries().indexOf(dumpEntry), DumpContainerColumn.DATABASE.getId());
	}
	
	public DumpEntry getDumpEntryById(int id) {
		return dumpContainer.getEntries().get(id);
	}
}
