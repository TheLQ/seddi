package org.thelq.se.dbimport;

import java.util.Collection;

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
}
