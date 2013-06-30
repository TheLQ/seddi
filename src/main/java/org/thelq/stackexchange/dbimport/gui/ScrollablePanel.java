/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thelq.stackexchange.dbimport.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 * @author Leon
 */
public class ScrollablePanel extends JPanel implements Scrollable {
	public ScrollablePanel() {
		super(null);
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 10;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
