package org.thelq.se.dbimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;

/**
 * Hello world!
 *
 */
@Slf4j
public class Main {
	protected DatabaseWriter dbWriter;

	public Main() throws XMLStreamException, Exception {
		dbWriter = new DatabaseWriter();

		//Get the directory that has our files
		File dumpDir = new File("C:/Users/Leon/Downloads/Stack Exchange Data Dump - Aug 2012/082012 Stackapps");
		if (!dumpDir.isDirectory())
			throw new RuntimeException("Provided path " + dumpDir.getAbsolutePath() + " isn't a directory!");

		//Get any dump files
		List<File> dumpFiles = new ArrayList();
		for (File curFile : dumpDir.listFiles())
			if (curFile.getName().endsWith(".xml")) {
				dumpFiles.add(curFile);
				log.info("Added dump file " + curFile.getAbsolutePath());
			}
		if (dumpFiles.isEmpty())
			throw new RuntimeException("No dumpfiles could be found in " + dumpDir.getAbsolutePath());

		//Load up parsers
		Map<String, ClassMetadata> metadataMap = DatabaseWriter.getSessionFactory().getAllClassMetadata();
		List<DumpParser> dumpParsers = new ArrayList();
		for (File curFile : dumpFiles) {
			DumpParser dumpParser = new DumpParser(curFile);
			log.info("Loading file " + curFile.getName() + " with root " + dumpParser.getRoot());

			//Using root node (table name), find column information in hibernate mapping
			String actualTableName = Utils.getCaseInsensitive(metadataMap.keySet(), dumpParser.getRoot());
			if (actualTableName == null)
				throw new RuntimeException("No mapping found for file " + curFile.getName()
						+ " using root " + dumpParser.getRoot());
			ClassMetadata tableDataRaw = metadataMap.get(actualTableName);
			if (tableDataRaw == null)
				throw new RuntimeException("Can't get table metadata for file " + curFile.getName()
						+ " using  " + dumpParser.getRoot());
			Map<String, Type> properties = new HashMap();
			properties.put(tableDataRaw.getIdentifierPropertyName(), tableDataRaw.getIdentifierType());
			for (String curPropertyName : tableDataRaw.getPropertyNames())
				properties.put(curPropertyName, tableDataRaw.getPropertyType(curPropertyName));
			dumpParser.setProperties(properties);

			dumpParsers.add(dumpParser);
		}

		//Execute!
		int totalEntries = 0;
		long startTime = System.currentTimeMillis();
		for (DumpParser parser : dumpParsers) {
			log.info("[ + " + parser.getRoot() + "] Starting parsing");
			String actualTableName = Utils.getCaseInsensitive(metadataMap.keySet(), parser.getRoot());
			while (!parser.isEndOfFile()) {
				log.debug("[ + " + parser.getRoot() + "] Batch: " + parser.getParsedCount());
				dbWriter.insertData(actualTableName, parser.parseNextBatch());
			}
			totalEntries += parser.getParsedCount();
			log.info("[ + " + parser.getRoot() + "] Done!");
		}

		//Get errors
		for (DumpParser parser : dumpParsers)
			for (String curError : parser.getErrors())
				log.warn("SAVED ERROR: " + curError);
		
		long totalSeconds = (System.currentTimeMillis() - startTime) / 60;
		log.info("Done!! Imported " + totalEntries + " in " + totalSeconds + " seconds");
	}

	public static void main(String[] args) {
		try {
			//testHibernate();
			DatabaseWriter.init();
			DatabaseWriter.createTables();
			new Main();
		} catch (Exception e) {
			log.error("Exception encounted in main", e);
		}
	}
}