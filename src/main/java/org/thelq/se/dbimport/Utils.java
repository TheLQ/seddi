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
}
