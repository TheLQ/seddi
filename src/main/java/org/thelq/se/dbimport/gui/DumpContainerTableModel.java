package org.thelq.se.dbimport.gui;

import javax.swing.table.AbstractTableModel;
import lombok.RequiredArgsConstructor;
import org.thelq.se.dbimport.ImportContainer;
import org.thelq.se.dbimport.sources.DumpEntry;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
public class DumpContainerTableModel extends AbstractTableModel {
	protected final ImportContainer importContainer;

	@Override
	public String getColumnName(int col) {
		return DumpContainerColumn.getById(col).getName();
	}

	public int getColumnCount() {
		return DumpContainerColumn.values().length;
	}

	public int getRowCount() {
		return importContainer.getDumpContainer().getEntries().size();
	}

	public Object getValueAt(int row, int col) {
		DumpEntry entry = importContainer.getDumpContainer().getEntries().get(row);
		DumpContainerColumn column = DumpContainerColumn.getById(col);
		if (column == DumpContainerColumn.NAME)
			return entry.getName();
		else if (column == DumpContainerColumn.SIZE)
			return entry.getSizeBytes();
		else if (column == DumpContainerColumn.PARSER)
			//return importContainer.getLogParserMap().get(entry);
			return "";
		else if (column == DumpContainerColumn.DATABASE)
			//return importContainer.getLogDatabaseMap().get(entry);
			return "";
		else
			throw new IllegalArgumentException("Unknown column " + col);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}
}
