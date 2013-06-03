package org.thelq.se.dbimport.sources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.thelq.se.dbimport.Controller;

/**
 *
 * @author Leon
 */
@Slf4j
public class ArchiveDumpContainer implements DumpContainer {
	@Getter
	protected String type = "Archive";
	protected final File file;
	@Getter
	protected final SevenZFile file7;
	@Getter
	protected String name;
	@Getter
	protected List<ArchiveDumpEntry> entries = new ArrayList();

	public ArchiveDumpContainer(Controller controller, File file) throws FileNotFoundException, IOException {
		this.file = file;
		this.name = file.getName();
		this.file7 = new SevenZFile(file);

		SevenZArchiveEntry curEntry;
		while ((curEntry = file7.getNextEntry()) != null) {
			if(curEntry.isDirectory())
				continue;
			entries.add(new ArchiveDumpEntry(controller, file, curEntry));
		}
	}

	public String getLocation() {
		return file.getAbsolutePath();
	}
}
