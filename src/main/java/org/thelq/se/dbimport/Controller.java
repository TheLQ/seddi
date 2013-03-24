package org.thelq.se.dbimport;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thelq.se.dbimport.gui.GUI;
import org.thelq.se.dbimport.sources.DumpContainer;

/**
 *
 * @author Leon
 */
@Slf4j
public class Controller {
	protected GUI gui;
	@Getter
	protected List<DumpContainer> dumpContainers = Collections.synchronizedList(new LinkedList());
	@Getter
	protected ExecutorService generalThreadPool = Executors.newCachedThreadPool();

	public Controller() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui = new GUI(Controller.this);
			}
		});
	}
	
	public void addDumpContainer(DumpContainer container) {
		//Make sure it doesn't exist already
		for(DumpContainer curContainer : dumpContainers)
			if(curContainer.getLocation().equals(container.getLocation()))
				throw new IllegalArgumentException(container.getType() + " " + container.getLocation() 
						+ " has already been added");
		dumpContainers.add(container);
		log.info("Added " + container.getType() + " " + container.getLocation());
	}

	public static void main(String[] args) {
		new Controller();
	}
}
