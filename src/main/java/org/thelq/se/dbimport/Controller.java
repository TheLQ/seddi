package org.thelq.se.dbimport;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.thelq.se.dbimport.gui.GUI;
import org.thelq.se.dbimport.sources.DumpContainer;
import org.thelq.se.dbimport.sources.DumpEntry;

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
	protected ExecutorService generalThreadPool;
	protected Map<String, Map<String, Type>> metadataMap;

	public Controller(boolean createGui) {
		generalThreadPool = Executors.newCachedThreadPool(new BasicThreadFactory.Builder()
				.namingPattern("seGeneral-pool-%d")
				.daemon(true)
				.build());
		if (createGui)
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					gui = new GUI(Controller.this);
				}
			});
	}

	public void initMetadataMap() {
		Builder<String, Map<String, Type>> metadataMapBuilder = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
		for (Map.Entry<String, ClassMetadata> curEntry : DatabaseWriter.getSessionFactory().getAllClassMetadata().entrySet()) {
			ClassMetadata tableDataRaw = curEntry.getValue();
			Builder<String, Type> propertiesBuilder = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
			propertiesBuilder.put(tableDataRaw.getIdentifierPropertyName(), tableDataRaw.getIdentifierType());
			for (String curPropertyName : tableDataRaw.getPropertyNames())
				propertiesBuilder.put(curPropertyName, tableDataRaw.getPropertyType(curPropertyName));
			metadataMapBuilder.put(curEntry.getKey(), propertiesBuilder.build());
		}
		metadataMap = metadataMapBuilder.build();
	}

	public void importAll(int threads) {
		if (!DatabaseWriter.isInited())
			throw new RuntimeException("Database isn't inited!");
		if (metadataMap == null)
			initMetadataMap();
		ThreadPoolExecutor importThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, new BasicThreadFactory.Builder()
				.namingPattern("seImport-pool-%d")
				.build());

		log.info("Starting import pool with " + threads + " threads");

		//Order threads by first entry from each container, second entry from each container...
		//This is so we don't slam a single container (IE 7z archives) with all the threads
		List<Future> futures = new ArrayList();
		int curIndex = 0;
		while (true) {
			int numFailed = 0;
			for (final DumpContainer curContainer : dumpContainers) {
				//Make sure this entry actually exists
				List<? extends DumpEntry> entries = curContainer.getEntries();
				if (curIndex >= entries.size()) {
					numFailed++;
					continue;
				}

				//Add to queue
				final DumpEntry curEntry = curContainer.getEntries().get(curIndex);
				futures.add(importThreadPool.submit(new Runnable() {
					public void run() {
						importSingle(curContainer, curEntry);
					}
				}));
			}

			//Check if we've exausted all DumpEntries
			if (numFailed == dumpContainers.size())
				break;

			//Nope, continue to the next index
			curIndex++;
		}

		//Block until all imports have completed
		for (Future curFuture : futures)
			try {
				curFuture.get();
			} catch (Exception e) {
				log.error("Cannot import", e);
			}
	}

	public void importSingle(DumpContainer container, DumpEntry entry) {
		DumpParser parser = entry.getParser();
		if (!metadataMap.containsKey(parser.getRoot()))
			throw new RuntimeException("Cannot find table mapping for root " + parser.getRoot()
					+ " for file " + entry.getLocation());
		parser.setProperties(metadataMap.get(parser.getRoot()));
		entry.setDatabaseWriter(new DatabaseWriter(container.getTablePrefix(), parser.getRoot()));
		while (!parser.isEndOfFile())
			parser.parseNextEntry();

		//Done, close everything
		parser.close();
		entry.getDatabaseWriter().close();
		entry.close();
	}

	public void addDumpContainer(DumpContainer container) {
		//Make sure it doesn't exist already
		for (DumpContainer curContainer : dumpContainers)
			if (curContainer.getLocation().equals(container.getLocation()))
				throw new IllegalArgumentException(container.getType() + " " + container.getLocation()
						+ " has already been added");
		if (container.getEntries().isEmpty())
			throw new RuntimeException(container.getType() + " doesn't have any dump files");
		dumpContainers.add(container);
		log.info("Added " + container.getType() + " " + container.getLocation());
	}

	public static void main(String[] args) {
		new Controller(true);
	}
}
