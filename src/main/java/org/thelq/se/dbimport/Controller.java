package org.thelq.se.dbimport;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
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
	protected ExecutorService generalThreadPool;
	protected Map<String, Map<String, Type>> metadataMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);

	public Controller(boolean createGui) {
		//Copied from Executors.newCachedThreadPool()
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

	public void addDumpContainer(DumpContainer container) {
		//Make sure it doesn't exist already
		for (DumpContainer curContainer : dumpContainers)
			if (curContainer.getLocation().equals(container.getLocation()))
				throw new IllegalArgumentException(container.getType() + " " + container.getLocation()
						+ " has already been added");
		dumpContainers.add(container);
		if (container.getEntries().isEmpty())
			throw new RuntimeException(container.getType() + " doesn't have any dump files");
		log.info("Added " + container.getType() + " " + container.getLocation());
	}

	public static void main(String[] args) {
		new Controller(true);
	}
}
