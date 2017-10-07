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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;

/**
 * MailExtractThread class for the real extraction command
 * <p>
 * Thread is needed for result to be seen in the console text area. It's then
 * possible to launch multiple extraction in parallel
 */
public class MailExtractThread extends Thread {

	/** The action number. */
	private int actionNumber;

	/** The store extractor. */
	private StoreExtractor storeExtractor;

	/** The logger. */
	private Logger logger;

	// generate a specific logger at the loglevel defined in constructor and
	// sending to stdout console instead of stderr console
	private Logger generateLogger(String fileName, Level logLevel) throws Exception {
		Logger logger;
		try {
			Properties props = System.getProperties();
			props.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %4$s: %5$s%n");
			logger = Logger.getLogger(Long.toString(this.getId()));
			logger.setLevel(logLevel);

			Formatter simpleFormatter;
			simpleFormatter = new SimpleFormatter();

			if (logLevel != Level.OFF) {
				Handler fileHandler = new FileHandler(fileName);
				fileHandler.setFormatter(simpleFormatter);
				fileHandler.setLevel(logLevel);
				logger.addHandler(fileHandler);
			}

			Handler consoleHandler = new StdoutConsoleHandler();
			consoleHandler.setFormatter(simpleFormatter);
			consoleHandler.setLevel(logLevel);
			logger.addHandler(consoleHandler);

			// don't use ConsoleHandler at global level
			logger.setUseParentHandlers(false);
		} catch (IOException e) {
			throw new Exception("mailextract: Can't create logger");
		}
		return logger;
	}

	/**
	 * Instantiates a new mail extract thread.
	 *
	 * @param actionNumber
	 *            the action number (LIST, STAT, EXTRACT)
	 * @param protocol
	 *            the protocol
	 * @param server
	 *            the server
	 * @param user
	 *            the user
	 * @param password
	 *            the password
	 * @param container
	 *            the container
	 * @param folder
	 *            the folder
	 * @param destRootPath
	 *            the dest root path
	 * @param destName
	 *            the dest name
	 * @param storeExtractorOptions
	 *            the store extractor options
	 * @param logLevel
	 *            the log level
	 */
	public MailExtractThread(int actionNumber, String protocol, String server, String user, String password,
			String container, String folder, String destRootPath, String destName, int storeExtractorOptions,
			String logLevel) {

		logger = null;

		try {
			logger = generateLogger(destRootPath + File.separator + destName + ".log", Level.parse(logLevel));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// do the job, creating a store extractor and running the extraction
		try {
			this.storeExtractor = StoreExtractor.createStoreExtractor(protocol, server, user, password, container,
					folder, destRootPath, destName, storeExtractorOptions, logger);
			this.actionNumber = actionNumber;
		} catch (Exception e) {
			this.actionNumber = 0;
			System.out.println(e.getMessage());
			;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			switch (actionNumber) {
			case MailExtractGraphicApp.LIST_ACTION:
				storeExtractor.listAllFolders(false);
				break;
			case MailExtractGraphicApp.STAT_ACTION:
				storeExtractor.listAllFolders(true);
				break;
			case MailExtractGraphicApp.EXTRACT_ACTION:
				if (storeExtractor.hasDestName())
					storeExtractor.extractAllFolders();
				else
					throw new ExtractionException("mailextract: no destination name for extraction");
				break;

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (logger != null) {
				Handler[] handler = logger.getHandlers();
				for (Handler h : handler) {
					h.close();
					logger.removeHandler(h);
				}
				logger = null;
			}
		}
	}

}