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
import lombok.Getter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thelq.stackexchange.dbimport.Controller;

/**
 *
 * @author Leon
 */
@Slf4j
public class ArchiveDumpEntry extends DumpEntry {
	protected final Controller controller;
	protected final int itemId;
	protected RandomAccessFile archiveRandomFile;
	protected ISevenZipInArchive archive7;
	protected File archiveFile;
	protected PipedOutputStream pipedOutput;
	protected ExceptionPipedInputStream pipedInput;
	@Getter
	protected String name;
	@Getter
	protected String location;
	@Getter
	protected long sizeBytes;

	public ArchiveDumpEntry(Controller controller, File archiveFile, int itemId) {
		this.controller = controller;
		this.itemId = itemId;
		try {
			this.archiveFile = archiveFile;
			archiveRandomFile = new RandomAccessFile(archiveFile, "r");
			archive7 = SevenZip.openInArchive(null, new RandomAccessFileInStream(archiveRandomFile));

			//Set properties that don't actually change, so might as well pre-fetch them
			this.name = (String) archive7.getProperty(itemId, PropID.PATH);
			this.location = archiveFile.getAbsolutePath() + System.getProperty("file.separator") + name;
			this.sizeBytes = (Long) archive7.getProperty(itemId, PropID.SIZE);
		} catch (Exception ex) {
			throw new RuntimeException("Cannot open archive", ex);
		}
	}

	public InputStream getInput() {
		if (pipedInput != null)
			throw new RuntimeException("Already generated an InputStream");
		try {
			pipedOutput = new PipedOutputStream();
			pipedInput = new ExceptionPipedInputStream(pipedOutput);
		} catch (IOException ex) {
			throw new RuntimeException("Cannot open Piped streams", ex);
		}
		controller.getGeneralThreadPool().execute(new Runnable() {
			protected final Logger log = LoggerFactory.getLogger(getClass());

			public void run() {
				try {
					archive7.extract(new int[]{itemId}, false, new OutputExtractCallback());
					log.debug("Done with extraction");
				} catch (Exception ex) {
					IOException exception = new IOException("Cannot extract archive " + archiveFile.getAbsolutePath(), ex);
					pipedInput.setException(exception);
					log.error("Exception encountered during extraction", ex);
				} finally {
					try {
						archive7.close();
						archiveRandomFile.close();
						pipedOutput.close();
						pipedInput.close();
						log.debug("Closed archive");
					} catch (Exception e) {
						log.error("Exception encountered during cosing", e);
					}
				}
			}
		});
		return pipedInput;
	}

	public void close() {
		//Everything is closed when extraction is finished
	}

	protected class OutputExtractCallback implements IArchiveExtractCallback {
		protected boolean skipFile = false;

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			if (index != itemId) {
				if (extractAskMode == ExtractAskMode.EXTRACT)
					throw new SevenZipException("Asked to extract index " + index + " but expected index " + itemId);
				skipFile = true;
				return null;
			}
			skipFile = false;
			return new ISequentialOutStream() {
				public int write(byte[] data) throws SevenZipException {
					try {
						pipedOutput.write(data);
						return data.length;
					} catch (IOException e) {
						throw new SevenZipException("Cannot write data to OutputStream", e);
					}
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			if (skipFile)
				return;
			if (extractOperationResult != ExtractOperationResult.OK)
				throw new SevenZipException("Extraction halted with " + extractOperationResult.name());
			try {
				pipedOutput.close();
			} catch (IOException ex) {
				throw new SevenZipException("Cannot close", ex);
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
		}

		public void setTotal(long total) throws SevenZipException {
		}
	}

	protected static class ExceptionPipedInputStream extends PipedInputStream {
		@Setter
		protected IOException exception;

		public ExceptionPipedInputStream(PipedOutputStream src) throws IOException {
			super(src);
		}

		protected void handleException() throws IOException {
			if (exception != null && in != -1) {
				close();
				throw exception;
			}
		}

		@Override
		public synchronized int read() throws IOException {
			handleException();
			return super.read();
		}

		@Override
		public synchronized int read(byte[] b, int off, int len) throws IOException {
			handleException();
			return super.read(b, off, len);
		}
	}
}
