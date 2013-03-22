package org.thelq.se.dbimport;

import java.io.File;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Leon
 */
public class Utils {
	protected static String getCaseInsensitive(Collection<String> data, String needle) {
		for (String curString : data)
			if (curString.equalsIgnoreCase(needle))
				return curString;
		//Not found
		return null;
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

	public static String genTablePrefix(File location) {
		if (!location.isDirectory())
			throw new IllegalArgumentException("File " + location.getAbsolutePath() + " is not a folder");
		String name = location.getName();
		//Hardcoded conversions
		if (StringUtils.contains(name, "Stackoverflow"))
			name = "so";
		else if (StringUtils.contains(name, "serverfault"))
			name = "sf";
		else if (StringUtils.contains(name, "superuser"))
			name = "su";
		else if (StringUtils.startsWith(name, "meta"))
			name = StringUtils.remove(name, "meta") + "_m";
		else if (StringUtils.startsWith(name, "meta."))
			name = StringUtils.remove(name, "meta.") + "_m";

		//Remove domain .com
		if (StringUtils.contains(name, ".com"))
			name = StringUtils.remove(name, ".com");
		else if (StringUtils.contains(name, "7z"))
			name = StringUtils.remove(name, ".7z");
		return name + "_";
	}
}
