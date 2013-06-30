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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Leon
 */
@Data
@Slf4j
public class FolderDumpContainer extends DumpContainer {
	protected final File folder;
	protected final ImmutableList<DumpEntry> entries;
	protected String type = "Folder";
	protected String name;
	

	public FolderDumpContainer(File folder) {
		if (!folder.isDirectory())
			throw new IllegalArgumentException("File " + folder.getAbsolutePath() + " is not a folder");
		this.folder = folder;
		this.name = folder.getName();

		//Add all the files
		ImmutableList.Builder<DumpEntry> entriesBuilder = ImmutableList.builder();
		for (File curFile : folder.listFiles()) {
			if (!curFile.getName().endsWith(".xml")) {
				log.info("Ignoring non-XML file " + curFile.getAbsolutePath());
				continue;
			}
			entriesBuilder.add(new FileDumpEntry(curFile));
		}
		this.entries = entriesBuilder.build();
	}
	
	@Override
	public String getLocation() {
		return folder.getAbsolutePath();
	}
}
