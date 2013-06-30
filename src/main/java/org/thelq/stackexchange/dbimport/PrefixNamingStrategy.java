/**
 * Copyright (C) 2013 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Unified StackExchange Data Dump Importer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, softwar
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
