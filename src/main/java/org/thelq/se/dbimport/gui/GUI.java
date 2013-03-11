package org.thelq.se.dbimport.gui;

import com.google.common.collect.Iterables;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
		JMenuItem menuAdd = new JMenuItem("Add Files/Folders");
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
		FormLayout primaryLayout = new FormLayout("pref:grow, 6dlu, pref:grow", "pref, 6dlu, pref:grow");
		DefaultFormBuilder primaryBuilder = new DefaultFormBuilder(primaryLayout)
				.border(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		//DB Config panel
		FormLayout configLayout = new FormLayout("pref, 3dlu, pref, 10dlu, pref, 3dlu, pref, 10dlu, pref, 3dlu, pref", "pref:grow, pref:grow");
		DefaultFormBuilder configBuilder = new DefaultFormBuilder(configLayout);
		configBuilder.appendSeparator("Database Configuration");
		configBuilder.append("Presest", new JComboBox(new String[]{"MySQL", "SQlite", "MSSQL"}));
		configBuilder.append("Username", new JTextField(10));
		configBuilder.append("Password", new JPasswordField(10));
		configBuilder.append("JDBC Connection", new JTextField(15), configLayout.getColumnCount() - 2);
		primaryBuilder.add(configBuilder.getPanel(), CC.xy(1, 1));

		//Options
		FormLayout optionsLayout = new FormLayout("pref, 3dlu, left:pref:grow, 10dlu, pref, 3dlu, left:pref:grow", "");
		DefaultFormBuilder optionsBuilder = new DefaultFormBuilder(optionsLayout);
		optionsBuilder.appendSeparator("Options");
		optionsBuilder.append("Disable Creating Tables", new JCheckBox());
		optionsBuilder.append("Lower memory usage", new JCheckBox());
		optionsBuilder.append("Global Table Prefix", new JTextField(7));
		primaryBuilder.add(optionsBuilder.getPanel(), CC.xy(3, 1));

		//Locations
		FormLayout locationsLayout = new FormLayout("pref:grow", "pref:grow");
		DefaultFormBuilder locationsBuilder = new DefaultFormBuilder(locationsLayout)
				.background(Color.WHITE);
		primaryBuilder.nextLine(2);
		primaryBuilder.appendSeparator("Dump Locations");
		for (int i = 0; i < 20; i++)
			locationsBuilder.append(genList());
		JScrollPane locationsPane = new JScrollPane(locationsBuilder.getPanel());
		locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		locationsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		primaryBuilder.append(locationsPane, 3);

		//Display
		frame.setContentPane(primaryBuilder.getPanel());
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
