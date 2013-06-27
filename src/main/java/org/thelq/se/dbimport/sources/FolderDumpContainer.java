package org.thelq.se.dbimport.sources;

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
