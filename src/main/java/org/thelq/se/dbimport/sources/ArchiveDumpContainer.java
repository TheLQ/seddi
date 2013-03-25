package org.thelq.se.dbimport.sources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
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
public class ArchiveDumpContainer implements DumpContainer {
	@Getter
	protected String type = "Archive";
	protected final File archiveFile;
	@Getter
	@Setter
	protected String tablePrefix;
	protected RandomAccessFile archiveRandomFile;
	@Getter
	protected ISevenZipInArchive archive7;
	@Getter
	protected List<ArchiveDumpEntry> entries = new ArrayList();
	@Getter
	protected final Controller controller;

	public ArchiveDumpContainer(Controller controller, File archiveFile) throws FileNotFoundException, SevenZipException {
		this.archiveFile = archiveFile;
		this.controller = controller;

		archiveRandomFile = new RandomAccessFile(archiveFile, "r");
		archive7 = SevenZip.openInArchive(null, new RandomAccessFileInStream(archiveRandomFile));

		for (int i = 0; i < archive7.getNumberOfItems(); i++)
			if (!((Boolean) archive7.getProperty(i, PropID.IS_FOLDER)).booleanValue())
				entries.add(new ArchiveDumpEntry(this, i));
	}

	public String getLocation() {
		return archiveFile.getAbsolutePath();
	}
}
