package org.thelq.se.dbimport.sources;

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
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.slf4j.LoggerFactory;
import org.thelq.se.dbimport.Controller;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Slf4j
public class ArchiveDumpEntry implements DumpEntry {
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
		this.location = file.getAbsolutePath() + name;
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
