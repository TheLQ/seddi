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

import java.io.InputStream;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import lombok.Data;
import org.thelq.stackexchange.dbimport.DatabaseWriter;
import org.thelq.stackexchange.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Data
public abstract class DumpEntry {
	protected DumpParser parser;
	protected DatabaseWriter databaseWriter;
	protected JLabel guiName;
	protected JLabel guiSize;
	protected JLabel guiLog;
	protected JSeparator guiSeparator;

	public abstract String getLocation();

	public abstract String getName();

	public abstract InputStream getInput();

	public abstract void close();

	public abstract long getSizeBytes();
}
