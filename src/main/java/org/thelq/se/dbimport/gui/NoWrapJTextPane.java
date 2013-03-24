package org.thelq.se.dbimport.gui;

import java.awt.Dimension;
import javax.swing.JTextPane;

/**
 *
 * @author Leon
 */
public class NoWrapJTextPane extends JTextPane {
	@Override
	public void setSize(Dimension d) {
		if (d.width < getParent().getSize().width)
			d.width = getParent().getSize().width;
		super.setSize(d);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
}
