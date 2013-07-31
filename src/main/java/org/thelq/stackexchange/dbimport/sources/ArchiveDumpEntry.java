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
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thelq.stackexchange.dbimport.Controller;

/**
 *
 * @author Leon
 */
@RequiredArgsConstructor
@Slf4j
public class ArchiveDumpEntry extends DumpEntry {
	protected final Controller controller;
	protected final File file;
	protected final SevenZArchiveEntry fileEntry;
	@Getter
	protected final String name;
	@Getter
	protected final String location;
	@Getter
	protected final long sizeBytes;
	protected InputStream input;

	public ArchiveDumpEntry(Controller controller, File file, SevenZArchiveEntry fileEntry) {
		this.controller = controller;
		this.file = file;
		this.fileEntry = fileEntry;
		this.name = fileEntry.getName();
		this.location = file.getAbsolutePath() + SystemUtils.FILE_SEPARATOR + name;
		this.sizeBytes = fileEntry.getSize();
	}

	public InputStream getInput() {
		if (input != null)
			throw new RuntimeException("Already generated an InputStream");
		
		try {
			final SevenZFile file7 = new SevenZFile(file);
			//Advance archive until we find the correct ArchiveEntry
			SevenZArchiveEntry curEntry;
			while((curEntry = file7.getNextEntry()) != null) {
				if(!curEntry.getName().equals(name))
					continue;
				//Found, return a wrapped InputStream
				return new InputStream() {
					@Override
					public int read() throws IOException {
						return file7.read();
					}
				};
			}
		} catch (IOException ex) {
			throw new RuntimeException("Cannot open archive entry", ex);
		}
		//Didn't find anything
		throw new RuntimeException("Could not find file " + name + " in archive " + file.getAbsolutePath());
	}

	public void close() {
	}
}
