
package org.thelq.se.dbimport.gui;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Leon
 */
@Data
public class TreeNodeContainer {
	protected String location;
	protected List<TreeNode> nodes = new ArrayList();
}
