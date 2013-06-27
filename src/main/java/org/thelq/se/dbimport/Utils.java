package org.thelq.se.dbimport;

import org.thelq.se.dbimport.sources.DumpContainer;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thelq.se.dbimport.sources.DumpContainer;

/**
 *
 * @author Leon
 */
@Slf4j
public class Utils {
	protected static String getCaseInsensitive(Collection<String> data, String needle) {
		for (String curString : data)
			if (curString.equalsIgnoreCase(needle))
				return curString;
		//Not found
		return null;
	}

	/**
	 * Get long form of location in "Type Location" format
	 * @param container
	 * @return 
	 */
	public static String getLongLocation(DumpContainer dumpContainer) {
		return dumpContainer.getType() + " " + dumpContainer.getLocation();
	}

	/**
	 * SwingUtilities.invokeAndWait exceptions rethrown as unchecked RuntimeExceptions.
	 * Saves an extra layer of indentation
	 * @param runnable 
	 */
	public static void invokeAndWaitUnchecked(Runnable runnable) {
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (Exception e) {
			throw new RuntimeException("Cannot wait for invokeAndWait", e);
		}
	}

	public static void shutdownPool(ExecutorService pool, String poolName) throws InterruptedException {
		pool.shutdown();
		int secondsPassed = 0;
		while (pool.awaitTermination(5, TimeUnit.SECONDS) == false) {
			secondsPassed = secondsPassed + 5;
			log.info("Still waiting for " + poolName + " to " + secondsPassed);
		}
	}

	public static String genTablePrefix(String containerName) {
		String name = containerName.trim().toLowerCase();
		if (StringUtils.isBlank(name))
			return "";
		//Hardcoded conversions
		name = StringUtils.removeEnd(name, ".7z");
		name = StringUtils.removeEnd(name, ".stackexchange.com");
		if (StringUtils.contains(name, "stackoverflow"))
			name = StringUtils.replace(name, "stackoverflow", "so");
		else if (StringUtils.contains(name, "serverfault"))
			name = StringUtils.replace(name, "serverfault", "sf");
		else if (StringUtils.containsIgnoreCase(name, "superuser"))
			name = StringUtils.replace(name, "superuser", "su");

		//Remove unnessesary extensions
		name = StringUtils.removeEnd(name, ".com");

		//Meta handling
		if (StringUtils.startsWith(name, "meta"))
			name = StringUtils.removeStart(name, "meta") + "_m";
		else if (StringUtils.startsWith(name, "meta."))
			name = StringUtils.removeStart(name, "meta.") + "_m";

		//Basic make valid for SQL
		//TODO: more validation?
		name = StringUtils.remove(name, " ");
		name = StringUtils.remove(name, ".");
		return name + "_";
	}
}
