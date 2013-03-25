package org.thelq.se.dbimport.sources;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import lombok.Setter;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import org.slf4j.LoggerFactory;
import org.thelq.se.dbimport.Controller;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
public class ArchiveDumpEntry implements DumpEntry {
	protected final ArchiveDumpContainer container;
	protected final int itemId;
	protected PipedOutputStream pipedOutput;
	protected ExceptionPipedInputStream pipedInput;

	public ArchiveDumpEntry(ArchiveDumpContainer container, int itemId) {
		this.container = container;
		this.itemId = itemId;
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
		container.getController().getGeneralThreadPool().execute(new Runnable() {
			public void run() {
				try {
					container.getArchive7().extract(new int[]{itemId}, false, new OutputExtractCallback());
				} catch (SevenZipException ex) {
					IOException exception = new IOException("Cannot extract archive " + container.getLocation(), ex);
					pipedInput.setException(exception);
					LoggerFactory.getLogger(getClass()).error("Exception encountered during extraction", ex);
				}
			}
		});
		return pipedInput;
	}

	public DumpParser getParser() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public DatabaseWriter getDatabaseWriter() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void close() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getLocation() {
		try {
			return container.getLocation() + System.getProperty("file.separator")
					+ container.getArchive7().getProperty(itemId, PropID.PATH);
		} catch (SevenZipException ex) {
			throw new RuntimeException("Cannot get path", ex);
		}
	}

	public String getName() {
		try {
			return (String) container.getArchive7().getProperty(itemId, PropID.PATH);
		} catch (SevenZipException ex) {
			throw new RuntimeException("Cannot get path", ex);
		}
	}

	public long getSizeBytes() {
		try {
			return (Long) container.getArchive7().getProperty(itemId, PropID.SIZE);
		} catch (SevenZipException ex) {
			throw new RuntimeException("Cannot get path", ex);
		}
	}

	protected class OutputExtractCallback implements IArchiveExtractCallback {
		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			if (index != itemId && extractAskMode == ExtractAskMode.EXTRACT)
				throw new SevenZipException("Asked to extract index " + index + " but expected index " + itemId);
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
