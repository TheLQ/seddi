package org.thelq.se.dbimport.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.plaf.basic.BasicTreeUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 *
 * @author Leon
 */
@Slf4j
public class TreeParserModel extends AbstractTreeTableModel {
	protected TreeRoot root = new TreeRoot();
	protected List<String> columns = Arrays.asList("Enabled", "Source", "# Processed", "Parser Status", "Database Status");

	@Override
	public String getColumnName(int column) {
		return columns.get(column);
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return (node instanceof TreeNode);
	}

	@Override
	public Object getValueAt(Object node, int column) {
		if (node instanceof TreeNodeContainer) {
			log.debug("getValueAt for TreeNodeContainer at column " + column);
			TreeNodeContainer container = (TreeNodeContainer) node;
			if (column == 0)
				return container.getLocation();
			else
				return null;
		} else if (node instanceof TreeNode) {
			log.debug("getValueAt for TreeNode at column " + column);
			TreeNode treeNode = (TreeNode) node;
			if (column == 0)
				return treeNode.isEnabled();
			else if (column == 1)
				return treeNode.getFileName();
			else if (column == 2)
				return treeNode.getNumProcessed();
			else if (column == 3)
				return treeNode.getParserStatus();
			else if (column == 4)
				return treeNode.getDbStatus();
			else
				throw new RuntimeException("Unknown column " + column + " for node " + node);
		} else
			throw new RuntimeException("Unknown node " + node + " at column " + column);
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof TreeRoot)
			return ((TreeRoot) root).getContainers().get(index);
		else if (parent instanceof TreeNodeContainer)
			return ((TreeNodeContainer) parent).getNodes().get(index);
		else
			throw new RuntimeException("Unknown parent type " + parent);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof TreeRoot)
			return ((TreeRoot) parent).getContainers().size();
		else if (parent instanceof TreeNodeContainer)
			return ((TreeNodeContainer) parent).getNodes().size();
		else
			throw new RuntimeException("Unknown parent type " + parent);
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent instanceof TreeNodeContainer) {
			if (!(child instanceof TreeNode))
				throw new RuntimeException("Unknown child type " + child);
			TreeNodeContainer container = (TreeNodeContainer) parent;
			int index = container.getNodes().indexOf(child);
			if (index == -1)
				throw new RuntimeException("Parent " + parent + " doesn't contain child " + child);
			return index;
		} else if (parent instanceof TreeRoot) {
			if (!(child instanceof TreeNodeContainer))
				throw new RuntimeException("Unknown child type " + child);
			TreeRoot root = (TreeRoot) parent;
			int index = root.getContainers().indexOf(child);
			if (index == -1)
				throw new RuntimeException("Parent " + parent + " doesn't contain child " + child);
			return index;
		}
		throw new RuntimeException("Unknown parent " + parent);
	}

	public List<TreeNodeContainer> getContainers() {
		return root.getContainers();
	}
}
