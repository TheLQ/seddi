package org.thelq.stackexchange.dbimport;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Add the specified prefix to all tables
 * @author Leon
 */
@RequiredArgsConstructor
public class PrefixNamingStrategy extends ImprovedNamingStrategy {
	protected final String prefix;

	@Override
	public String classToTableName(String className) {
		return prefix + super.classToTableName(className);
	}

	@Override
	public String tableName(String tableName) {
		return prefix + super.tableName(tableName);
	}
}
