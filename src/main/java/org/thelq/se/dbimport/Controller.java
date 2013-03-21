package org.thelq.se.dbimport;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thelq.se.dbimport.gui.GUI;

/**
 *
 * @author Leon
 */
@Slf4j
public class Controller {
	protected GUI gui;
	@Getter
	protected LinkedHashMap<File, List<DumpParser>> parsers = new LinkedHashMap();
	@Getter
	protected ExecutorService generalThreadPool = Executors.newCachedThreadPool();

	public Controller() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui = new GUI(Controller.this);
			}
		});
	}

	public void addFolder(final File folder) {
		if (!folder.isDirectory())
			throw new IllegalArgumentException("File " + folder.getAbsolutePath() + " is not a folder");
		if (parsers.containsKey(folder))
			throw new IllegalArgumentException("Already added folder " + folder.getAbsolutePath());
		File[] folderFiles = folder.listFiles();
		ArrayList<DumpParser> parserList = new ArrayList(folderFiles.length);
		for (File curFile : folderFiles) {
			if (!curFile.getName().endsWith(".xml"))
				log.info("Ignoring non-XML file " + curFile.getAbsolutePath());
			parserList.add(new DumpParser(curFile));
		}
		parsers.put(folder, parserList);
	}

	public static void main(String[] args) {
		new Controller();
	}
}
