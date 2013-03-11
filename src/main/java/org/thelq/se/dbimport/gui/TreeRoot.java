
package org.thelq.se.dbimport.gui;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Leon
 */
@Data
public class TreeRoot {
	List<TreeNodeContainer> containers = new ArrayList();
}
