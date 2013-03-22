package org.thelq.se.dbimport.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
@Getter
public enum DumpContainerColumn {
	NAME("Name", 0),
	SIZE("File Size", 1),
	PARSER("Parser Status", 2),
	DATABASE("Database Status", 3);
	protected final String name;
	protected final int id;

	public static DumpContainerColumn getById(int id) {
		for (DumpContainerColumn curColumn : values())
			if (curColumn.getId() == id)
				return curColumn;
		throw new IllegalArgumentException("Column with ID " + id + " does not exist");
	}
}
