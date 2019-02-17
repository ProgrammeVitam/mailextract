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

import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.javamail.JMStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.msg.MsgStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst.PstStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst.embeddedmsg.PstEmbeddedStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.utils.DateRange;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.utils.MailExtractProgressLogger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import javax.mail.URLName;

import java.io.PrintStream;

import static fr.gouv.vitam.tools.mailextract.lib.utils.MailExtractProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.mailextract.lib.utils.MailExtractProgressLogger.MESSAGE;

/**
 * Abstract factory class for operation context on a defined mailbox or mail
 * file.
 * 
 * <p>
 * The {@link #createStoreExtractor createStoreExtractor} call create a
 * StoreExtractor subclass convenient for the declared protocol. This creation
 * specify the target of extraction, the different options of extraction, or
 * listing, and the log level. It makes a first level of connection and
 * compliance check to warranty other methods calls a viable mail box access.
 * 
 * The StoreExtractor use sub class extractors to manage different protocols or
 * formats (called schemes). There are defaults sub class for:
 * <ul>
 * <li>IMAP/IMAPS/POP3 (GIMAP experimental) server with user/password login</li>
 * <li>Thunderbird directory containing mbox files and .sbd directory
 * hierarchy</li>
 * <li>mbox file</li>
 * <li>eml file</li>
 * <li>Outlook pst file</li>
 * <li>Msg file</li>
 * </ul>
 * Notice: you have to call {@link #initDefaultExtractors initDefaultExtractors}
 * method before any usage to benefit from these existing extractors, and you
 * can add new ones with {@link #addExtractionRelation addExtractionRelation},
 * and they will be automatically used when needed.
 * 
 * <p>
 * The extraction generate on disk a directories/files structure convenient for
 * SEDA archive packet (NF Z44-022), which is:
 * <ul>
 * <li>each folder is extracted as a directory named "Folder#'UniqID': 'name'",
 * with uniqID being a unique ID innerly generated and name being the n first
 * characters of the folder name UTF-8 encoded (n is defined by the option
 * namesLength, default being 12). The folder descriptive metadata is in the
 * file named ArchiveUnitContent.xml or, in V2 model, __ArchiveUnitMetadata (XML/UTF-8 encoded) in its directory. It
 * represent the folder ArchiveUnit with no objects</li>
 * <li>each message is extracted in a directory named "Message#'UniqID':
 * 'name'", with name being the n first characters of the message title UTF-8
 * encoded. It represent the message ArchiveUnit. In this message directory,
 * there's:
 * <ul>
 * <li>the message descriptive metadata file (ArchiveUnitContent.xml or,
 * in V2 model, __ArchiveUnitMetadata),</li>
 * <li>one directory for each special attachments being extractible stores
 * (attached messages or attached files formatted in eml, msg, pst...) (if any)
 * <ul>
 * <li>if simple message, it's named "Message#'UniqID': 'name'", with name being
 * the n first characters of the message title UTF-8 encoded, and contents the
 * message extraction,</li>
 * <li>if complex extraction, it's named "Container#'UniqID': 'name'", with name
 * being the n first characters of the attachment name (if non default to
 * 'infile') UTF-8 encoded, and contents the container descriptive metadata file
 * (ArchiveUnitContent.xml or, in V2 model, __ArchiveUnitMetadata) and
 * the hierarchy of extracted folders, messages, attachments...</li>
 * </ul>
 * <li>one directory by attachment (if any) named "Attachment#'UniqID': 'name'"
 * with the name being the n first characters of the attachment filename. In
 * this folder are :</li>
 * <ul>
 * <li>the attachment descriptive metadata file (ArchiveUnitContent.xml
 * or, in V2 model, __ArchiveUnitMetadata),
 * and</li>
 * <li>the attachment binary file, being a final object, named according the
 * format "__'ObjectType'_'Version'_'filename'" or, in V2 model,
 * "__'ObjectType'_'Version'__'filename'", with in this case ObjectType
 * being "BinaryMaster", version being "1", filename being the attachment file
 * name with extension if any.</li>
 * <li>the text version of attachment binary file, if option is set, named
 * according the format "__'ObjectType'_'Version'_'filename'" or, in V2 model,
 *  * "__'ObjectType'_'Version'__'filename'", with in this case
 * ObjectType being "TextContent", version being "1", filename being the
 * attachment file name with extension if any.</li>
 * </ul>
 * <li>the message body in eml format file, being a final object, named
 * "__BinaryMaster_1_'messageID'", or in V2 model, "__BinaryMaster_1__'messageID'"
 * with the messageID being the n first characters
 * of the uniq messageID , and</li>
 * <li>the message body in text format file, if option is set, being a final
 * object, named "__TextContent_1_'messageID'", or in V2 model,
 * "__TextContent_1__'messageID'" with the messageID being the n
 * first characters of the uniq messageID.</li>
 * </ul>
 * </ul>
 * Note: any folder's name is modified with starting and ending "__" when the
 * ArchiveUnit, the folder represents, contains an object.
 * <p>
 * For detailed information on descritptive metadata collected see
 * {@link StoreFolder} and {@link StoreMessage}.
 * <p>
 * The extraction or listing operation is logged on console and file
 * (root/username[-timestamp].log - cf args). At the different levels (using
 * {@link MailExtractProgressLogger}) you can have: information
 * about global process and extraction errors (GLOBAL),
 * warning about extraction problems and items dropped (WARNING), list of
 * treated folders (FOLDER), accumulated count of treated messages (MESSAGE_GROUP),
 * list of treated messages (MESSAGE), problems with some expected metadata (MESSAGE_DETAILS).
 * <p>
 * It's also possible (ruled by option) to generate a csv file with one line by
 * extracted message with a selection of metadata, including appointment details
 * <p>
 * Note: Default or generated metadata values are, for now, hardcoded in french
 */
public abstract class StoreExtractor {

	// Map of StoreExtractor classes by scheme

	/** The map of mimetypes/scheme known relations. */
	static HashMap<String, String> mimeTypeSchemeMap = new HashMap<String, String>();

	/** The map of scheme/extractor class known relations. */
	@SuppressWarnings("rawtypes")
	static HashMap<String, Class> schemeStoreExtractorClassMap = new HashMap<String, Class>();

	/**
	 * The map of scheme/container extraction (vs single file extraction) known
	 * relations.
	 */
	static HashMap<String, Boolean> schemeContainerMap = new HashMap<String, Boolean>();

	/**
	 * Subscribes all defaults store extractor.
	 */
	public static void initDefaultExtractors() {
		JMStoreExtractor.subscribeStoreExtractor();
		MsgStoreExtractor.subscribeStoreExtractor();
		PstStoreExtractor.subscribeStoreExtractor();
		PstEmbeddedStoreExtractor.subscribeStoreExtractor();
	}

	// StoreExtractor definition parameters

	/**
	 * Scheme defining specific store extractor (imap| imaps| pop3| thunderbird|
	 * mbox| eml| pst| msg| experimental gimap)...)
	 */
	protected String scheme;

	/** Hostname of target store in ((hostname|ip)[:port]) *. */
	protected String host;

	/** Port of target store in ((hostname|ip)[:port]) *. */
	protected int port;

	/** User account name, can be null if not used *. */
	protected String user;

	/** Password, can be null if not used *. */
	protected String password;

	/** Path of ressource to extract *. */
	protected String path;

	/**
	 * Path of the folder in the store used as root for extraction, can be null
	 * if default root folder.
	 */
	protected String storeFolder;

	/** Path of the directory where will be the extraction directory. */
	protected String destRootPath;

	/** Name of the extraction directory. */
	protected String destName;

	/** Extractor options, flags coded on an int, defined thru constants *. */
	protected StoreExtractorOptions options;

	/** Extractor context description. */
	protected String description;

	// message count
	private int messageCount;

	// private fields for global statictics
	private int totalElementsCount;
	private int totalAttachedMessagesCount;
	private int totalFoldersCount;
	private long totalRawSize;

	// private object extraction root folder in store
	private StoreFolder rootAnalysisMBFolder;

	// private root storeExtractor for nested extraction, null if root
	private StoreExtractor rootStoreExtractor;

	// private logger
	private MailExtractProgressLogger logger;

	// private output stream for extract list, if any
	private PrintStream psExtractList;

	/**
	 * Add mimetypes, scheme, isContainer, store extractor known relation.
	 * <p>
	 * This is used by store extractor sub classes to subscribe. When the
	 * relation is known it can be used for processing automatically the
	 * mimetype files with appropriate store extractors, in code.
	 * <p>
	 * Warning: the mime type has to be the code returned by tika!
	 *
	 * @param mimeType
	 *            the mime type
	 * @param scheme
	 *            the scheme
	 * @param isContainer
	 *            the is container
	 * @param extractor
	 *            the extractor
	 */
	@SuppressWarnings("rawtypes")
	public static void addExtractionRelation(String mimeType, String scheme, boolean isContainer, Class extractor) {
		// if there is a file mimetype for this scheme
		if (mimeType != null)
			mimeTypeSchemeMap.put(mimeType, scheme);
		schemeStoreExtractorClassMap.put(scheme, extractor);
		schemeContainerMap.put(scheme, isContainer);
	}

	/**
	 * Compose an URL String.
	 *
	 * @param scheme
	 *            Type of local store to extract (thunderbird|pst|eml|mbox) or
	 *            protocol for server access (imap|imaps|pop3...)
	 * @param authority
	 *            Server:port of target account ((hostname|ip)[:port
	 * @param user
	 *            User account name, can be null if not used
	 * @param password
	 *            Password, can be null if not used
	 * @param path
	 *            Path to the ressource
	 * @return the string
	 */
	static public String composeStoreURL(String scheme, String authority, String user, String password, String path) {
		String result = null;

		try {
			result = scheme + "://";
			if (user != null && !user.isEmpty()) {
				result += URLEncoder.encode(user, "UTF-8");
				if (password != null && !password.isEmpty())
					result += ":" + URLEncoder.encode(password, "UTF-8");
				result += "@";
			}
			if (authority != null && !authority.isEmpty())
				result += authority;
			else
				result += "localhost";
			if (path != null && !path.isEmpty())
				result += "/" + URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// impossible case with UTF-8
		}
		return result;
	}

	/**
	 * Instantiates a new store extractor.
	 *
	 * @param urlString
	 *            the url string
	 * @param storeFolder
	 *            Path of the extracted folder in the store box, can be null if
	 *            default root folder
	 * @param destPathString
	 *            the dest path string
	 * @param options
	 *            Extractor options
	 * @param rootStoreExtractor
	 *            the creating store extractor in nested extraction, or null if
	 *            root one
	 * @param logger
	 *            logger used
	 * @param psExtractList
	 *            the ps extract list
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected StoreExtractor(String urlString, String storeFolder, String destPathString, StoreExtractorOptions options,
			StoreExtractor rootStoreExtractor, MailExtractProgressLogger logger, PrintStream psExtractList) throws ExtractionException {

		URLName url;
		url = new URLName(urlString);

		this.scheme = url.getProtocol();
		this.host = url.getHost();
		this.port = url.getPort();
		try {
			if (url.getUsername() != null)
				this.user = URLDecoder.decode(url.getUsername(), "UTF-8");
			if (url.getPassword() != null)
				this.password = URLDecoder.decode(url.getPassword(), "UTF-8");
			if (url.getFile() != null)
				this.path = URLDecoder.decode(url.getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// not possible
		}
		this.storeFolder = storeFolder;
		this.destRootPath = Paths.get(destPathString).normalize().getParent().toString();
		this.destName = Paths.get(destPathString).normalize().getFileName().toString();
		if (options == null)
			this.options = new StoreExtractorOptions();
		else
			this.options = options;

		this.messageCount = 0;
		this.totalFoldersCount = 0;
		this.totalAttachedMessagesCount = 0;
		this.totalElementsCount = 0;
		this.totalRawSize = 0;

		this.rootStoreExtractor = rootStoreExtractor;
		this.logger = logger;
		this.psExtractList = psExtractList;

		this.description = ":p:" + scheme + ":u:" + user;
	}

	/**
	 * Log the context of the StoreExtractor.
	 */
	public void writeTargetLog() throws InterruptedException {

		// if root extractor log extraction context
		if (rootStoreExtractor == null) {
			getLogger().progressLog(GLOBAL,
					"Target store with scheme=" + scheme + (host == null || host.isEmpty() ? "" : "  server=" + host)
							+ (port == -1 ? "" : ":" + Integer.toString(port))
							+ (user == null || user.isEmpty() ? "" : " user=" + user)
							+ (password == null || password.isEmpty() ? "" : " password=" + password)
							+ (path == null || path.isEmpty() ? "" : " path=" + path)
							+ (storeFolder == null || storeFolder.isEmpty() ? "" : " store folder=" + storeFolder));
			getLogger().progressLog(GLOBAL,"to " + destRootPath + " in " + destName + " directory");

			boolean first = true;
			String optionsLog = "";
			if (options.keepOnlyDeepEmptyFolders) {
				optionsLog += "keeping all empty folders except root level ones";
				first = false;
			}
			if (options.dropEmptyFolders) {
				if (!first)
					optionsLog += ", ";
				optionsLog += "droping all empty folders";
				first = false;
			}
			if (options.warningMsgProblem) {
				if (!first)
					optionsLog += ", ";
				optionsLog += "generate warning when there's a problem on a message (otherwise log at FINEST level)";
				first = false;
			}
			if (!first)
				optionsLog += ", ";
			optionsLog += "with names length=" + Integer.toString(options.namesLength);
			first = false;
			if (!first)
				optionsLog += ", ";
			optionsLog += "with log level " + getLogger().getLevelName();

			getLogger().progressLog(GLOBAL,optionsLog);
		}
		// if internal extractor give attachment context
		else {
			getLogger().progressLog(MESSAGE,"Target attached store scheme=" + scheme);
			getLogger().progressLog(MESSAGE,"to " + destRootPath + " in " + destName + " directory");
		}

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
	public MailExtractProgressLogger getLogger() {
		return logger;
	}

	private int uniqID = 1;

	/**
	 * Checks for dest name.
	 *
	 * @return true, if successful
	 */
	public boolean hasDestName() {
		return !((destName == null) || destName.isEmpty());
	}

	/**
	 * Gets a uniq ID in store extractor context.
	 * <p>
	 * Sequence incremented at each call in root store extractor context to
	 * garanty unicity for the whole extraction process even in nested
	 * extractions.
	 *
	 * @return a uniq ID
	 */
	public int getUniqID() {
		if (rootStoreExtractor == null)
			return uniqID++;
		else
			return rootStoreExtractor.getUniqID();
	}

	/**
	 * Increment the count of messages directly in the store (not attached...).
	 */
	public void incMessageCount() {
		messageCount ++;
	}

	/**
	 * Get the count of messages directly in the store (not attached...).
	 */
	public int getMessageCount() {
		return messageCount;
	}

	/**
	 * Increment the elements total count.
	 *
	 * @param inc
	 *            the increment
	 */
	public void addTotalElementsCount(int inc) {
		totalElementsCount += inc;
	}

	/**
	 * Gets the total count of all analyzed elements.
	 *
	 * @return the elements count
	 */
	public int getTotalElementsCount() {
		return totalElementsCount;
	}

	/**
	 * Increment the attached messages total count.
	 *
	 * @param inc
	 *            the inc
	 */
	public void addTotalAttachedMessagesCount(int inc) {
		totalAttachedMessagesCount += inc;
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
	 *
	 * @param elementSize
	 *            the element size
	 */
	public void addTotalRawSize(long elementSize) {
		totalRawSize += elementSize;
	}

	/**
	 * Gets the total raw size of all analyzed elements.
	 * <p>
	 * The "raw" size is the sum of the size of elements as in the store, the
	 * extraction will be larger (up to x2)
	 *
	 * @return the total raw size
	 */
	public long getTotalRawSize() {
		return totalRawSize;
	}

	// /**
	// * Checks for options.
	// *
	// * @param flags
	// * constant values (CONST_...) |/+ composition
	// * @return true, if successful
	// */
	// public boolean hasOptions(int flags) {
	// return (options & flags) != 0;
	// }
	//
	/**
	 * Gets the extraction context description.
	 *
	 * @return the description String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Is the store extractor the root one in nested extraction.
	 *
	 * @return the description String
	 */
	public boolean isRoot() {
		return rootStoreExtractor == null;
	}

	/**
	 * Gets the extraction root folder in store.
	 *
	 * @return the root StoreFolder
	 */
	public StoreFolder getRootFolder() {
		return rootAnalysisMBFolder;
	}

	/**
	 * Sets the extraction root folder in store.
	 *
	 * @param rootFolder
	 *            the new root folder
	 */
	public void setRootFolder(StoreFolder rootFolder) {
		rootAnalysisMBFolder = rootFolder;
	}

	/**
	 * Gets the store extractor options.
	 *
	 * @return the store extractor options
	 */
	public StoreExtractorOptions getOptions() {
		return options;
	}

	/**
	 * Gets the output stream for extraction list, if any.
	 *
	 * @return the store extractor options
	 */
	public PrintStream getPSExtractList() {
		return psExtractList;
	}

	/**
	 * Create a store extractor for the declared scheme in url as a factory
	 * creator.
	 *
	 * @param urlString
	 *            the url string
	 * @param storeFolder
	 *            Path of the extracted folder in the account mail box, can be
	 *            null if default root folder
	 * @param destPathString
	 *            the dest path string
	 * @param options
	 *            Options (flag composition of CONST_)
	 * @param logger
	 *            logger used
	 * @param psExtractList
	 *            the ps extract list
	 * @return the store extractor, constructed as a non abstract subclass
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public static StoreExtractor createStoreExtractor(String urlString, String storeFolder, String destPathString,
			StoreExtractorOptions options, MailExtractProgressLogger logger, PrintStream psExtractList) throws ExtractionException {
		StoreExtractor storeExtractor;

		storeExtractor = createInternalStoreExtractor(urlString, storeFolder, destPathString, options, null, logger,
				psExtractList);

		// write column names, if extract list expected
		if (options.extractList)
			psExtractList.println("SentDate|ReceivedDate|FromName|FromAddress|ToList|Subject|MessageID|"
					+ "AttachmentList|ReplyTo|Folder|Size|Attached|"
					+ "AppointmentLocation|AppointmentBeginDate|AppointmentEndDate");

		return storeExtractor;
	}

	/**
	 * Create an internal depth store extractor as a factory creator.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static StoreExtractor createInternalStoreExtractor(String urlString, String storeFolder,
			String destPathString, StoreExtractorOptions options, StoreExtractor rootStoreExtractor, MailExtractProgressLogger logger,
			PrintStream psExtractList) throws ExtractionException {

		StoreExtractor store;
		URLName url;

		url = new URLName(urlString);

		// get read of leading file separator in folder
		if ((storeFolder != null) && (!storeFolder.isEmpty()) && (storeFolder.substring(0, 1) == File.separator))
			storeFolder = storeFolder.substring(1);

		// find the store extractor constructor for scheme in URL
		Class storeExtractorClass = StoreExtractor.schemeStoreExtractorClassMap.get(url.getProtocol());
		if (storeExtractorClass == null) {
			throw new ExtractionException("mailextract: Unknown embedded store type=" + url.getProtocol());
		} else {
			try {
				store = (StoreExtractor) storeExtractorClass.getConstructor(String.class, String.class, String.class,
						StoreExtractorOptions.class, StoreExtractor.class, MailExtractProgressLogger.class, PrintStream.class)
						.newInstance(urlString, storeFolder, destPathString, options, rootStoreExtractor, logger,
								psExtractList);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
					| SecurityException e) {
				throw new ExtractionException("mailextract: Dysfonctional embedded store type=" + url.getProtocol());
			} catch (InvocationTargetException e) {
				Throwable te = e.getCause();
				if (te instanceof ExtractionException)
					throw (ExtractionException) te;
				throw new ExtractionException("mailextract: Dysfonctional embedded store type=" + url.getProtocol());
			}
		}
		return store;

	}

	/**
	 * Extract all folders from the defined root folder (considering drop
	 * options).
	 * 
	 * <p>
	 * This is a method where the extraction structure and content is partially
	 * defined (see also {@link StoreMessage#extractMessage extractMessage} and
	 * {@link StoreFolder#extractFolder extractFolder}).
	 * 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void extractAllFolders() throws ExtractionException, InterruptedException {
		String title;

		Instant start = Instant.now();

		writeTargetLog();
		getLogger().progressLog(GLOBAL,"Extraction processed");

		rootAnalysisMBFolder.extractFolderAsRoot(true);

		ArchiveUnit rootNode = rootAnalysisMBFolder.getArchiveUnit();
		rootNode.addMetadata("DescriptionLevel", "RecordGrp", true);

		// title generation from context
		if ((user != null) && (!user.isEmpty()))
			title = "Ensemble des messages électroniques envoyés et reçus par le compte " + user;
		else if ((path != null) && (!path.isEmpty()))
			title = "Ensemble des messages électroniques du container " + path;
		else
			title = "Ensemble de messages ";
		if ((host != null) && (!host.isEmpty()))
			title += " sur le serveur " + host + (port == -1 ? "" : ":" + Integer.toString(port));
		title += " à la date du " + start;
		rootNode.addMetadata("Title", title, true);
		if (rootAnalysisMBFolder.dateRange.isDefined()) {
			rootNode.addMetadata("StartDate", DateRange.getISODateString(rootAnalysisMBFolder.dateRange.getStart()),
					true);
			rootNode.addMetadata("EndDate", DateRange.getISODateString(rootAnalysisMBFolder.dateRange.getEnd()), true);
		}
		rootNode.write();

		Instant end = Instant.now();
		String size = Double.toString(Math.round(((double) getTotalRawSize()) * 100.0 / (1024.0 * 1024.0)) / 100.0);
		getLogger().progressLog(GLOBAL,"Terminated in " + Duration.between(start, end).toString() + " writing "
				+ Integer.toString(getFolderTotalCount()) + " folders and " + Integer.toString(getTotalElementsCount())
				+ " messages, for a total size of " + size + " MBytes and "
				+ Integer.toString(getTotalAttachedMessagesCount()) + " attached message");
		System.out.println("Terminated in " + Duration.between(start, end).toString() + " writing "
				+ Integer.toString(getFolderTotalCount()) + " folders and " + Integer.toString(getTotalElementsCount())
				+ " messages, for a total size of " + size + " MBytes and "
				+ Integer.toString(getTotalAttachedMessagesCount()) + " attached message");

	}

	/**
	 * List all folders from the defined root folder (no drop options).
	 * 
	 * <p>
	 * Warning: listing with detailed information is a potentially expensive
	 * operation, especially when accessing distant account, as all elements are
	 * inspected (in the case of a distant account that mean also
	 * downloaded...).
	 *
	 * @param stats
	 *            true if detailed information (number and raw size of elements
	 *            in each folder) is asked for
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void listAllFolders(boolean stats) throws ExtractionException, InterruptedException {
		String time, tmp;
		Duration d;

		Instant start = Instant.now();

		writeTargetLog();
		getLogger().progressLog(GLOBAL,"Listing processed");

		rootAnalysisMBFolder.listFolder(stats);

		Instant end = Instant.now();
		System.out.println("--------------------------------------------------------------------------------");

		d = Duration.between(start, end);
		time = String.format("%dm%02ds", d.toMinutes(), d.minusMinutes(d.toMinutes()).getSeconds());
		tmp = String.format("Terminated in %s listing %d folders", time, getFolderTotalCount());
		if (stats) {
			tmp += String.format(" with %d messages, for %.2f MBytes, and %d attached messages",
					getTotalElementsCount(), ((double) getTotalRawSize()) / (1024.0 * 1024.0),
					getTotalAttachedMessagesCount());
		}

		getLogger().progressLog(GLOBAL,tmp);
		System.out.println(tmp);
	}

	/**
	 * Do all end tasks for the StoreExtractor, like deleting temporary files.
	 *
	 * @throws ExtractionException
	 *             the extraction exception
	 */
	public void endStoreExtractor() throws ExtractionException {
	}

	/**
	 * Checks for magic number.
	 *
	 * @param content
	 *            the content
	 * @param magicNumber
	 *            the magic number
	 * @return true, if successful
	 */
	// Utility function to detect if the four bytes is a defined magic number
	public static boolean hasMagicNumber(byte[] content, byte[] magicNumber) {
		return hasMagicNumber(content, magicNumber, 0);
	}

	// Utility function to detect if the bytes at offset is a defined magic
	/**
	 * Checks for magic number.
	 *
	 * @param content
	 *            the content
	 * @param magicNumber
	 *            the magic number
	 * @param offset
	 *            the offset
	 * @return true, if successful
	 */
	// number
	public static boolean hasMagicNumber(byte[] content, byte[] magicNumber, int offset) {
		if (content.length < magicNumber.length + offset)
			return false;
		for (int i = 0; i < magicNumber.length; i++) {
			if (content[i + offset] != magicNumber[i])
				return false;
		}
		return true;
	}

	/**
	 * Gets the attachment in which is the embedded object.
	 *
	 * @return the attachment
	 */
	abstract public StoreMessageAttachment getAttachment();

	/**
	 * Gets the scheme if this content can be managed by this StoreExtractor, or
	 * null
	 *
	 * @param content
	 *            the content
	 * @return the scheme
	 */
	public static String getVerifiedScheme(byte[] content) {
		return null;
	}
}
