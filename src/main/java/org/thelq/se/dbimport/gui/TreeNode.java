
package org.thelq.se.dbimport.gui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.DumpParser;

/**
 *
 * @author Leon
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreeNode {
	//protected DumpParser parser;
	//protected DatabaseWriter dbWriter;
	protected boolean enabled;
	protected String fileName;
	protected int numProcessed;
	protected String parserStatus;
	protected String dbStatus;
	
}
