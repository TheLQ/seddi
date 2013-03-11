package org.thelq.se.dbimport.gui;

import com.google.common.collect.Iterables;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import org.thelq.se.dbimport.Controller;

/**
 *
 * @author Leon
 */
public class GUI {
	protected Controller controller;
	protected JFrame frame;
	protected JTable workerTable;

	public GUI(Controller passedController) {
		this.controller = passedController;
		frame = new JFrame();
		frame.setTitle("Unified StackExchange Data Dump Importer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//Setup menu
		JMenuBar menuBar = new JMenuBar();
		JMenuItem menuAdd = new JMenuItem("Add Files/Directories");
		menuAdd.setMnemonic(KeyEvent.VK_F);
		menuAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle("Select Folders/Files/Archives");

				if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;
				for (File curFile : fc.getSelectedFiles())
					controller.addFile(curFile);
			}
		});
		menuBar.add(menuAdd);
		frame.setJMenuBar(menuBar);

		//Primary panel
		//FormLayout primaryLayout = new FormLayout("pref:grow, 3dlu, pref:grow", "pref, pref, pref:grow");
		FormLayout primaryLayout = new FormLayout("pref:grow", "pref:grow, pref:grow");
		DefaultFormBuilder primaryBuilder = new DefaultFormBuilder(primaryLayout);

		/**
		 * Disable Creating Tables, Lower memory usage
		 */
		//Options
		primaryBuilder.appendSeparator("Options");
		//primaryBuilder.append(new JButton("t2"));
		//primaryBuilder.append(new JButton("t3")); 

		/**
		 * Enabled, Source, Processed, Details
		 */
		//primaryBuilder.append(genList());
		//primaryBuilder.append(genList());
		FormLayout listLayout = new FormLayout("pref:grow", "pref");
		DefaultFormBuilder listBuilder = new DefaultFormBuilder(listLayout).lineGapSize(Sizes.ZERO);
		for (int i = 0; i < 20; i++)
			listBuilder.append(genList());

		//Display
		//frame.setContentPane(primaryBuilder.getPanel());
		frame.setContentPane(new JScrollPane(listBuilder.getPanel()));
		frame.pack();
		frame.setVisible(true);
	}

	protected JComponent genList() {
		FormLayout layout = new FormLayout("15dlu, pref:grow", "pref:grow, pref:grow");
		final JPanel panel = new JPanel(layout);
		panel.setBackground(Color.WHITE);
		//panel.add(new JLabel("Directory:"), CC.xy(1, 1));
		final JLabel label = new JLabel("Directory: C:/df/df/asdf/afdsawe/3/dsafadsc.jpg");
		label.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
		final JComponent table = genJTableExample();
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				table.setVisible(!table.isVisible());
				if (table.isVisible())
					label.setIcon(UIManager.getIcon("Tree.expandedIcon"));
				else
					label.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
				panel.repaint();
			}
		});
		table.setVisible(false);
		panel.add(label, CC.xyw(1, 1, 2));
		panel.add(table, CC.xy(2, 2));
		//panel.setBorder(BorderFactory.createLineBorder(Color.RED));
		return panel;
	}

	protected JComponent genJTableExample() {
		final String[] columnNames = {"First Name",
			"Last Name",
			"Sport",
			"# of Years",
			"Vegetarian"};
		final Object[][] rowData = {
			{"Kathy", "Smith",
				"Snowboarding", new Integer(5), new Boolean(false)},
			{"John", "Doe",
				"Rowing", new Integer(3), new Boolean(true)},
			{"Sue", "Black",
				"Knitting", new Integer(2), new Boolean(false)},
			{"Jane", "White",
				"Speed reading", new Integer(20), new Boolean(true)},
			{"Joe", "Brown",
				"Pool", new Integer(10), new Boolean(false)}
		};
		JTable exampleTable = new JTable(new AbstractTableModel() {
			public String getColumnName(int col) {
				return columnNames[col].toString();
			}

			public int getRowCount() {
				return rowData.length;
			}

			public int getColumnCount() {
				return columnNames.length;
			}

			public Object getValueAt(int row, int col) {
				return rowData[row][col];
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public void setValueAt(Object value, int row, int col) {
				rowData[row][col] = value;
				fireTableCellUpdated(row, col);
			}
		});
		return exampleTable;
	}

	protected JComponent genJTable() {
		JTable workerTable = new JTable(new AbstractTableModel() {
			String[] columns = {"Enabled", "Source", "# Processed", "Parser Status", "Database Status"};

			@Override
			public String getColumnName(int column) {
				return columns[column];
			}

			public int getColumnCount() {
				return columns.length;
			}

			public int getRowCount() {
				return controller.getParsers().size();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0)
					//Enabled status
					return Iterables.get(controller.getParsers().keySet(), rowIndex).isEnabled();
				throw new RuntimeException("Unknown column " + columnIndex);
			}
		});
		workerTable.setFillsViewportHeight(true);
		return workerTable;
	}

	public static void main(String[] args) {
		new GUI(null);
	}
}
