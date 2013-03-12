package org.thelq.se.dbimport;

import org.thelq.se.dbimport.gui.GUI;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Leon
 */
@Slf4j
public class Controller {
	protected GUI gui;
	@Getter
	protected LinkedHashMap<DumpParser, DatabaseWriter> parsers = new LinkedHashMap();
	@Getter
	protected ExecutorService generalThreadPool = Executors.newCachedThreadPool();

	public Controller() {
		gui = new GUI(this);
	}

	public void addFile(final File file) {
		if (file.isDirectory()) {
			for (File curFile : file.listFiles())
				addFile(curFile);
			return;
		}
		//Only care about xml files
		if (!file.getName().endsWith(".xml")) {
			log.debug("Ignoring file " + file.getAbsolutePath());
			return;
		}
		generalThreadPool.execute(new Runnable() {
			public void run() {
				try {
					parsers.put(new DumpParser(file), new DatabaseWriter());
				} catch (Exception e) {
					//TODO: Inform other parts of failure?
					log.error("Cannot load " + file);
				}
			}
		});
	}
	
	public static void main(String[] args) {
		new Controller();
	}
}
