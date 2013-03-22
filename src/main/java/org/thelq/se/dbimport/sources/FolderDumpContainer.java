package org.thelq.se.dbimport.sources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Leon
 */
@Data
@Slf4j
public class FolderDumpContainer implements DumpContainer {
	protected final File folder;
	protected String tablePrefix;
	protected String type = "Folder";
	protected List<FileDumpEntry> entries = new ArrayList();

	public FolderDumpContainer(File folder) {
		if (!folder.isDirectory())
			throw new IllegalArgumentException("File " + folder.getAbsolutePath() + " is not a folder");
		this.folder = folder;

		//Add all the files
		for (File curFile : folder.listFiles()) {
			if (!curFile.getName().endsWith(".xml"))
				log.info("Ignoring non-XML file " + curFile.getAbsolutePath());
			entries.add(new FileDumpEntry(curFile));
		}
	}
	
	@Override
	public String getLocation() {
		return folder.getAbsolutePath();
	}
}
