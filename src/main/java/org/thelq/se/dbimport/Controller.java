package org.thelq.se.dbimport;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.jboss.logging.MDC;
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
	protected List<ImportContainer> importContainers = Collections.synchronizedList(new LinkedList());
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

	public void initMetadataMap(SessionFactory sessionFactory) {
		ImmutableSortedMap.Builder<String, Map<String, Type>> metadataMapBuilder = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
		for (Map.Entry<String, ClassMetadata> curEntry : sessionFactory.getAllClassMetadata().entrySet()) {
			ClassMetadata tableDataRaw = curEntry.getValue();
			ImmutableMap.Builder<String, Type> propertiesBuilder = ImmutableMap.builder();
			propertiesBuilder.put(tableDataRaw.getIdentifierPropertyName(), tableDataRaw.getIdentifierType());
			for (String curPropertyName : tableDataRaw.getPropertyNames())
				propertiesBuilder.put(curPropertyName, tableDataRaw.getPropertyType(curPropertyName));
			metadataMapBuilder.put(curEntry.getKey(), propertiesBuilder.build());
		}
		metadataMap = metadataMapBuilder.build();
	}

	public void importAll(int threads, final boolean createTables) throws InterruptedException {
		//Build a test session factory with the first entry to see if database credentials work
		DatabaseWriter.buildSessionFactory(importContainers.get(0));

		if (metadataMap == null)
			initMetadataMap(importContainers.get(0).getSessionFactory());

		//Database wors, start importing
		ThreadPoolExecutor importThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, new BasicThreadFactory.Builder()
				.namingPattern("seImport-pool-%d")
				.build());

		log.info("Starting import pool with " + threads + " threads");

		if (createTables) {
			log.info("Creating all tables");
			final CountDownLatch finishedLatch = new CountDownLatch(importContainers.size());
			for (final ImportContainer curContainer : importContainers)
				importThreadPool.submit(new Runnable() {
					public void run() {
						try {
							MDC.put("longContainer", " [" + curContainer.getDumpContainer().getName() + "]");
							synchronized (curContainer.getHibernateCreateLock()) {
								if (curContainer.getSessionFactory() == null)
									DatabaseWriter.buildSessionFactory(curContainer);
							}
							DatabaseWriter.createTables(curContainer);
						} finally {
							finishedLatch.countDown();
						}
					}
				});
			//Wait for all imports to finish
			finishedLatch.await();
		}

		//Order threads by first entry from each container, second entry from each container...
		//This is so we don't slam a single container (IE 7z archives) with all the threads
		List<Future> futures = new ArrayList();
		int curIndex = 0;
		while (true) {
			int numFailed = 0;
			for (final ImportContainer curContainer : importContainers) {
				//Make sure this entry actually exists
				List<? extends DumpEntry> entries = curContainer.getDumpContainer().getEntries();
				if (curIndex >= entries.size()) {
					numFailed++;
					continue;
				}

				//Add to queue
				final DumpEntry curEntry = curContainer.getDumpContainer().getEntries().get(curIndex);
				futures.add(importThreadPool.submit(new Runnable() {
					public void run() {
						synchronized (curContainer.getHibernateCreateLock()) {
							if (curContainer.getSessionFactory() == null)
								DatabaseWriter.buildSessionFactory(curContainer);
						}
						importSingle(curContainer, curEntry, createTables);
					}
				}));
			}

			//Check if we've exausted all DumpEntries
			if (numFailed == importContainers.size())
				break;

			//Nope, continue to the next index
			curIndex++;
		}

		//Block until all imports have completed
		for (Future curFuture : futures)
			try {
				curFuture.get();
			} catch (Exception e) {
				log.error("Could not wait for import thread to complete", e);
			}
		
		Utils.shutdownPool(importThreadPool, "import pool");
		log.info("Import finished!");
	}

	public void importSingle(ImportContainer container, DumpEntry entry, boolean createTables) {
		try {
			String mdcValue = container.getTablePrefix();
			if (StringUtils.isBlank(mdcValue))
				mdcValue = container.getDumpContainer().getName();
			MDC.put("longContainer", " [" + mdcValue + entry.getName() + "]");

			//Init parser
			DumpParser parser = new DumpParser(entry);
			MDC.put("longContainer", " [" + mdcValue + parser.getRoot() + "]");
			container.getParserMap().put(entry, parser);
			if (!metadataMap.containsKey(parser.getRoot()))
				throw new RuntimeException("Cannot find table mapping for root " + parser.getRoot()
						+ " for file " + entry.getLocation());
			parser.setProperties(metadataMap.get(parser.getRoot()));

			//Init database
			DatabaseWriter databaseWriter = new DatabaseWriter(container, parser.getRoot());
			container.getDatabaseWriterMap().put(entry, databaseWriter);
			parser.setDatabaseWriter(databaseWriter);

			//Import!
			while (!parser.isEndOfFile())
				parser.parseNextEntry();

			//Done, close everything
			parser.close();
			databaseWriter.close();
			entry.close();
		} catch (Exception e) {
			log.error("Cannot import", e);
		}
	}

	public ImportContainer addDumpContainer(DumpContainer container) {
		//Make sure it doesn't exist already
		for (ImportContainer curContainer : importContainers)
			if (curContainer.getDumpContainer().getLocation().equals(container.getLocation()))
				throw new IllegalArgumentException(container.getType() + " " + container.getLocation()
						+ " has already been added");
		if (container.getEntries().isEmpty())
			throw new RuntimeException(container.getType() + " doesn't have any dump files");

		ImportContainer importContainer = new ImportContainer(container);
		importContainers.add(importContainer);
		log.info("Added " + Utils.getLongLocation(importContainer));
		return importContainer;
	}

	public static void main(String[] args) {
		new Controller(true);
	}
}
