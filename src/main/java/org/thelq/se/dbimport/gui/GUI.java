package org.thelq.se.dbimport.gui;

import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.Iterables;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import lombok.Getter;
import ch.qos.logback.classic.Logger;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.thelq.se.dbimport.Controller;
import org.thelq.se.dbimport.DatabaseWriter;

/**
 *
 * @author Leon
 */
public class GUI {
	protected Controller controller;
	protected JFrame frame;
	protected JTable workerTable;
	protected JComboBox dbType;
	protected JTextField username;
	protected JTextField password;
	protected JTextField jdbcString;
	protected JTextField dialect;
	protected JTextField driver;
	protected JCheckBox dbAdvanced;
	protected JButton importButton;
	protected JCheckBox disableCreateTables;
	protected JCheckBox lowerMemoryUsage;
	protected JTextField globalTablePrefix;
	protected DefaultFormBuilder locationsBuilder;
	@Getter
	protected JTextPane loggerText;
	protected AppenderBase logAppender;

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
		FormLayout primaryLayout = new FormLayout("5dlu, pref:grow, 5dlu, 5dlu, pref",
				"pref, top:pref, pref, fill:pref:grow, pref, fill:80dlu");
		PanelBuilder primaryBuilder = new PanelBuilder(primaryLayout)
				.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		//DB Config panel
		primaryBuilder.addSeparator("Database Configuration", CC.xyw(1, 1, 2));
		FormLayout configLayout = new FormLayout("pref, 3dlu, pref:grow, 6dlu, pref",
				"pref, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow");
		configLayout.setHonorsVisibility(true);
		PanelBuilder configBuilder = new PanelBuilder(configLayout);
		configBuilder.addLabel("Preset", CC.xy(1, 2), dbType = new JComboBox(), CC.xy(3, 2));
		configBuilder.add(dbAdvanced = new JCheckBox("Show advanced options"), CC.xy(5, 2));
		configBuilder.addLabel("JDBC Connection", CC.xy(1, 4), jdbcString = new JTextField(15), CC.xyw(3, 4, 3));
		configBuilder.addLabel("Username", CC.xy(1, 6), username = new JTextField(10), CC.xy(3, 6));
		configBuilder.addLabel("Password", CC.xy(1, 8), password = new JPasswordField(10), CC.xy(3, 8));
		configBuilder.add(importButton = new JButton("Import"), CC.xywh(5, 6, 1, 3));
		//Add hidden
		JLabel dialectLabel = new JLabel("Dialect");
		dialectLabel.setVisible(false);
		configBuilder.add(dialectLabel, CC.xy(1, 10), dialect = new JTextField(10), CC.xyw(3, 10, 3));
		dialect.setVisible(false);
		JLabel driverLabel = new JLabel("Driver");
		driverLabel.setVisible(false);
		configBuilder.add(driverLabel, CC.xy(1, 12), driver = new JTextField(10), CC.xyw(3, 12, 3));
		driver.setVisible(false);
		primaryBuilder.add(configBuilder.getPanel(), CC.xy(2, 2));

		//Options
		primaryBuilder.addSeparator("Options", CC.xyw(4, 1, 2));
		FormLayout optionsLayout = new FormLayout("pref, 3dlu, left:pref:grow, left:pref:grow", "");
		DefaultFormBuilder optionsBuilder = new DefaultFormBuilder(optionsLayout);
		optionsBuilder.append(disableCreateTables = new JCheckBox("Disable Creating Tables"), 3);
		optionsBuilder.append(lowerMemoryUsage = new JCheckBox("Lower memory usage"), 3);
		optionsBuilder.append("Global Table Prefix", globalTablePrefix = new JTextField(7));
		primaryBuilder.add(optionsBuilder.getPanel(), CC.xy(5, 2));

		//Locations
		primaryBuilder.addSeparator("Dump Locations", CC.xyw(1, 3, 5));
		FormLayout locationsLayout = new FormLayout("pref:grow", "pref");
		locationsBuilder = new DefaultFormBuilder(locationsLayout)
				.background(Color.WHITE);
		for (int i = 0; i < 10; i++)
			locationsBuilder.append(genList());
		JScrollPane locationsPane = new JScrollPane(locationsBuilder.getPanel());
		locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		locationsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		primaryBuilder.add(locationsPane, CC.xyw(2, 4, 4));

		//Logger
		primaryBuilder.addSeparator("Log", CC.xyw(1, 5, 5));
		loggerText = new JTextPaneNW();
		loggerText.setEditable(false);
		loggerText.setAlignmentX(Component.CENTER_ALIGNMENT);
		JScrollPane loggerPane = new JScrollPane(loggerText);
		loggerPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		loggerPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		primaryBuilder.add(loggerPane, CC.xyw(2, 6, 4));

		//Display
		frame.setContentPane(primaryBuilder.getPanel());
		frame.pack();
		frame.setVisible(true);

		//Initialize logger
		logAppender = new GUILogAppender(this);

		//Import start code
		importButton.addActionListener(new ImportActionListener());

		//Add options (Could be in a map, but this is cleaner)
		dbType.addItem(new DatabaseOption()
				.name("MySQL")
				.jdbcString("jdbc:mysql://127.0.0.1:3306/so_new?rewriteBatchedStatements=true")
				.dialect("org.hibernate.dialect.MySQL5Dialect")
				.driver("com.mysql.jdbc.Driver"));
		dbType.addItem(new DatabaseOption()
				.name("PostgreSQL")
				.jdbcString("jdbc:postgresql://127.0.0.1:5432/so_new")
				.dialect("org.hibernate.dialect.PostgreSQLDialect")
				.driver("org.postgresql.Driver"));
		dbType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setDbOption((DatabaseOption) e.getItem());
			}
		});
		setDbOption((DatabaseOption) dbType.getItemAt(0));

		//Show and hide advanced options with checkbox
		dbAdvanced.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JCheckBox) e.getSource()).isSelected();
				driver.setVisible(selected);
				((JLabel) driver.getClientProperty("labeledBy")).setVisible(selected);
				dialect.setVisible(selected);
				((JLabel) dialect.getClientProperty("labeledBy")).setVisible(selected);
				frame.pack();
				frame.validate();
			}
		});
	}

	@Accessors(fluent = true)
	@Setter
	@Getter
	protected class DatabaseOption {
		String name;
		String jdbcString;
		String driver;
		String dialect;

		@Override
		public String toString() {
			return name;
		}
	}

	protected class ImportActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//Disable all GUI components so they can't change anything during processing
			setGuiEnabled(false);

			//Run in new thread
			controller.getGeneralThreadPool().execute(new Runnable() {
				public void run() {
					try {
						start();
					} catch (final Exception e) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								String root = "";
								if (e.getCause() != null)
									root = "\n" + ExceptionUtils.getRootCauseMessage(e);
								String message = "Error: " + e.getLocalizedMessage()
										+ root
										+ "\nSee Log for more information";
								String title = e.getLocalizedMessage();
								LoggerFactory.getLogger(getClass()).error(message, e);
								JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
							}
						});
					}
					setGuiEnabled(true);
				}
			});
		}

		protected void start() throws Exception {
			//Try to connect to the database
			try {
				DatabaseWriter.setUsername(username.getText());
				DatabaseWriter.setPassword(password.getText());
				DatabaseWriter.setDialect(dialect.getText());
				DatabaseWriter.setDriver(driver.getText());
				DatabaseWriter.setJdbcString(jdbcString.getText());
				DatabaseWriter.init();
			} catch (Exception e) {
				throw new Exception("Cannot connect to database", e);
			}
		}
	}

	protected void setDbOption(DatabaseOption option) {
		dbType.setSelectedItem(option);
		driver.setText(option.driver());
		driver.setCaretPosition(0);
		dialect.setText(option.dialect());
		dialect.setCaretPosition(0);
		jdbcString.setText(option.jdbcString());
		jdbcString.setCaretPosition(0);
	}

	protected void setGuiEnabled(boolean enabled) {
		username.setEnabled(enabled);
		password.setEnabled(enabled);
		dialect.setEnabled(enabled);
		driver.setEnabled(enabled);
		jdbcString.setEnabled(enabled);
		importButton.setEnabled(enabled);
		disableCreateTables.setEnabled(enabled);
		lowerMemoryUsage.setEnabled(enabled);
		globalTablePrefix.setEnabled(enabled);
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

	public class JTextPaneNW extends JTextPane {
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
}
