/**
 * Copyright (C) 2013 Leon Blakey <lord.quackstar at gmail.com>
 *
 * This file is part of Unified StackExchange Data Dump Importer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, softwar
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thelq.stackexchange.dbimport;

import org.thelq.stackexchange.dbimport.sources.DumpContainer;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.thelq.stackexchange.dbimport.sources.DumpContainer;

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
