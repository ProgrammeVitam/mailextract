/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 * 
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.mailextract.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * MailExtractApp class for launching the command or the graphic application.
 */
public class MailExtractApp {

	static private Logger logger;

	// define the jopt option parser
	private final static OptionParser createOptionParser() {
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

	/**
	 * The main method for both command and graphic version.
	 *
	 * @param args
	 *            the arguments
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws UnsupportedLookAndFeelException
	 *             the unsupported look and feel exception
	 */
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
		else if (options.has("l") || options.has("z") || (!options.has("l") && !options.has("z") && !options.has("x")))
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
			new MailExtractGraphicApp(protocol, server, user, password, container, folder, destRootPath, destName,
					storeExtractorOptions, logLevel, local);
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

	// generate a specific logger at the loglevel defined in constructor
	private final static Logger generateLogger(String fileName, Level logLevel) throws Exception {
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
	private final static void logFatalError(Exception e, StoreExtractor storeExtractor, Logger logger) {
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
	private final static String getPrintStackTrace(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter p = new PrintWriter(baos);

		e.printStackTrace(p);
		p.close();
		return baos.toString();
	}

}
