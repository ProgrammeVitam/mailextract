package fr.gouv.vitam.tools.mailextract.app;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;

public class MailExtractThread extends Thread {

	int actionNumber;
	StoreExtractor storeExtractor;
	Logger logger;

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

			Handler consoleHandler = new StdoutConsoleHandler();
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

	public MailExtractThread(int actionNumber, String protocol, String server, String user, String password,
			String container, String folder, String destRootPath, String destName, int storeExtractorOptions, String logLevel) {

		Logger logger = null;

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
			this.logger = logger;
		} catch (Exception e) {
			this.actionNumber = 0;
			System.out.println(e.getMessage());
			;
		}
	}

	public void run() {
		try {
			switch (actionNumber) {
			case MailExtractApp.LIST_ACTION:
				storeExtractor.listAllFolders(false);
				break;
			case MailExtractApp.STAT_ACTION:
				storeExtractor.listAllFolders(true);
				break;
			case MailExtractApp.EXTRACT_ACTION:
				if (storeExtractor.hasDestName())
					storeExtractor.extractAllFolders();
				else
					throw new ExtractionException("mailextract: no destination name for extraction");
				break;
			}
			Handler[] handler = logger.getHandlers();
			for (Handler h : handler) {
				h.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			;
		}
	}

}
