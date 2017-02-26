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
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fr.gouv.vitam.tools.mailextract.lib.core.DateRange;
import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * 
 * <p>
 * Main class of mailextract tool. It performs extraction and structure listing
 * of mail boxes from different sources:
 * <ul>
 * <li>IMAP or IMAPS server with user/password login</li>
 * <li>Thunderbird directory containing mbox files and .sbd directory
 * hierarchy</li>
 * <li>Outlook pst file</li>
 * </ul>
 * 
 * <p>
 * The extraction generate on disk a directories/files structure convenient for
 * SEDA archive packet (NF Z44-022). For detailed information see class
 * {@link StoreExtractor}.
 * 
 * <p>
 * The operation, extraction or listing, can be logged on console and file
 * (root/username[-timestamp].log - cf args). At the different levels you can
 * have: extraction errors (SEVERE), warning about extraction problems and items
 * dropped (WARNING), information about global process (INFO), list of treated
 * folders (FINE), list of treated messages (FINER), problems with some expected
 * metadata (FINEST). <br>
 * The default level of log is INFO for extracting and OFF for listing.
 * 
 * <p>
 * The arguments syntax is:
 * <table>
 * <tr>
 * <td>--help</td>
 * <td>help</td>
 * </tr>
 * <tr>
 * <td>--mailprotocol</td>
 * <td>mail protocol for server access (imap|imaps...)</td>
 * </tr>
 * <tr>
 * <td>--thunderbird</td>
 * <td>thunderbird mbox directory</td>
 * </tr>
 * <tr>
 * <td>--outlook</td>
 * <td>outlook pst file</td>
 * </tr>
 * <tr>
 * <td>--user</td>
 * <td>user account name (also used for destination extraction naming)</td>
 * </tr>
 * <tr>
 * <td>--password</td>
 * <td>password</td>
 * </tr>
 * <tr>
 * <td>--server</td>
 * <td>mail server [HostName|IP](:port)</td>
 * </tr>
 * <tr>
 * <td>--container</td>
 * <td>mail container directory for mbox or file for pst</td>
 * </tr>
 * <tr>
 * <td>--folder</td>
 * <td>specific mail folder used as root for extraction or listing</td>
 * </tr>
 * <tr>
 * <td>--rootdir</td>
 * <td>root (default current directory) for output to root/username
 * directory</td>
 * </tr>
 * <tr>
 * <td>--addtimestamp</td>
 * <td>add a timestamp to output directory (root/username-timestamp)</td>
 * </tr>
 * <tr>
 * <td>--dropemptyfolders</td>
 * <td>drop empty folders</td>
 * </tr>
 * <tr>
 * <td>--keeponlydeep</td>
 * <td>keep only empty folders not at root level</td>
 * </tr>
 * <tr>
 * <td>--loglevel</td>
 * <td>event level to log (SEVERE| WARNING| INFO| FINE| FINER| FINEST)</td>
 * </tr>
 * <tr>
 * <td>--namesshortened</td>
 * <td>generate short directories and files names</td>
 * </tr>
 * <tr>
 * <td>--warning</td>
 * <td>generate warning when there's a problem on a message (otherwise log at
 * FINEST level)</td>
 * </tr>
 * <tr>
 * <td>--xtract</td>
 * <td>generate extraction logs</td>
 * </tr>
 * <tr>
 * <td>-l</td>
 * <td>access account and list folders (no drop options)</td>
 * </tr>
 * <tr>
 * <td>-z</td>
 * <td>access account and list folders and there statistics (no drop
 * options)</td>
 * </tr>
 * <tr>
 * <td>--xml</td>
 * <td>extract metadata in xml rather than json</td>
 * </tr>
 * <tr>
 * <td>--gs</td>
 * <td>extract metadata permitted by generator_seda in required format</td>
 * </tr>
 * </table>
 * Long options can be reduced to short ones (for example -h is equivalent to
 * --help)
 *
 * <p>
 * <b>Warning:</b> Listing with detailed information is a potentially expensive
 * operation, especially when accessing distant account, as all messages are
 * inspected (in the case of a distant account that mean also downloaded...).
 * 
 * <p>
 * Note: For now it can't extract S/MIME (ciphered and/or signed) messages.
 * <p>
 * It implements the operating class {@link StoreExtractor}
 * 
 * @author JSL
 **/
public class MailExtract {

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
		parser.accepts("addtimestamp", "add a timestamp to output directory (root/username-timestamp)");
		parser.accepts("dropemptyfolders", "drop empty folders");
		parser.accepts("keeponlydeep", "keep only empty folders not at root level");
		parser.accepts("verbatim", "event level to log (SEVERE|WARNING|INFO|FINE|FINER|FINEST)").withRequiredArg();
		parser.accepts("namesshortened", "generate short directories and files names");
		parser.accepts("warning",
				"generate warning when there's a problem on a message (otherwise log at FINEST level)");
		parser.accepts("l", "access account and list folders (no drop options)");
		parser.accepts("z", "access account and list folders and there statistics (no drop options)");

		return parser;
	}

	/**
	 * The main method called from shell.
	 * <p>
	 * Exit with 0 if extraction performed without error, and with 1 if not
	 *
	 * @param args
	 *            - shell args
	 * @throws Exception
	 */
	final public static void main(String[] args) throws Exception {
		// prepare parsing with jopt
		OptionParser parser = createOptionParser();
		OptionSet options = null;

		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.err.println("wrong arguments to know syntax use -h or --help option");
			System.exit(1);
		}

        // do option parsing
		String destRootPath = "", destName = "";
		String protocol = "", server = "localhost", user = "", password = "", container = "", folder = "";
		int storeExtractorOptions = 0;
		String logLevel;

		// help
		if (options.has("help")) {
			parser.printHelpOn(System.out);
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
		} else {
			System.err.println(
					"only imap protocol, thunderbird mbox directory and outlook pst file extraction available");
			System.exit(1);
		}

		// collect or construct all store extractor variables
		user = (String) options.valueOf("user");
		password = (String) options.valueOf("password");
		server = (String) options.valueOf("server");
		container = (String) options.valueOf("container");
		folder = (String) options.valueOf("folder");

		if (options.has("rootdir"))
			destRootPath = (String) options.valueOf("rootdir");
		else
			destRootPath = System.getProperty("user.dir");

		if (user == null || user.isEmpty())
			destName = "unknown_extract";
		else
			destName = user;
		if (options.has("addtimestamp")) {
			Date date = new Date();
			destName += "_" + DateRange.getISODateString(date).replaceAll("[^\\p{IsAlphabetic}\\p{Digit}]", "_");
		}

		StoreExtractor storeExtractor = null;
		Logger logger = generateLogger(destRootPath + File.separator + destName + ".log", Level.parse(logLevel));

		// do the job, creating a store extractor and running the extraction
		try {
			storeExtractor = StoreExtractor.createStoreExtractor(protocol, server, user, password, container, folder,
					destRootPath, destName, storeExtractorOptions, logger);
			if (options.has("l") || options.has("z"))
				storeExtractor.listAllFolders(options.has("z"));
			else
				storeExtractor.extractAllFolders();
		} catch (Exception e) {
			logFatalError(e, storeExtractor, logger);
			System.exit(1);
		}
	}

	// generate a specific logger at the loglevel defined in constructor
	private static Logger generateLogger(String fileName, Level logLevel) throws ExtractionException {
		Logger logger;
		try {
			Properties props = System.getProperties();
			props.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %4$s: %5$s%n");
			logger = Logger.getLogger(MailExtract.class.getName());
			logger.setLevel(logLevel);

			Formatter simpleFormatter;
			simpleFormatter = new SimpleFormatter();

			Handler fileHandler = new FileHandler(fileName);
			fileHandler.setFormatter(simpleFormatter);
			fileHandler.setLevel(logLevel);
			logger.addHandler(fileHandler);

			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(simpleFormatter);
			consoleHandler.setLevel(logLevel);
			if (System.getProperty("os.name").toLowerCase().startsWith("windows"))
				consoleHandler.setEncoding("Cp850");
			logger.addHandler(consoleHandler);

			// don't use ConsoleHandler at global level
			logger.setUseParentHandlers(false);
		} catch (IOException e) {
			throw new ExtractionException("mailextract: Can't create logger");
		}
		return logger;
	}

	// try if possible to log in the store extractor logger all the information
	// about the fatal error
	final private static void logFatalError(Exception e, StoreExtractor storeExtractor, Logger logger) {
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
}
