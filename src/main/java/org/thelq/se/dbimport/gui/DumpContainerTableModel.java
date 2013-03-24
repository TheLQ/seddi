package org.thelq.se.dbimport.gui;

import javax.swing.table.AbstractTableModel;
import lombok.RequiredArgsConstructor;
import org.thelq.se.dbimport.sources.DumpEntry;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
public class DumpContainerTableModel extends AbstractTableModel {
	protected final GUIDumpContainer guiDumpContainer;

	public String getColumnName(int col) {
		return DumpContainerColumn.getById(col).getName();
	}

	public int getColumnCount() {
		return DumpContainerColumn.values().length;
	}

	public int getRowCount() {
		return guiDumpContainer.getDumpContainer().getEntries().size();
	}

	public Object getValueAt(int row, int col) {
		DumpEntry entry = guiDumpContainer.getDumpEntryById(row);
		DumpContainerColumn column = DumpContainerColumn.getById(col);
		if (column == DumpContainerColumn.NAME)
			return entry.getName();
		else if (column == DumpContainerColumn.SIZE)
			return entry.getSizeBytes();
		else if (column == DumpContainerColumn.PARSER)
			return guiDumpContainer.getLogParserMap().get(entry);
		else if (column == DumpContainerColumn.DATABASE)
			return guiDumpContainer.getLogDatabaseMap().get(entry);
		else
			throw new IllegalArgumentException("Unknown column " + col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
}
