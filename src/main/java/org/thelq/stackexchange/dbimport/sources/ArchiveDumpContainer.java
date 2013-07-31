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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.thelq.stackexchange.dbimport.Controller;

/**
 *
 * @author Leon
 */
@Slf4j
public class ArchiveDumpContainer extends DumpContainer {
	@Getter
	protected String type = "Archive";
	protected final File file;
	@Getter
	protected final SevenZFile file7;
	@Getter
	protected String name;
	@Getter
	protected final ImmutableList<DumpEntry> entries;

	public ArchiveDumpContainer(Controller controller, File file) throws FileNotFoundException, IOException {
		this.file = file;
		this.name = file.getName();
		this.file7 = new SevenZFile(file);

		ImmutableList.Builder<DumpEntry> entriesBuilder = ImmutableList.builder();
		SevenZArchiveEntry curEntry;
		while ((curEntry = file7.getNextEntry()) != null) {
			if(curEntry.isDirectory())
				continue;
			entriesBuilder.add(new ArchiveDumpEntry(controller, file, curEntry));
 		}
		this.entries = entriesBuilder.build();
	}

	public String getLocation() {
		return file.getAbsolutePath();
	}
}
