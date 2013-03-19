package org.thelq.se.dbimport.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Leon
 */
public abstract class SimpleDocumentListener<E> implements DocumentListener {
	public void insertUpdate(DocumentEvent e) {
		updatePerformed(e);
	}

	public void removeUpdate(DocumentEvent e) {
		updatePerformed(e);
	}

	public void changedUpdate(DocumentEvent e) {
		updatePerformed(e);
	}

	public abstract void updatePerformed(DocumentEvent e);
}
