package org.thelq.stackexchange.dbimport.gui;

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
	public static DatabaseOption SELECTONE = new DatabaseOption().name("Select One");
	String name;
	String jdbcString;
	String driver;
	String dialect;

	@Override
	public String toString() {
		return name;
	}
}
