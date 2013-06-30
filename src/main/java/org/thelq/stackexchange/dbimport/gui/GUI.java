package org.thelq.stackexchange.dbimport.gui;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;
import org.thelq.stackexchange.dbimport.Controller;
import org.thelq.stackexchange.dbimport.DatabaseWriter;
import org.thelq.stackexchange.dbimport.Utils;
import org.thelq.stackexchange.dbimport.sources.ArchiveDumpContainer;
import org.thelq.stackexchange.dbimport.sources.DumpContainer;
import org.thelq.stackexchange.dbimport.sources.DumpEntry;
import org.thelq.stackexchange.dbimport.sources.FolderDumpContainer;

/**
 *
 * @author Leon
 */
@Slf4j
public class GUI {
	@Getter
	protected Controller controller;
	protected JFrame frame;
	protected JComboBox<DatabaseOption> dbType;
	protected JTextField username;
	protected JTextField password;
	protected JTextField jdbcString;
	protected JTextField dialect;
	protected JTextField driver;
	protected JCheckBox dbAdvanced;
	protected JButton importButton;
	protected JCheckBox disableCreateTables;
	protected JSpinner threads;
	protected JTextField globalTablePrefix;
	protected JSpinner batchSize;
	protected DefaultFormBuilder locationsBuilder;
	protected JScrollPane locationsPane;
	@Getter
	protected JTextPane loggerText;
	protected GUILogAppender logAppender;
	protected JMenuItem menuAdd;

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
		menuAdd = new JMenuItem("Add Folders/Archives");
		menuAdd.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuAdd);
		frame.setJMenuBar(menuBar);

		//Primary panel
		FormLayout primaryLayout = new FormLayout("5dlu, pref:grow, 5dlu, 5dlu, pref",
				"pref, top:pref, pref, fill:140dlu:grow, pref, fill:80dlu");
		PanelBuilder primaryBuilder = new PanelBuilder(primaryLayout)
				.border(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		//DB Config panel
		primaryBuilder.addSeparator("Database Configuration", CC.xyw(1, 1, 2));
		FormLayout configLayout = new FormLayout("pref, 3dlu, pref:grow, 6dlu, pref",
				"pref, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref:grow");
		configLayout.setHonorsVisibility(true);
		final PanelBuilder configBuilder = new PanelBuilder(configLayout);
		configBuilder.addLabel("Preset", CC.xy(1, 2), dbType = new JComboBox<DatabaseOption>(), CC.xy(3, 2));
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
		optionsBuilder.append("Global Table Prefix", globalTablePrefix = new JTextField(7));
		optionsBuilder.append("Threads", threads = new JSpinner());
		//Save a core for the database
		int numThreads = Runtime.getRuntime().availableProcessors();
		numThreads = (numThreads != 1) ? numThreads - 1 : numThreads;
		threads.setModel(new SpinnerNumberModel(numThreads, 1, 100, 1));
		optionsBuilder.append("Batch Size", batchSize = new JSpinner());
		batchSize.setModel(new SpinnerNumberModel(500, 1, 500000, 1));
		primaryBuilder.add(optionsBuilder.getPanel(), CC.xy(5, 2));

		//Locations
		primaryBuilder.addSeparator("Dump Locations", CC.xyw(1, 3, 5));
		FormLayout locationsLayout = new FormLayout("pref, 15dlu, pref, 5dlu, pref, 5dlu, pref:grow, 2dlu, pref", "");
		locationsBuilder = new DefaultFormBuilder(locationsLayout, new ScrollablePanel())
				.background(Color.WHITE)
				.lineGapSize(Sizes.ZERO);
		locationsPane = new JScrollPane(locationsBuilder.getPanel());
		locationsPane.getViewport().setBackground(Color.white);
		locationsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		locationsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		primaryBuilder.add(locationsPane, CC.xyw(2, 4, 4));

		//Logger
		primaryBuilder.addSeparator("Log", CC.xyw(1, 5, 5));
		loggerText = new JTextPane();
		loggerText.setEditable(false);
		JPanel loggerTextPanel = new JPanel(new BorderLayout());
		loggerTextPanel.add(loggerText);
		JScrollPane loggerPane = new JScrollPane(loggerTextPanel);
		loggerPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		loggerPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		JPanel loggerPanePanel = new JPanel(new BorderLayout());
		loggerPanePanel.add(loggerPane);
		primaryBuilder.add(loggerPanePanel, CC.xyw(2, 6, 4));

		menuAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: Allow 7z files but handle corner cases
				final JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle("Select Folders/Archives");
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Archives", "7z", "zip"));
				fc.addChoosableFileFilter(new FileFilter() {
					@Getter
					protected String description = "Folders";

					@Override
					public boolean accept(File file) {
						return file.isDirectory();
					}
				});

				if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
					return;

				//Add files and folders in a seperate thread while updating gui in EDT
				importButton.setEnabled(false);
				for (File curFile : fc.getSelectedFiles()) {
					DumpContainer dumpContainer = null;
					try {
						if (curFile.isDirectory())
							dumpContainer = new FolderDumpContainer(curFile);
						else
							dumpContainer = new ArchiveDumpContainer(controller, curFile);
						controller.addDumpContainer(dumpContainer);
					} catch (Exception ex) {
						String type = (dumpContainer != null) ? dumpContainer.getType() : "";
						LoggerFactory.getLogger(getClass()).error("Cannot open " + type, ex);
						String location = (dumpContainer != null) ? Utils.getLongLocation(dumpContainer) : "";
						showErrorDialog(ex, "Cannot open " + location, curFile.getAbsolutePath());
						continue;
					}
				}
				updateLocations();
				importButton.setEnabled(true);
			}
		});

		//Add options (Could be in a map, but this is cleaner)
		dbType.addItem(DatabaseOption.SELECTONE);
		dbType.addItem(new DatabaseOption()
				.name("MySQL")
				.jdbcString("jdbc:mysql://127.0.0.1:3306/stackexchange?rewriteBatchedStatements=true")
				.dialect("org.hibernate.dialect.MySQL5Dialect")
				.driver("com.mysql.jdbc.Driver"));
		dbType.addItem(new DatabaseOption()
				.name("PostgreSQL")
				.jdbcString("jdbc:postgresql://127.0.0.1:5432/stackexchange")
				.dialect("org.hibernate.dialect.PostgreSQLDialect")
				.driver("org.postgresql.Driver"));
		dbType.addItem(DatabaseOption.CUSTOM);
		setDbOption(dbType.getItemAt(0));
		dbType.addItemListener(new ItemListener() {
			boolean shownMysqlWarning = false;

			public void itemStateChanged(ItemEvent e) {
				//Don't run this twice for a single select
				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;

				DatabaseOption selectedOption = (DatabaseOption) dbType.getSelectedItem();
				if (selectedOption.name().equals("MySQL") && !shownMysqlWarning) {
					//Hide popup so you don't have to click twice on the dialog 
					dbType.setPopupVisible(false);
					JOptionPane.showMessageDialog(frame,
							"Warning: Your server must be configured with character_set_server=utf8mb4"
							+ "\nOtherwise, data dumps that contain 4 byte UTF-8 characters will fail",
							"MySQL Warning",
							JOptionPane.WARNING_MESSAGE);
					shownMysqlWarning = true;
				}

				//Do not change anything if Custom is selected so user can edit it
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
			}
		});

		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (controller.getDumpContainers().isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Please add dump folders/archives", "Import Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (dbType.getSelectedItem() == DatabaseOption.CUSTOM) {
					JOptionPane.showMessageDialog(frame, "Please configure database options", "Import Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				//Disable all GUI components so they can't change anything during processing
				setGuiEnabled(false);

				//Run in new thread
				controller.getGeneralThreadPool().execute(new Runnable() {
					public void run() {
						try {
							start();
						} catch (final Exception e) {
							//Show an error message box
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									LoggerFactory.getLogger(getClass()).error("Cannot import", e);
									showErrorDialog(e, "Cannot import", null);
								}
							});
						}
						//Renable GUI
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setGuiEnabled(true);
							}
						});
					}
				});
			}
		});

		//Done, init logger
		logAppender.init();
		log.info("Finished creating GUI");

		//Display
		frame.setContentPane(primaryBuilder.getPanel());
		frame.pack();
		frame.setMinimumSize(frame.getSize());

		frame.setVisible(true);
	}

	protected void start() throws Exception {
		//Set database info
		DatabaseWriter.setUsername(username.getText());
		DatabaseWriter.setPassword(password.getText());
		DatabaseWriter.setDialect(dialect.getText());
		DatabaseWriter.setDriver(driver.getText());
		DatabaseWriter.setJdbcString(jdbcString.getText());
		DatabaseWriter.setBatchSize((Integer) batchSize.getValue());
		DatabaseWriter.setGlobalPrefix(globalTablePrefix.getText());

		//Need to set the table prefix so everything else sees it
		for (DumpContainer curContainer : controller.getDumpContainers()) {
			log.info("Setting prefix on " + curContainer.getLocation() + " to " + curContainer.getGuiTablePrefix().getText());
			curContainer.setTablePrefix(curContainer.getGuiTablePrefix().getText());
		}

		controller.importAll((Integer) threads.getValue(), !disableCreateTables.isSelected());
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
		threads.setEnabled(enabled);
		globalTablePrefix.setEnabled(enabled);
		batchSize.setEnabled(enabled);
		dbAdvanced.setEnabled(enabled);
		menuAdd.setEnabled(enabled);
	}

	/**
	 * Update the list of locations
	 */
	protected void updateLocations() {
		locationsBuilder.getPanel().removeAll();
		for (final DumpContainer curContainer : controller.getDumpContainers()) {
			//Initialize components
			if (curContainer.getGuiHeader() == null) {
				JLabel headerLabel = new JLabel(Utils.getLongLocation(curContainer));
				headerLabel.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
				curContainer.setGuiHeader(headerLabel);
				//Handlers
				headerLabel.addMouseListener(new MouseAdapter() {
					boolean visible = true;

					@Override
					public void mouseClicked(MouseEvent e) {
						//Update labels
						visible = !visible;
						for (DumpEntry curEntry : curContainer.getEntries()) {
							curEntry.getGuiName().setVisible(visible);
							curEntry.getGuiSize().setVisible(visible);
							curEntry.getGuiLog().setVisible(visible);
							if (curEntry.getGuiSeparator() != null)
								curEntry.getGuiSeparator().setVisible(visible);
						}

						//Change icon
						if (visible)
							curContainer.getGuiHeader().setIcon(UIManager.getIcon("Tree.expandedIcon"));
						else
							curContainer.getGuiHeader().setIcon(UIManager.getIcon("Tree.collapsedIcon"));
						locationsPane.revalidate();
					}
				});
			}
			if (curContainer.getGuiTablePrefix() == null) {
				JTextField headerPrefix = new JTextField(6);
				curContainer.setGuiTablePrefix(headerPrefix);
				headerPrefix.setText(Utils.genTablePrefix(curContainer.getName()));
				if (StringUtils.isBlank(headerPrefix.getText()))
					log.warn("Unable to generate a table prefix for {}", curContainer.getLocation());
			}

			//Start adding to panel
			locationsBuilder.leadingColumnOffset(0);
			locationsBuilder.append(curContainer.getGuiHeader(), 7);
			locationsBuilder.append(curContainer.getGuiTablePrefix());
			locationsBuilder.nextLine();
			locationsBuilder.leadingColumnOffset(2);

			Iterator<DumpEntry> entriesItr = curContainer.getEntries().iterator();
			while (entriesItr.hasNext()) {
				DumpEntry curEntry = entriesItr.next();
				if (curEntry.getGuiName() == null)
					curEntry.setGuiName(new JLabel(curEntry.getName()));
				locationsBuilder.append(curEntry.getGuiName());
				if (curEntry.getGuiSize() == null)
					curEntry.setGuiSize(new JLabel(sizeInMegabytes(curEntry.getSizeBytes())));
				locationsBuilder.append(curEntry.getGuiSize());
				if (curEntry.getGuiLog() == null)
					curEntry.setGuiLog(new JLabel("Waiting..."));
				locationsBuilder.append(curEntry.getGuiLog(), 3);
				locationsBuilder.nextLine();
				if (entriesItr.hasNext()) {
					if (curEntry.getGuiSeparator() == null)
						curEntry.setGuiSeparator(new JSeparator());
					locationsBuilder.append(curEntry.getGuiSeparator(), 7);
					locationsBuilder.nextLine();
				}
			}
		}

		locationsPane.validate();
	}

	public static String sizeInMegabytes(long size) {
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, 2)) + " MiB";
	}

	protected void showErrorDialog(Exception ex, String title, String messageRaw) {
		String root = "";
		if (ex.getCause() != null)
			root = "\n" + ExceptionUtils.getRootCauseMessage(ex);
		String message = "";
		if (!StringUtils.isBlank(messageRaw))
			message = "\n" + messageRaw.trim();
		JOptionPane.showMessageDialog(frame, title
				+ message
				+ "\n\nError: " + ex.getLocalizedMessage()
				+ root
				+ "\nSee Log for more information",
				title, JOptionPane.ERROR_MESSAGE);
	}
}
