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
package org.thelq.stackexchange.dbimport.sources;

import com.google.common.collect.ImmutableList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import lombok.Data;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * A data class containing everything needed for importing a dumpContainer
 * @author Leon
 */
@Data
public abstract class DumpContainer {
	protected SessionFactory sessionFactory;
	protected ServiceRegistry serviceRegistry;
	protected Configuration hibernateConfiguration;
	protected final Object hibernateCreateLock = new Object();
	protected JLabel guiHeader;
	protected JTextField guiTablePrefix;
	protected String tablePrefix;

	public abstract ImmutableList<DumpEntry> getEntries();
	
	public abstract String getLocation();

	public abstract String getName();

	public abstract String getType();
}
