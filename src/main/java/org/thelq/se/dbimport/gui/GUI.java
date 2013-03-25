package org.thelq.se.dbimport.gui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.thelq.se.dbimport.Controller;
import org.thelq.se.dbimport.DatabaseWriter;
import org.thelq.se.dbimport.Utils;
import org.thelq.se.dbimport.sources.DumpContainer;
import org.thelq.se.dbimport.sources.FolderDumpContainer;

/**
 *
 * @author Leon
 */
@Slf4j
public class GUI {
	protected Controller controller;
	protected JFrame frame;
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
	protected JSpinner batchSize;
	protected DefaultFormBuilder locationsBuilder;
	@Getter
	protected JTextPane loggerText;
	protected GUILogAppender logAppender;
	protected List<GUIDumpContainer> guiDumpContainers = new ArrayList();

	public GUI(Controller passedController) {
		//Initialize logger
		logAppender = new GUILogAppender(this);

		//Set our Look&Feel
		try {
			if (SystemUtils.IS_OS_WINDOWS)
				UIManager.setLookAndFeel(new WindowsLookAndFeel());
			else
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.warn("Defaulting to Swing L&F due to exception", e);
		}

		this.controller = passedController;
		frame = new JFrame();
		frame.setTitle("Unified StackExchange Data Dump Importer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		//Setup menu
		JMenuBar menuBar = new JMenuBar();
		JMenuItem menuAdd = new JMenuItem("Add Files/Folders");
		menuAdd.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuAdd);
		frame.setJMenuBar(menuBar);

		//Primary panel
		FormLayout primaryLayout = new FormLayout("5dlu, max(140dlu;pref), 5dlu, 5dlu, pref",
				"pref, top:pref, pref, fill:140dlu, pref, fill:80dlu");
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
		FormLayout optionsLayout = new FormLayout("pref, 3dlu, pref:grow", "");
		DefaultFormBuilder optionsBuilder = new DefaultFormBuilder(optionsLayout);
		optionsBuilder.append(disableCreateTables = new JCheckBox("Disable Creating Tables"), 3);
		optionsBuilder.append(lowerMemoryUsage = new JCheckBox("Lower memory usage"), 3);
		optionsBuilder.append("Global Table Prefix", globalTablePrefix = new JTextField(7));
		optionsBuilder.append("Batch Size", batchSize = new JSpinner());
		batchSize.setModel(new SpinnerNumberModel(500, 1, 500000, 1));
		primaryBuilder.add(optionsBuilder.getPanel(), CC.xy(5, 2));

		//Locations
		primaryBuilder.addSeparator("Dump Locations", CC.xyw(1, 3, 5));
		FormLayout locationsLayout = new FormLayout("pref:grow", "pref");
		locationsBuilder = new DefaultFormBuilder(locationsLayout)
				.background(Color.WHITE);
		JScrollPane locationsPane = new JScrollPane(locationsBuilder.getPanel());
		locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		locationsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		primaryBuilder.add(locationsPane, CC.xyw(2, 4, 4));

		//Logger
		primaryBuilder.addSeparator("Log", CC.xyw(1, 5, 5));
		loggerText = new NoWrapJTextPane();
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

		menuAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Allow 7z files but handle corner cases
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle("Select Folders/Files/Archives");

				if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;

				//Add files and folders in a seperate thread while updating gui in EDT
				importButton.setEnabled(false);
				for (File curFile : fc.getSelectedFiles()) {
					controller.addDumpContainer(new FolderDumpContainer(curFile));
					updateLocations();
				}
				importButton.setEnabled(true);
			}
		});

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
		dbType.addItem(DatabaseOption.CUSTOM);
		setDbOption((DatabaseOption) dbType.getItemAt(0));
		dbType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				//Don't run this twice for a single select
				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;

				//Do not change anything if Custom is selected so user can edit it
				DatabaseOption selectedOption = (DatabaseOption) dbType.getSelectedItem();
				if (selectedOption != DatabaseOption.CUSTOM)
					setDbOption(selectedOption);
			}
		});

		//Change type to custom when a field is edited 
		jdbcString.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void updatePerformed(DocumentEvent e) {
				DatabaseOption selectedOption = (DatabaseOption) dbType.getSelectedItem();
				if (selectedOption == DatabaseOption.CUSTOM)
					//Nothing to match
					return;
				String jdbcUser = jdbcString.getText();
				String jdbcOption = selectedOption.jdbcString();
				if (!StringUtils.substringBefore(jdbcUser, "://").equals(StringUtils.substringBefore(jdbcOption, "://"))
						|| !StringUtils.substringAfter(jdbcUser, "?").equals(StringUtils.substringAfter(jdbcOption, "?")))
					dbType.setSelectedItem(DatabaseOption.CUSTOM);
			}
		});
		dialect.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void updatePerformed(DocumentEvent e) {
				DatabaseOption selectedOption = (DatabaseOption) dbType.getSelectedItem();
				if (!dialect.getText().equals(selectedOption.dialect()) && selectedOption != DatabaseOption.CUSTOM)
					dbType.setSelectedItem(DatabaseOption.CUSTOM);
			}
		});
		driver.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void updatePerformed(DocumentEvent e) {
				DatabaseOption selectedOption = (DatabaseOption) dbType.getSelectedItem();
				if (!driver.getText().equals(selectedOption.driver()) && selectedOption != DatabaseOption.CUSTOM)
					dbType.setSelectedItem(DatabaseOption.CUSTOM);
			}
		});


		//Show and hide advanced options with checkbox
		dbAdvanced.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean selected = ((JCheckBox) e.getSource()).isSelected();
				driver.setVisible(selected);
				((JLabel) driver.getClientProperty("labeledBy")).setVisible(selected);
				dialect.setVisible(selected);
				((JLabel) dialect.getClientProperty("labeledBy")).setVisible(selected);
				updateGuiLayout();
			}
		});

		//Done, init logger
		logAppender.init();
		log.info("Finished creating GUI");
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
				DatabaseWriter.setBatchSize((Integer) batchSize.getValue());
				DatabaseWriter.init();
			} catch (Exception e) {
				throw new Exception("Cannot connect to database", e);
			}
		}
	}

	/**
	 * Set the dbType, driver, dialect, and jdbcString to the specified DatabaseOption
	 * @param option 
	 */
	protected void setDbOption(DatabaseOption option) {
		dbType.setSelectedItem(option);
		driver.setText(option.driver());
		driver.setCaretPosition(0);
		dialect.setText(option.dialect());
		dialect.setCaretPosition(0);
		jdbcString.setText(option.jdbcString());
		jdbcString.setCaretPosition(0);
	}

	/**
	 * Disable or enable the fields in the GUI
	 * @param enabled 
	 */
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

	/**
	 * Validate and pack the entire frame
	 */
	protected void updateGuiLayout() {
		frame.pack();
		frame.validate();
	}

	/**
	 * Update the list of locations
	 */
	protected void updateLocations() {
		int previousContainerSize = guiDumpContainers.size();
		Outer:
		for (DumpContainer curDumpContainer : controller.getDumpContainers()) {
			//Have we already added this?
			for (GUIDumpContainer curGuiDumpContainer : guiDumpContainers)
				if (curGuiDumpContainer.getDumpContainer() == curDumpContainer)
					continue Outer;

			//No, create a new entery in the locations pane
			GUIDumpContainer guiDumpContainer = new GUIDumpContainer(curDumpContainer);
			guiDumpContainers.add(guiDumpContainer);

			FormLayout layout = new FormLayout("15dlu, fill:pref:grow, pref, 3dlu, pref", "pref:grow, pref:grow");
			final PanelBuilder curLocationBuilder = new PanelBuilder(layout, new FormDebugPanel())
					.background(Color.WHITE);
			final JLabel headerLabel = new JLabel(curDumpContainer.getType() + " " + curDumpContainer.getLocation());
			headerLabel.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
			curLocationBuilder.add(headerLabel, CC.xyw(1, 1, 2));
			final JTextField headerPrefix = new JTextField(6);
			curLocationBuilder.add(headerPrefix, CC.xy(5, 1));
			guiDumpContainer.setTablePrefix(headerPrefix);

			//Try to generate a prefix from the container name
			String containerName = StringUtils.substringAfterLast(curDumpContainer.getLocation(), "/");
			if (StringUtils.isBlank(containerName))
				containerName = StringUtils.substringAfterLast(curDumpContainer.getLocation(), "\\");
			if (!StringUtils.isBlank(containerName))
				headerPrefix.setText(Utils.genTablePrefix(containerName));
			else
				log.warn("Unable to generate a table prefix for " + curDumpContainer.getLocation());

			//Generate a table
			final JTable table = new JTable(new DumpContainerTableModel(guiDumpContainer));
			table.setVisible(false);
			table.setFillsViewportHeight(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			curLocationBuilder.add(table, CC.xyw(2, 2, 4));
			guiDumpContainer.setTable(table);

			//Add to builder
			locationsBuilder.append(curLocationBuilder.getPanel());
			updateGuiLayout();

			//Handlers
			headerLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					table.setVisible(!table.isVisible());
					if (table.isVisible())
						headerLabel.setIcon(UIManager.getIcon("Tree.expandedIcon"));
					else
						headerLabel.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
					curLocationBuilder.getPanel().revalidate();
				}
			});
		}

		if (previousContainerSize != guiDumpContainers.size()) {
			//Update all column sizes
			int maxNameWidth = 0;
			int maxSizeWidth = 0;
			for (GUIDumpContainer curGuiDumpContainer : guiDumpContainers) {
				JTable curTable = curGuiDumpContainer.getTable();
				maxNameWidth = Math.max(maxNameWidth, getMaxColumnSize(curTable, DumpContainerColumn.NAME));
				maxSizeWidth = Math.max(maxSizeWidth, getMaxColumnSize(curTable, DumpContainerColumn.SIZE));
			}
			maxNameWidth += 6;
			maxSizeWidth += 6;
			for (GUIDumpContainer curGuiDumpContainer : guiDumpContainers) {
				JTable curTable = curGuiDumpContainer.getTable();
				setColumnWidth(curTable, DumpContainerColumn.NAME, maxNameWidth);
				setColumnWidth(curTable, DumpContainerColumn.SIZE, maxSizeWidth);

				//Split remaining width
				int totalRemaining = (int) curTable.getSize().getWidth() - maxNameWidth - maxSizeWidth;
				setColumnWidth(curTable, DumpContainerColumn.PARSER, totalRemaining / 2);
				setColumnWidth(curTable, DumpContainerColumn.DATABASE, totalRemaining / 2);
			}
		}
	}

	/**
	 * Set the column width on the specified table
	 * Modified from https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
	 * @param table
	 * @param column
	 * @param width 
	 */
	protected static void setColumnWidth(JTable table, DumpContainerColumn column, int width) {
		TableColumn tableColumn = table.getColumnModel().getColumn(column.getId());
		tableColumn.setMaxWidth(width);
		tableColumn.setWidth(width);
		table.getTableHeader().setResizingColumn(tableColumn);
	}

	/**
	 * Get the maximum size of a column in the specified table
	 * Modified from https://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
	 * @param table 
	 * @param column
	 * @return 
	 */
	protected static int getMaxColumnSize(JTable table, DumpContainerColumn column) {
		int max = 0;
		int maxWidth = table.getColumnModel().getColumn(column.getId()).getMaxWidth();
		for (int row = 0; row < table.getRowCount(); row++) {
			TableCellRenderer cellRenderer = table.getCellRenderer(row, column.getId());
			Component c = table.prepareRenderer(cellRenderer, row, column.getId());
			int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
			if (width >= maxWidth) {
				max = maxWidth;
				break;
			} else
				max = Math.max(max, width);
		}
		return max;
	}
}
