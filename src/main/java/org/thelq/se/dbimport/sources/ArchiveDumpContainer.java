package org.thelq.se.dbimport.sources;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.thelq.se.dbimport.Controller;

/**
 *
 * @author Leon
 */
@Slf4j
public class ArchiveDumpContainer extends DumpContainer {
	@Getter
	protected String type = "Archive";
	protected final File archiveFile;
	@Getter
	protected String name;
	@Getter
	protected final ImmutableList<DumpEntry> entries;

	public ArchiveDumpContainer(Controller controller, File archiveFile) throws FileNotFoundException, SevenZipException {
		this.archiveFile = archiveFile;
		this.name = archiveFile.getName();

		try {
			@Cleanup
			RandomAccessFile archiveRandomFile = new RandomAccessFile(archiveFile, "r");
			@Cleanup
			ISevenZipInArchive archive7 = SevenZip.openInArchive(null, new RandomAccessFileInStream(archiveRandomFile));
			ImmutableList.Builder<DumpEntry> entriesBuilder = ImmutableList.builder();
			for (int i = 0; i < archive7.getNumberOfItems(); i++)
				if (!((Boolean) archive7.getProperty(i, PropID.IS_FOLDER)).booleanValue())
					entriesBuilder.add(new ArchiveDumpEntry(controller, archiveFile, i));
			this.entries = entriesBuilder.build();
		} catch (Exception e) {
			throw new RuntimeException("Could not iterate archive " + archiveFile.getAbsolutePath(), e);
		}
	}

	public String getLocation() {
		return archiveFile.getAbsolutePath();
	}
}
