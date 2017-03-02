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

package fr.gouv.vitam.tools.mailextract.lib.core;

import fr.gouv.vitam.tools.mailextract.lib.javamail.JMStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.libpst.LPStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

import java.io.File;
//import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

//import org.apache.tika.Tika;
//import org.apache.tika.exception.TikaException;

/**
 * Abstract factory class for operation context on a defined mailbox.
 * 
 * <p>
 * The {@link #createStoreExtractor createStoreExtractor} call create a
 * StoreExtractor subclass convenient for the declared protocol. This creation
 * specify the target of extraction, the different options of extraction, or
 * listing, and the log level. It makes a first level of connection and
 * compliance check to warranty other methods calls a viable mail box access.
 * 
 * The StoreExtractor object has the context and links used to perform
 * extraction or structure listing of mail boxes from different sources:
 * <ul>
 * <li>IMAP server with user/password login</li>
 * <li>Thunderbird directory containing mbox files and .sbd directory
 * hierarchy</li>
 * <li>Outlook pst file</li>
 * </ul>
 * 
 * <p>
 * The extraction generate on disk a directories/files structure convenient for
 * SEDA archive packet (NF Z44-022), which is:
 * <ul>
 * <li>each folder is extracted as a directory named "Folder#'UniqID': 'name'",
 * with uniqID being a unique ID innerly generated and name being the first 32
 * characters of the folder name UTF-8 encoded. The folder descriptive metadata
 * is in the file named manifest.json (json/UTF-8 encoded) in its directory. It
 * represent the folder ArchiveUnit with no objects</li>
 * <li>each message is extracted in a directory named "Message#'UniqID':
 * 'name'", with name being the first 32 characters of the folder name UTF-8
 * encoded. It represent the message ArchiveUnit with no objects. In this
 * message directory, there's:
 * <ul>
 * <li>the message descriptive metadata file (manifest.json),</li>
 * <li>one directory by attachment (if any) named "__Attachment#'UniqID':
 * 'name'__" with the name being the firts 32 characters of the attachment
 * filename and the starting and ending "__" indicating that this folder
 * represent an ArchiveUnit with objects. In this folder are :</li>
 * <ul>
 * <li>the attachment descriptive metadata file (manifest.json), and</li>
 * <li>the attachment binary file, being a final object, is named according the
 * format "'ObjectType'-'Version'-'filename'", with in this case ObjectType
 * being "BinaryMaster", version being "1", filename being the attachment file
 * name with extension if any.</li>
 * </ul>
 * <li>one directory for the message body named "__Body__" with the starting and
 * ending "__" indicating that this folder represent an ArchiveUnit with
 * objects. In this folder are :</li>
 * <ul>
 * <li>the message object descriptive metadata file (manifest.json),
 * <li>the message body binary file, being a final object, named
 * "BinaryMaster_1_object" (no filename), and</li>
 * <li>the message body text content, being a final object, named
 * "TextContent_1_object" (no filename).</li>
 * </ul>
 * </ul>
 * </ul>
 * Note: when CONST_NAMES_SHORTENED option is enabled, during the name
 * generation the directory type is reduced to it's first character (Folder to
 * F, Message to M...) and not 32 but only 8 characters from objects names are
 * used
 * <p>
 * For detailed information on descritptive metadata collected see
 * {@link MailBoxFolder} and {@link MailBoxMessage}.
 * <p>
 * The extraction or listing operation is logged on console and file
 * (root/username[-timestamp].log - cf args). At the different levels (using
 * {@link java.util.logging.Logger}) you can have: extraction errors (SEVERE),
 * warning about extraction problems and items dropped (WARNING), information
 * about global process (INFO), list of treated folders (FINE), list of treated
 * messages (FINER), problems with some expected metadata (FINEST).
 * 
 * <p>
 * Note: Metadata values are, for now, hardcoded in french
 */
public abstract class StoreExtractor {

	/** Protocol used for extraction (imap| thundermbox| pstfile) */
	protected String protocol;
	/** Server of target account ((hostname|ip)[:port]) **/
	protected String server;
	/** User account name **/
	protected String user;
	/** Password, can be null if not used **/
	protected String password;
	/**
	 * Path to the local extraction target (Thunderbird or Outlook), can be null
	 * of not used
	 **/
	protected String container;
	/**
	 * Path of the extracted folder in the account mail box, can be null if
	 * default root folder
	 **/
	protected String folder;

	/** Path and name of the extraction directory */
	protected String destRootPath, destName;
	/** Extractor options, flags coded on an int, defined thru constants **/
	protected int options;

	/** Root folder for extraction (can be different form account root) **/
	protected MailBoxFolder rootAnalysisMBFolder;

	// private fields for global statictics
	private int totalMessagesCount;
	private int totalAttachedMessagesCount;
	private int totalFoldersCount;
	private long totalRawSize;

	// private logger
	private Logger logger;

	/**
	 * Instantiates a new store extractor.
	 *
	 * @param protocol
	 *            Protocol used for extraction (imap| thundermbox| pstfile)
	 * @param server
	 *            Server of target account ((hostname|ip)[:port
	 * @param user
	 *            User account name
	 * @param password
	 *            Password, can be null if not used
	 * @param container
	 *            Path to the local extraction target (Thunderbird or Outlook
	 * @param folder
	 *            Path of the extracted folder in the account mail box, can be
	 *            null if default root folder
	 * @param destRootPath
	 *            Root path of the extraction directory
	 * @param destName
	 *            Name of the extraction directory
	 * @param options
	 *            Extractor options
	 * @param logger
	 *            Logger used (from {@link java.util.logging.Logger})
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected StoreExtractor(String protocol, String server, String user, String password, String container,
			String folder, String destRootPath, String destName, int options, Logger logger)
			throws ExtractionException {

		this.protocol = protocol;
		this.server = server;
		this.user = user;
		this.password = password;
		this.container = container;
		this.folder = folder;
		this.destRootPath = destRootPath;
		this.destName = destName;
		this.options = options;

		this.totalFoldersCount = 0;
		this.totalAttachedMessagesCount = 0;
		this.totalMessagesCount = 0;
		this.totalRawSize = 0;

		this.logger = logger;

		// this.tika = new Tika();
	}

	/** KEEP_ONLY_DEEP_EMPTY_FOLDERS option constant */
	final static public int CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS = 1;

	/** DROP_EMPTY_FOLDERS option constant */
	final static public int CONST_DROP_EMPTY_FOLDERS = 2;

	/** WARNING_MSG_PROBLEMS option constant */
	final static public int CONST_WARNING_MSG_PROBLEM = 4;

	/** NAMES_SHORTENED option constant */
	final static public int CONST_NAMES_SHORTENED = 8;

	/** EXTRACTION option constant */
	final static public int CONST_EXTRACTION = 128;

	// Write log identifying the mail box target, and options
	private void writeTargetLog() {
		getLogger().info("Target mail box with protocol=" + protocol
				+ (server == null || server.isEmpty() ? "" : "  server=" + server)
				+ (user == null || user.isEmpty() ? "" : " user=" + user)
				+ (password == null || password.isEmpty() ? "" : " password=" + password)
				+ (container == null || container.isEmpty() ? "" : " container=" + container)
				+ (folder == null || folder.isEmpty() ? "" : " folder=" + folder));
		getLogger().info("to directory " + destRootPath + File.separator + destName);

		boolean first = true;
		String optionsLog = "";
		if (hasOptions(CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS)) {
			optionsLog += "keeping all empty folders except root level ones";
			first = false;
		}
		if (hasOptions(CONST_DROP_EMPTY_FOLDERS)) {
			if (!first)
				optionsLog += ", ";
			optionsLog += "droping all empty folders";
			first = false;
		}
		if (hasOptions(CONST_WARNING_MSG_PROBLEM)) {
			if (!first)
				optionsLog += ", ";
			optionsLog += "generate warning when there's a problem on a message (otherwise log at FINEST level)";
			first = false;
		}
		if (hasOptions(CONST_NAMES_SHORTENED)) {
			if (!first)
				optionsLog += ", ";
			optionsLog += "with shortened names";
			first = false;
		}
		if (!first)
			optionsLog += ", ";
		optionsLog += "with log level " + getLogger().getLevel();

		getLogger().info(optionsLog);
	}

	/**
	 * Gets the logger created during the store extractor construction, and used
	 * in all mailextract classes.
	 * 
	 * <p>
	 * For convenience each class which may have some log actions has it's own
	 * getLogger method always returning this store extractor logger.
	 *
	 * @return logger
	 */
	public Logger getLogger() {
		return logger;
	}

	private int uniqID = 1;

	
	/**
	 * Checks for dest name.
	 *
	 * @return true, if successful
	 */
	public boolean hasDestName(){
		return !((destName==null) || destName.isEmpty());
	}
	
	/**
	 * Gets a uniq ID in store extractor context.
	 * <p>
	 * Sequence incremented at each call
	 *
	 * @return a uniq ID
	 */
	public int getUniqID() {
		return uniqID++;
	}

	/**
	 * Increment the messages total count.
	 */
	public void addTotalMessagesCount(int count) {
		totalMessagesCount += count;
	}

	/**
	 * Gets the total count of all analyzed messages.
	 *
	 * @return the message count
	 */
	public int getTotalMessagesCount() {
		return totalMessagesCount;
	}

	/**
	 * Increment the attached messages total count.
	 */
	public void addTotalAttachedMessagesCount(int count) {
		totalAttachedMessagesCount += count;
	}

	/**
	 * Gets the total count of all analyzed attached messages.
	 *
	 * @return the message count
	 */
	public int getTotalAttachedMessagesCount() {
		return totalAttachedMessagesCount;
	}

	/**
	 * Increment the folders total count.
	 */
	public void incTotalFoldersCount() {
		totalFoldersCount++;
	}

	/**
	 * Gets the total count of all analyzed folders.
	 * 
	 * @return the folder total count
	 */
	public int getFolderTotalCount() {
		return totalFoldersCount;
	}

	/**
	 * Add to total raw size.
	 */
	public void addTotalRawSize(long messageSize) {
		totalRawSize += messageSize;
	}

	/**
	 * Gets the total raw size of all analyzed messages.
	 * <p>
	 * The "raw" size is the sum of the size of messages as in the mailbox, the
	 * extraction will be larger (up to x2)
	 *
	 * @return the total raw size
	 */
	public long getTotalRawSize() {
		return totalRawSize;
	}

	/**
	 * Checks for options.
	 *
	 * @param flags
	 *            constant values (CONST_...) |/+ composition
	 * @return true, if successful
	 */
	public boolean hasOptions(int flags) {
		return (options & flags) != 0;
	}

	/**
	 * Create a store extractor as a factory creator.
	 *
	 * @param protocol
	 *            Protocol used for extraction (thundermbox| pstfiles| imap|
	 *            imaps[not tested gimap| pop3])
	 * @param server
	 *            Server of target account ((hostname|ip)[:port
	 * @param user
	 *            User account name
	 * @param password
	 *            Password, can be null if not used
	 * @param container
	 *            Path to the local extraction target (Thunderbird or Outlook
	 * @param folder
	 *            Path of the extracted folder in the account mail box, can be
	 *            null if default root folder
	 * @param destRootPath
	 *            Root path of the extraction directory
	 * @param destName
	 *            Name of the extraction directory
	 * @param options
	 *            Options (flag composition of CONST_)
	 * @param logger
	 *            Logger used (from {@link java.util.logging.Logger})
	 * @return the store extractor, constructed as a non abstract subclass
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public static StoreExtractor createStoreExtractor(String protocol, String server, String user, String password,
			String container, String folder, String destRootPath, String destName, int options, Logger logger)
			throws ExtractionException {

		if (protocol.equals("libpst"))
			return new LPStoreExtractor(protocol, server, user, password, container, folder, destRootPath, destName,
					options, logger);
		else
			return new JMStoreExtractor(protocol, server, user, password, container, folder, destRootPath, destName,
					options, logger);
	}

	/**
	 * Extract all folders from the defined root folder (considering drop
	 * options).
	 * 
	 * <p>
	 * This is a method where the extraction structure and content is partially
	 * defined (see also {@link MailBoxMessage#extractMessage extractMessage}
	 * and {@link MailBoxFolder#extractFolder extractFolder}).
	 * 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void extractAllFolders() throws ExtractionException {
		Instant start = Instant.now();

		writeTargetLog();
		getLogger().info("Extraction processed");

		rootAnalysisMBFolder.extractFolderAsRoot();

		ArchiveUnit rootNode = rootAnalysisMBFolder.getArchiveUnit();
		rootNode.addMetadata("DescriptionLevel", "RecordGrp", true);
		rootNode.addMetadata("Title",
				"Ensemble des messages électroniques envoyés et reçus par le titulaire du compte " + user
						+ " sur le serveur " + server + " à la date du " + start,
				true);
		if (rootAnalysisMBFolder.dateRange.isDefined()) {
			rootNode.addMetadata("StartDate", DateRange.getISODateString(rootAnalysisMBFolder.dateRange.getStart()),
					true);
			rootNode.addMetadata("EndDate", DateRange.getISODateString(rootAnalysisMBFolder.dateRange.getEnd()), true);
		}
		rootNode.write();

		Instant end = Instant.now();
		getLogger().info("Terminated in " + Duration.between(start, end).toString() + " writing "
				+ Integer.toString(getFolderTotalCount()) + " folders and " + Integer.toString(getTotalMessagesCount())
				+ " messages, for a total size of " + Long.toString(getTotalRawSize())+" and "
						+ Integer.toString(getTotalAttachedMessagesCount()) + " attached message");

	}

	/**
	 * List all folders from the defined root folder (no drop options).
	 * 
	 * <p>
	 * Warning: listing with detailed information is a potentially expensive
	 * operation, especially when accessing distant account, as all messages are
	 * inspected (in the case of a distant account that mean also
	 * downloaded...).
	 *
	 * @param stats
	 *            true if detailed information (number and raw size of messages
	 *            in each folder) is asked for
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void listAllFolders(boolean stats) throws ExtractionException {
		Instant start = Instant.now();

		writeTargetLog();
		getLogger().info("Listing processed");

		rootAnalysisMBFolder.listFolder(stats);

		Instant end = Instant.now();
		System.out.println("--------------------------------------------------------------------------------");

		if (stats) {
			String size = Double.toString(Math.round(((double) getTotalRawSize()) * 100.0 / (1024.0 * 1024.0)) / 100.0);
			System.out.println("Terminated in " + Duration.between(start, end).toString() + " listing "
					+ Integer.toString(getFolderTotalCount()) + " folders with "
					+ Integer.toString(getTotalMessagesCount()) + " messages, for " + size + " MBytes, and "
					+ Integer.toString(getTotalAttachedMessagesCount()) + " attached message");
			getLogger().info("Terminated in " + Duration.between(start, end).toString() + " listing "
					+ Integer.toString(getFolderTotalCount()) + " folders with "
					+ Integer.toString(getTotalMessagesCount()) + " messages, for " + size + " MBytes, and "
					+ Integer.toString(getTotalAttachedMessagesCount()) + " attached message");
		} else {
			System.out.println("Terminated in " + Duration.between(start, end).toString() + " listing "
					+ Integer.toString(getFolderTotalCount()) + " folders");
			getLogger().info("Terminated in " + Duration.between(start, end).toString() + " listing "
					+ Integer.toString(getFolderTotalCount()) + " folders");
		}

	}

}
