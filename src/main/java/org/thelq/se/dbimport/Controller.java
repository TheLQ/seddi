package org.thelq.se.dbimport;

import java.io.File;
import java.util.LinkedList;
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
	protected LinkedList<DumpParser> parsers = new LinkedList();
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
			log.info("Ignoring folder " + file.getAbsolutePath());
			return;
		}
		if (!file.getName().endsWith(".xml")) {
			log.info("Ignoring non-XML file " + file.getAbsolutePath());
			return;
		}
		generalThreadPool.execute(new Runnable() {
			public void run() {
				try {
					parsers.add(new DumpParser(file));
				} catch (Exception e) {
					//TODO: Inform other parts of failure?
					log.error("Cannot load " + file, e);
				}
			}
		});
	}

	public static void main(String[] args) {
		new Controller();
	}
}
