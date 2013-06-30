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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Leon
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileDumpEntry extends DumpEntry {
	protected final File file;
	protected FileInputStream inputStream;

	public FileDumpEntry(File file) {
		this.file = file;
	}

	@Override
	public String getLocation() {
		return file.getAbsolutePath();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public InputStream getInput() {
		if (inputStream != null)
			throw new RuntimeException("Already generated an input stream");
		try {
			return inputStream = new FileInputStream(file);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get input stream from file " + file.getAbsolutePath());
		}
	}

	@Override
	public void close() {
		try {
			inputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException("Cannot fully close File", ex);
		}
	}

	public long getSizeBytes() {
		return file.length();
	}
}
