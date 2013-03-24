package org.thelq.se.dbimport.gui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author Leon
 */
@Accessors(fluent = true)
@Setter
@Getter
public class DatabaseOption {
	public static DatabaseOption CUSTOM = new DatabaseOption().name("Custom");
	String name;
	String jdbcString;
	String driver;
	String dialect;

	@Override
	public String toString() {
		return name;
	}
}
