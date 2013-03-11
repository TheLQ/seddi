/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.se.dbimport.gui;

import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author Leon
 */
public class TreeTableColumnModel extends DefaultTableColumnModelExt {
	private static final long serialVersionUID = -460564269970113310L;
	private int[] savedWidth = null;
	JXTreeTable table;

	public TreeTableColumnModel(JXTreeTable table) {
		super();
		this.table = table;
		if (null != table)
			for (int i = 0; i < table.getColumnCount(); i++)
				this.addColumn(new TableColumnExt(i));
	}

	@Override
	public TableColumn getColumn(int columnIndex) {
		TableColumn tc = super.getColumn(columnIndex);
		/*TreePath tp = table.getPathForRow(aktCellRectRow);
		Object op = null;
		aktTreeNode = null;

		if (null != tp) {
			op = tp.getLastPathComponent();
			if (op instanceof Node)
				aktTreeNode = (TreeNode) op;
		}

		if (null != aktTreeNode && aktTreeNode.isLeaf() != true) {
			saveWidth();
			if (columnIndex == 0)
				tc.setWidth(getWithTotal());
			else
				tc.setWidth(0);
		} else
			restoreWidth();*/
		return tc;
	}

	private int getWithTotal() {
		if (null == savedWidth)
			return -1;
		int width = 0;
		for (int i = 0; i < savedWidth.length; ++i)
			width += savedWidth[ i];
		return width;
	}

	private void saveWidth() {
		if (savedWidth != null)
			return;
		savedWidth = new int[super.getColumnCount()];
		for (int i = 0; i < super.getColumnCount(); ++i) {
			TableColumn tc = super.getColumn(i);
			savedWidth[ i] = tc.getWidth();
		}
	}

	private void restoreWidth() {
		if (savedWidth == null)
			return;
		for (int i = 0; i < super.getColumnCount(); ++i) {
			TableColumn tc = super.getColumn(i);
			tc.setWidth(savedWidth[ i]);
		}
		savedWidth = null;
	}
}