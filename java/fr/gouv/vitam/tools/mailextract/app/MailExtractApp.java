package fr.gouv.vitam.tools.mailextract.app;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class MailExtractApp implements ActionListener, Runnable {

	public MailExtractMainWindow mainWindow;
	static public Logger logger;

	// do option parsing in static main
	String destRootPath = "", destName = "";
	String protocol = "", server = "localhost", user = "", password = "", container = "", folder = "";
	int storeExtractorOptions = 0;
	boolean local = true;;
	String logLevel = "";

	private MailExtractApp(String protocol, String server, String user, String password, String container,
			String folder, String destRootPath, String destName, int storeExtractorOptions, boolean local) {
		this.protocol = protocol;
		this.server = server;
		this.user = user;
		this.password = password;
		this.container = container;
		this.folder = folder;
		this.destRootPath = destRootPath;
		this.destName = destName;
		this.storeExtractorOptions = storeExtractorOptions;
		this.local = local;

		EventQueue.invokeLater(this);
	}

	public void run() {
		try {
			mainWindow = new MailExtractMainWindow(this);
			insertOptions();
			redirectSystemStreams();
			mainWindow.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void insertOptions() {
		if (local) {
			mainWindow.localRadioButton.doClick();
			if (protocol.equals("libpst"))
				mainWindow.outlookRadioButton.doClick();
			else
				mainWindow.thunderbirdRadioButton.doClick();
			mainWindow.containerField.setText(container);
		} else {
			mainWindow.protocoleRadioButton.doClick();
			if (protocol.equalsIgnoreCase("imap"))
				mainWindow.imapRadioButton.doClick();
			else
				mainWindow.imapsRadioButton.doClick();
			mainWindow.userField.setText(user);
			mainWindow.passwordField.setText(password);
			mainWindow.serverField.setText(server);
		}
		mainWindow.folderField.setText(folder);
		mainWindow.savedirField.setText(destRootPath);
		mainWindow.nameField.setText(destName);

		if ((storeExtractorOptions | StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS) != 0) {
			mainWindow.keeponlydeepCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions | StoreExtractor.CONST_DROP_EMPTY_FOLDERS) != 0) {
			mainWindow.dropemptyfoldersCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions | StoreExtractor.CONST_WARNING_MSG_PROBLEM) != 0) {
			mainWindow.warningCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions | StoreExtractor.CONST_NAMES_SHORTENED) != 0) {
			mainWindow.nameshortenedCheckBox.setSelected(true);
		}

	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		// global panel local or protocole enable/disable
		if (command.equals("local")) {
			mainWindow.thunderbirdRadioButton.setEnabled(true);
			mainWindow.outlookRadioButton.setEnabled(true);
			mainWindow.containerLabel.setEnabled(true);
			mainWindow.containerField.setEnabled(true);
			mainWindow.containerButton.setEnabled(true);
			mainWindow.imapRadioButton.setEnabled(false);
			mainWindow.imapsRadioButton.setEnabled(false);
			mainWindow.serverLabel.setEnabled(false);
			mainWindow.serverField.setEnabled(false);
			mainWindow.userLabel.setEnabled(false);
			mainWindow.userField.setEnabled(false);
			mainWindow.passwordLabel.setEnabled(false);
			mainWindow.passwordField.setEnabled(false);
		} else if (command.equals("protocole")) {
			mainWindow.thunderbirdRadioButton.setEnabled(false);
			mainWindow.outlookRadioButton.setEnabled(false);
			mainWindow.containerLabel.setEnabled(false);
			mainWindow.containerField.setEnabled(false);
			mainWindow.containerButton.setEnabled(false);
			mainWindow.imapRadioButton.setEnabled(true);
			mainWindow.imapsRadioButton.setEnabled(true);
			mainWindow.serverLabel.setEnabled(true);
			mainWindow.serverField.setEnabled(true);
			mainWindow.userLabel.setEnabled(true);
			mainWindow.userField.setEnabled(true);
			mainWindow.passwordLabel.setEnabled(true);
			mainWindow.passwordField.setEnabled(true);
		} else if (command.equals("container")) {
			String filename = selectPath(false);
			if (filename != null)
				mainWindow.containerField.setText(filename);
		} else if (command.equals("savedir")) {
			String dirname = selectPath(true);
			if (dirname != null)
				mainWindow.savedirField.setText(dirname);
		} else if (command.equals("list"))
			doAction(LIST_ACTION);
		else if (command.equals("stat"))
			doAction(STAT_ACTION);
		else if (command.equals("extract"))
			doAction(EXTRACT_ACTION);
	}

	private String selectPath(boolean fileBool) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser
				.setFileSelectionMode((fileBool ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES));
		fileChooser.setFileHidingEnabled(false);
		int returnVal = fileChooser.showOpenDialog(this.mainWindow);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			return (file.getAbsolutePath());
		} else
			return null;
	}

	static final int LIST_ACTION = 1;
	static final int STAT_ACTION = 2;
	static final int EXTRACT_ACTION = 3;

	void doAction(int actionNumber) {
		parseParams();

		new MailExtractThread(actionNumber, protocol, server, user, password, container, folder, destRootPath, destName,
				storeExtractorOptions, logLevel).start();
	}

	void parseParams() {
		destRootPath = "";
		destName = "";
		protocol = "";
		server = "localhost";
		user = "";
		password = "";
		container = "";
		folder = "";
		storeExtractorOptions = 0;
		local = true;

		// local
		if (mainWindow.localRadioButton.isSelected()) {
			if (mainWindow.thunderbirdRadioButton.isSelected())
				protocol = "thundermbox";
			else
				protocol = "libpst";
			container = mainWindow.containerField.getText();
		}
		// server
		else {
			if (mainWindow.imapsRadioButton.isSelected())
				protocol = "imaps";
			else
				protocol = "imap";
			server = mainWindow.serverField.getText();
			user = mainWindow.userField.getText();
			password = mainWindow.passwordField.getText();
		}
		folder = mainWindow.folderField.getText();
		destRootPath = mainWindow.savedirField.getText();
		destName = mainWindow.nameField.getText();

		if (mainWindow.keeponlydeepCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS;
		}
		if (mainWindow.dropemptyfoldersCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_DROP_EMPTY_FOLDERS;
		}
		if (mainWindow.warningCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_WARNING_MSG_PROBLEM;
		}
		storeExtractorOptions |= StoreExtractor.CONST_NAMES_SHORTENED;
		logLevel = (String) mainWindow.loglevelComboBox.getSelectedItem();
	}

	// generate a specific logger at the loglevel defined in constructor
	private static Logger generateLogger(String fileName, Level logLevel) throws Exception {
		Logger logger;
		try {
			Properties props = System.getProperties();
			props.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %4$s: %5$s%n");
			logger = Logger.getLogger(MailExtractApp.class.getName());
			logger.setLevel(logLevel);

			Formatter simpleFormatter;
			simpleFormatter = new SimpleFormatter();

			if (logLevel != Level.OFF) {
				Handler fileHandler = new FileHandler(fileName);
				fileHandler.setFormatter(simpleFormatter);
				fileHandler.setLevel(logLevel);
				logger.addHandler(fileHandler);
			}

			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(simpleFormatter);
			consoleHandler.setLevel(logLevel);
			if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
				consoleHandler.setEncoding("Cp850");
			logger.addHandler(consoleHandler);

			// don't use ConsoleHandler at global level
			logger.setUseParentHandlers(false);
		} catch (IOException e) {
			throw new Exception("mailextract: Can't create logger");
		}
		return logger;
	}

	// try if possible to log in the store extractor logger all the information
	// about the fatal error
	final static void logFatalError(Exception e, StoreExtractor storeExtractor, Logger logger) {
		logger.severe("Terminated with unrecoverable error");
		if (!e.getMessage().isEmpty())
			logger.severe(e.getMessage());
		logger.severe(getPrintStackTrace(e));
		if (storeExtractor == null
				|| storeExtractor.getFolderTotalCount() + storeExtractor.getTotalMessagesCount() == 0)
			logger.severe("No writing done");
		else
			logger.severe("Partial writing done " + Integer.toString(storeExtractor.getFolderTotalCount())
					+ " folders and " + Integer.toString(storeExtractor.getTotalMessagesCount()) + " messages");

	}

	// make a String from the stack trace
	final private static String getPrintStackTrace(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter p = new PrintWriter(baos);

		e.printStackTrace(p);
		p.close();
		return baos.toString();
	}

	static MailExtractApp theApp;

	public static MailExtractApp getTheApp() {
		return theApp;
	}

	// define the jopt option parser
	final static private OptionParser createOptionParser() {
		OptionParser parser;

		parser = new OptionParser();
		parser.accepts("help").forHelp();
		parser.accepts("mailprotocol", "mail protocol for server access (imap|imaps...)").withRequiredArg();
		parser.accepts("thunderbird", "thunderbird mbox directory");
		parser.accepts("outlook", "outlook pst file");
		parser.accepts("user", "user account name (also used for destination extraction naming)").withRequiredArg();
		parser.accepts("password", "password").withRequiredArg();
		parser.accepts("server", "mail server [HostName|IP](:port)").withRequiredArg();
		parser.accepts("container", "mail container directory for mbox or file for pst").withRequiredArg();
		parser.accepts("folder", "specific mail folder").withRequiredArg();
		parser.accepts("rootdir", "root (default current directory) for output to root/username directory")
				.withRequiredArg();
		parser.accepts("dropemptyfolders", "drop empty folders");
		parser.accepts("keeponlydeep", "keep only empty folders not at root level");
		parser.accepts("verbatim", "event level to log (OFF|SEVERE|WARNING|INFO|FINE|FINER|FINEST)").withRequiredArg();
		parser.accepts("namesshortened", "generate short directories and files names");
		parser.accepts("warning",
				"generate warning when there's a problem on a message (otherwise log at FINEST level)");
		parser.accepts("x", "extract account");
		parser.accepts("l", "access account and list folders (no drop options)");
		parser.accepts("z", "access account and list folders and there statistics (no drop options)");

		return parser;
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		// params
		String destRootPath = "", destName = "";
		String protocol = "", server = "localhost", user = "", password = "", container = "", folder = "";
		int storeExtractorOptions = 0;
		boolean local = true;

		String logLevel;

		// prepare parsing with jopt
		OptionParser parser = createOptionParser();
		OptionSet options = null;

		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.err.println("wrong arguments to know syntax use -h or --help option");
			System.exit(1);
		}

		// help
		try {
			if (options.has("help")) {
				parser.printHelpOn(System.out);
				System.exit(0);
			}
		} catch (Exception e) {
			System.exit(0);

		}

		// non specific option parsing
		if (options.has("verbatim"))
			logLevel = (String) options.valueOf("verbatim");
		else if (options.has("l") || options.has("z"))
			logLevel = "OFF";
		else
			logLevel = "INFO";
		try {
			Level.parse(logLevel);
		} catch (IllegalArgumentException iae) {
			System.err.println("unknown log level");
			System.exit(1);
		}

		if (options.has("keeponlydeep")) {
			storeExtractorOptions |= StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS;
		}
		if (options.has("dropemptyfolders")) {
			storeExtractorOptions |= StoreExtractor.CONST_DROP_EMPTY_FOLDERS;
		}
		if (options.has("warning")) {
			storeExtractorOptions |= StoreExtractor.CONST_WARNING_MSG_PROBLEM;
		}
		if (options.has("namesshortened")) {
			storeExtractorOptions |= StoreExtractor.CONST_NAMES_SHORTENED;
		}

		// specific option parsing for imap protocol extraction
		if (options.has("mailprotocol")) {
			if (!options.has("user")) {
				System.err.println("need a username for distant access protocol");
				System.exit(1);
			}
			if (!options.has("server")) {
				System.err.println("need a server (hostname or ip) for distant access protocol");
				System.exit(1);
			}
			if (options.has("container")) {
				System.err.println("no container for distant access protocol");
				System.exit(1);
			}
			protocol = (String) options.valueOf("mailprotocol");
			local = false;
		}
		// specific option parsing for thunderbird mbox directory extraction
		else if (options.has("thunderbird")) {
			if (options.has("password")) {
				System.out.println("no password for thunderbird mbox directory");
				System.exit(1);
			}
			if (!options.has("container")) {
				System.out.println("need a container pointing to thunderbird mbox directory");
				System.exit(1);
			}
			protocol = "thundermbox";
			local = true;
		}
		// specific option parsing for outlook pst file extraction
		else if (options.has("outlook")) {
			if (options.has("server")) {
				System.err.println("no server for outlook pst file extraction");
				System.exit(1);
			}
			if (!options.has("container")) {
				System.out.println("need a container pointing to outlook pst file");
				System.exit(1);
			}
			protocol = "libpst";
			local = true;
		}

		// collect or construct all store extractor variables
		user = (String) options.valueOf("user");
		password = (String) options.valueOf("password");
		server = (String) options.valueOf("server");
		container = (String) options.valueOf("container");
		folder = (String) options.valueOf("folder");

		if (user == null)
			user = "";
		if (password == null)
			password = "";
		if (server == null)
			server = "";
		if (container == null)
			container = "";
		if (folder == null)
			folder = "";

		if (options.has("rootdir"))
			destRootPath = (String) options.valueOf("rootdir");
		else
			destRootPath = System.getProperty("user.dir");

		// if no do option graphic version
		if (!options.has("l") && !options.has("z") && !options.has("x"))
			theApp = new MailExtractApp(protocol, server, user, password, container, folder, destRootPath, destName,
					storeExtractorOptions, local);
		else {
			StoreExtractor storeExtractor = null;

			if (protocol.isEmpty()) {
				System.err.println(
						"only imap protocols, thunderbird mbox directory and outlook pst file extraction available");
				System.exit(1);
			}

			// do the job, creating a store extractor and running the extraction
			try {
				logger = generateLogger(destRootPath + File.separator + destName + ".log", Level.parse(logLevel));
				if (user == null || user.isEmpty())
					destName = "unknown_extract";
				else
					destName = user;

				storeExtractor = StoreExtractor.createStoreExtractor(protocol, server, user, password, container,
						folder, destRootPath, destName, storeExtractorOptions, logger);
				if (options.has("l") || options.has("z")) {
					storeExtractor.listAllFolders(options.has("z"));
				} else {
					storeExtractor.extractAllFolders();
				}
			} catch (Exception e) {
				logFatalError(e, storeExtractor, logger);
				System.exit(1);
			}
		}
	}

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainWindow.consoleTextArea.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		// System.setErr(new PrintStream(out, true));
	}
}
