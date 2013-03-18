package org.thelq.se.dbimport;

import java.io.File;
import java.util.LinkedHashMap;
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
	protected LinkedHashMap<DumpParser, DatabaseWriter> parsers = new LinkedHashMap();
	@Getter
	protected ExecutorService generalThreadPool = Executors.newCachedThreadPool();

	public Controller() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui = new GUI(Controller.this);
			}
		});
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
