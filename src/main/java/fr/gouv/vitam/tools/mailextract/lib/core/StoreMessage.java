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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import fr.gouv.vitam.tools.mailextract.lib.formattools.FileTextExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * Abstract class for store file which is a mail box message.
 * <p>
 * It defines all information (descriptive metadata and objects) to collect from
 * a message and the method to generate directory/files structure from this
 * information. Each subclass has to be able to extract these informations from
 * a message.
 * <ul>
 * <p>
 * Metadata information to collect in Vitam guidelines for mail extraction
 * <li>Subject (Title metadata),</li>
 * <li>List of "From" addresses (Writer metadata),</li>
 * <li>List of "To" recipients addresses (Addressee metadata),</li>
 * <li>List of "Cc" and "Bcc" recipients addresses (Recipient metadata),</li>
 * <li>Message unique ID given by the sending server (OriginatingSystmId
 * metadata),</li>
 * <li>Sent date (SentDate metadata),</li>
 * <li>Received date (ReceivedDate metadata),</li> In the descriptive metadata
 * is also added the DescriptionLevel, which is Item for message, for Body and
 * for Attachements.
 * <p>
 * Metadata information extracted for study
 * <li>Message unique ID of the message replied to (and in some implementation
 * forwarded) by the current message (OriginatingSystemIdReplyTo metadata),</li>
 * <li>List of message unique ID of messages in the same thread of forward and
 * reply (OriginatingSystemId-References metadata),</li>
 * <li>List of "Sender" addresses, address of the person sending the mail, for
 * example a secretary for his boss (Sender metadata),</li>
 * <li>List of "Reply-To" addresses (ReplyTo metadata),</li>
 * <li>List of "Return-Path" addresses, more reliable information given by the
 * first mail relay server (ReturnPath metadata),</li>
 * <li>Complete mail header (MailHeader metadata).</li>
 * <p>
 * Content information extracted
 * <li>Text Content, text extraction of the message body,</li>
 * <li>Attachments, content with filename,</li>
 * <li>Raw Content, message as is in the mailbox.</li>
 * </ul>
 * <p>
 * All addresses fields are lists because even for one only person, they can
 * contain different forms of the address, as Microsoft ActiveDirectory DN and
 * smtp address.
 * 
 * 
 */
public abstract class StoreMessage extends StoreFile {

	/** Mail box folder. containing this message. **/
	protected StoreFolder storeFolder;

	/**
	 * Raw binary content of the message for mime sources, or of the mime fake
	 * for others.
	 */
	protected byte[] mimeContent;

	/** Mime fake if any, or null for mime source */
	protected MimeMessage mimeFake;

	// /** Text version of the message body. */
	// protected String textContent;

	/** Different versions of the message body. */
	protected String[] bodyContent = new String[3];
	static public final int TEXT_BODY = 0;
	static public final int HTML_BODY = 1;
	static public final int RTF_BODY = 2;

	/** Complete mail header. */
	protected List<String> mailHeader;

	/** Attachments list. */
	protected List<Attachment> attachments;

	/**
	 * Subject.
	 * <p>
	 * It can't be null. If the message has no subject it has to be an empty
	 * String
	 */
	protected String subject;

	/**
	 * "From" address.
	 */
	protected String from;

	/** List of "To" recipients addresses. */
	protected List<String> recipientTo;

	/** List of "Cc"recipients addresses. */
	protected List<String> recipientCc;

	/** List of "Bcc" recipients addresses. */
	protected List<String> recipientBcc;

	/** List of "Reply-To" addresses. */
	protected List<String> replyTo;

	/** "Return-Path" address. */
	protected String returnPath;

	/** Sent date. **/
	protected Date sentDate;

	/** Received date. **/
	protected Date receivedDate;

	/** Message unique ID given by the sending server. */
	protected String messageUID;

	/**
	 * Message unique ID of the message replied to (and in some implementation
	 * forwarded) by the current message.
	 */
	protected String inReplyToUID;

	/**
	 * List of message unique ID of the message in the same thread of forward
	 * and reply.
	 */
	protected List<String> references;

	/** List of "Sender" addresses. */
	protected List<String> sender;

	/** Macro types of attachment */
	public static final int MACRO_ATTACHMENT_TYPE_FILTER = 0xF;
	public static final int FILE_ATTACHMENT = 0x00;
	public static final int INLINE_ATTACHMENT = 0x01;
	public static final int STORE_ATTACHMENT = 0x02;

	/** Specific store types of attachment */
	public static final int SPECIFIC_ATTACHMENT_TYPE_FILTER = 0XF0;
	public static final int EML_STORE_ATTACHMENT = 0x10;
	public static final int MSG_STORE_ATTACHMENT = 0x20;
	public static final int MBOX_STORE_ATTACHMENT = 0x30;

	/**
	 * Utility class to encapsulate an attachment file with content and metadata
	 * (for now only filename) size and filename.
	 */
	protected class Attachment {

		/** Binary raw content. */
		byte[] rawContent;

		/** Filename. */
		String filename;

		/** File dates. */
		Date creationDate, modificationDate;

		/** Type of attachment **/
		String mimeType;

		/** Content-ID **/
		String contentID;

		/** Attachment type. */
		int attachmentType;

		/**
		 * Instantiates a new attachment.
		 *
		 * @param filename
		 *            Filename
		 * @param rawContent
		 *            Binary raw content
		 * @param creationDate
		 *            Creation Date
		 * @param modificationDate
		 *            Last modification Date
		 * @param mimeType
		 *            MimeType
		 * @param contentID
		 *            Mime multipart content ID usefull for inline
		 * @param attachmentType
		 *            Type of attachment (inline, simple file, another store...)
		 */

		public Attachment(String filename, byte[] rawContent, Date creationDate, Date modificationDate, String mimeType,
				String contentID, int attachmentType) {
			this.filename = filename;
			this.rawContent = rawContent;
			this.creationDate = creationDate;
			this.modificationDate = modificationDate;
			this.mimeType = mimeType;
			this.contentID = contentID;
			this.attachmentType = attachmentType;
		}

		public String getFilename() {
			return filename;
		}

		public byte[] getRawContent() {
			return rawContent;
		}

	}

	/**
	 * Instantiates a new mail box message.
	 *
	 * @param storeFolder
	 *            Mail box folder containing this message
	 */
	protected StoreMessage(StoreFolder storeFolder) {
		this.storeFolder = storeFolder;
	}

	/**
	 * Gets the sent date.
	 * 
	 * <p>
	 * Specific metadata getter used for folder date range computation.
	 *
	 * @return the sent date
	 */
	public Date getSentDate() {
		return sentDate;
	}

	/**
	 * Gets the message size.
	 * 
	 * <p>
	 * Specific information getter used for listing size statistic of folders.
	 * Depend on sub class implementation.
	 *
	 * @return the message size
	 */
	public abstract long getMessageSize();

	/**
	 * Gets the logger created during the store extractor construction, and used
	 * in all mailextract classes.
	 * 
	 * <p>
	 * For convenience each class which may have some log actions has it's own
	 * getLogger method always returning the store extractor logger.
	 *
	 * @return logger
	 */
	public Logger getLogger() {
		return storeFolder.getLogger();
	}

	/**
	 * Gets the current operation store extractor.
	 * 
	 * @return storeExtractor
	 */
	public StoreExtractor getStoreExtractor() {
		return storeFolder.getStoreExtractor();
	}

	/**
	 * Log at warning or at finest level depending on store extractor options
	 * <p>
	 * To log a problem on a specific message
	 * 
	 * @param msg
	 *            Message to log
	 **/
	protected void logWarning(String msg) {
		if (storeFolder.getStoreExtractor().hasOptions(StoreExtractor.CONST_WARNING_MSG_PROBLEM))
			getLogger().warning(msg);
		else
			getLogger().finest(msg);
	}

	// /**
	// * Strip beginning < and ending > from a string
	// */
	// private static String getTag(String str) {
	// str.trim();
	// if (!str.isEmpty()) {
	// if ((str.charAt(0) == '<') && (str.charAt(str.length() - 1) == '>')) {
	// str = str.substring(1, str.length() - 1);
	// }
	// }
	// return str;
	// }
	//

	protected abstract void doAnalyzeMessage() throws ExtractionException;

	/**
	 * Analyze message to collect metadata and content information (protocol
	 * specific).
	 * 
	 * <p>
	 * This is the main method for sub classes, where all metadata and
	 * information has to be extracted in standard representation out of the
	 * inner representation of the message.
	 * 
	 * If needed a fake raw SMTP content (.eml) is generated with all the body
	 * formats available but without the attachments, which are extracted too.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void analyzeMessage() throws ExtractionException {
		doAnalyzeMessage();
		// generate mime fake if needed and associated mimeContent
		if (mimeContent == null) {
			mimeFake = getMimeFake();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				mimeFake.writeTo(baos);
				mimeContent = baos.toByteArray();
			} catch (Exception e) {
				logWarning("mailextract.javamail: Can't extract raw content from message " + subject);
			}
		}
		if (mimeContent == null)
			mimeContent = "".getBytes();
		getLogger().finer("mailextract.javamail: Extracted message " + (subject == null ? "Unknown title" : subject));
		getLogger().finest("with SentDate=" + (sentDate == null ? "Unknown sent date" : sentDate.toString()));
	}

	/**
	 * Create the Archive Unit structures with all content and metadata needed,
	 * and then write them on disk if writeFlag is true
	 * <p>
	 * This is "the" method where the extraction structure and content is mainly
	 * defined (see also {@link StoreFolder#extractFolder extractFolder} and
	 * {@link StoreExtractor#extractAllFolders extractAllFolders}).
	 * 
	 * @param writeFlag
	 *            write or not flag (no write used for stats)
	 * 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public final void extractMessage(boolean writeFlag) throws ExtractionException {
		ArchiveUnit messageNode = null;
		// String description = "[Vide]";
		String content;

		// create message unit
		if ((subject == null) || subject.isEmpty())
			subject = "[Vide]";
		messageNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.folderArchiveUnit, "Message", subject);

		// metadata in SEDA 2.0-ontology order
		messageNode.addMetadata("DescriptionLevel", "Item", true);
		messageNode.addMetadata("Title", subject, true);
		messageNode.addMetadata("OriginatingSystemId", messageUID, false);

		// description = "Message extrait du compte " +
		// mailBoxFolder.storeExtractor.user;
		// messageNode.addMetadata("Description", description, true);
		messageNode.addPersonMetadata("Writer", from, false);
		messageNode.addPersonMetadataList("Addressee", recipientTo, false);
		messageNode.addPersonMetadataList("Recipient", recipientCc, false);
		messageNode.addPersonMetadataList("Recipient", recipientBcc, false);
		messageNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), false);
		messageNode.addMetadata("ReceivedDate", DateRange.getISODateString(receivedDate), false);

		// not in SEDA ontology

		// reply-to messageID
		if ((inReplyToUID != null) && !inReplyToUID.isEmpty())
			messageNode.addMetadata("OriginatingSystemIdReplyTo", inReplyToUID, false);

		// extract text content in file format and in metadata
		if (bodyContent[TEXT_BODY] != null) {
			content = bodyContent[TEXT_BODY].trim();
			if (!content.isEmpty()) {
				messageNode.addMetadata("TextContent", content, true);
				messageNode.addObject(content, messageUID + ".txt", "TextContent", 1);
			}
		}
		// add object binary master
		messageNode.addObject(mimeContent, messageUID + ".eml", "BinaryMaster", 1);

		if (writeFlag)
			messageNode.write();

		if (attachments != null && !attachments.isEmpty()) {
			// create all attachments subunits/object groups
			extractMessageAttachments(messageNode, writeFlag);
		}
	}

	/** Extract a file or inline message attachment. */
	private final void extractFileOrInlineAttachment(ArchiveUnit messageNode, Attachment attachment, boolean writeFlag)
			throws ExtractionException {
		ArchiveUnit attachmentNode;
		String textExtract;

		if ((attachment.filename == null) || attachment.filename.isEmpty())
			attachment.filename = "[Vide]";
		attachmentNode = new ArchiveUnit(storeFolder.storeExtractor, messageNode, "Attachment", attachment.filename);
		attachmentNode.addMetadata("DescriptionLevel", "Item", true);
		attachmentNode.addMetadata("Title", attachment.filename, true);
		attachmentNode.addMetadata("Description",
				"Document \"" + attachment.filename + "\" joint au message " + messageUID, true);

		// get the max of creation and modification date which define the
		// creation date of the present file
		// (max for correcting a current confusion between theese two dates)
		Date date = null;
		if (attachment.creationDate != null) {
			if (attachment.modificationDate != null)
				date = (attachment.creationDate.compareTo(attachment.modificationDate) > 0 ? attachment.creationDate
						: attachment.modificationDate);
			else
				date = attachment.creationDate;
		} else if (attachment.modificationDate != null)
			date = attachment.modificationDate;
		if (date != null)
			attachmentNode.addMetadata("CreatedDate", DateRange.getISODateString(attachment.creationDate), true);

		// Raw object extraction
		attachmentNode.addObject(attachment.rawContent, attachment.filename, "BinaryMaster", 1);

		// Text object extraction
		try {
			textExtract = FileTextExtractor.getInstance().getText(attachment.rawContent);
			if (!((textExtract == null) || textExtract.isEmpty()))
				attachmentNode.addObject(textExtract.getBytes(), attachment.filename + ".txt", "TextContent", 1);
		} catch (ExtractionException ee) {
			logWarning("mailextract: Can't extract text content from attachment " + attachment.filename + " in message "
					+ subject);
		}
		if (writeFlag)
			attachmentNode.write();
	}

	// create a store temporary file
	private File writeStoreFile(String dirPath, byte[] byteContent) throws ExtractionException {
		File storeFile;

		OutputStream output = null;
		try {
			Files.createDirectories(Paths.get(dirPath));
			storeFile = new File(dirPath + File.separator + "tmpStore");
			output = new BufferedOutputStream(new FileOutputStream(storeFile));
			if (byteContent != null)
				output.write(byteContent);
		} catch (IOException ex) {
			if (dirPath.length() + 8 > 250)
				throw new ExtractionException(
						"mailextract: Store file extraction illegal destination file (may be too long pathname), extracting unit in path "
								+ dirPath);
			else
				throw new ExtractionException(
						"mailextract: Store file extraction illegal destination file, extracting unit in path "
								+ dirPath);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					throw new ExtractionException(
							"mailextract: Can't close store file extraction, extracting unit in path " + dirPath);
				}
		}

		return (storeFile);
	}

	// delete a store temporary file
	private void clearStoreFile(File storeFile) {
		storeFile.delete();
	}

	/** Extract a store attachment */
	private final void extractStoreAttachment(ArchiveUnit rootNode, DateRange attachedMessagedateRange, String filename,
			byte[] rawContent, int attachedType, String tag, boolean writeFlag) throws ExtractionException {
		String protocol;

		switch (attachedType & SPECIFIC_ATTACHMENT_TYPE_FILTER) {
		case EML_STORE_ATTACHMENT:
			protocol = "eml";
			break;
		default:
			logWarning("mailextract: Unknown embedded store type=" + attachedType + " , extracting unit in path "
					+ rootNode.getFullName());
			return;
		}

		File storeFile = writeStoreFile(rootNode.getFullName(), rawContent);
		try {
			StoreExtractor extractor = StoreExtractor.createInternalStoreExtractor(protocol, "", "", "",
					storeFile.getAbsolutePath(), "", rootNode.getRootPath(), rootNode.getName(),
					getStoreExtractor().options, getStoreExtractor(), getLogger());
			extractor.writeTargetLog();
			extractor.rootAnalysisMBFolder.extractFolderAsRoot(writeFlag);
			getStoreExtractor().addTotalAttachedMessagesCount(
					extractor.getTotalMessagesCount() + extractor.getTotalAttachedMessagesCount());
			attachedMessagedateRange.extendRange(extractor.rootAnalysisMBFolder.getDateRange());
		} finally {
		}
		clearStoreFile(storeFile);
	}

	/** Extract all message attachments. */
	private final void extractMessageAttachments(ArchiveUnit messageNode, boolean writeFlag)
			throws ExtractionException {
		ArchiveUnit rootNode;
		DateRange attachedMessagedateRange;
		boolean attachedFlag = false;
		int count = 1;

		// prepare an ArchiveUnit to keep all attached message that can be
		// recursively extracted
		rootNode = new ArchiveUnit(storeFolder.storeExtractor, messageNode.getFullName(), "Attached Messages");
		rootNode.addMetadata("DescriptionLevel", "Item", true);
		rootNode.addMetadata("Title", "Messages attachés", true);
		rootNode.addMetadata("Description", "Ensemble des messages attachés joint au message " + messageUID, true);
		attachedMessagedateRange = new DateRange();

		for (Attachment a : attachments) {
			// message identification
			if ((a.attachmentType & MACRO_ATTACHMENT_TYPE_FILTER) == STORE_ATTACHMENT) {
				// recursive extraction of a message in attachment...
				logWarning("mailextract: Attached message extraction from message " + subject);
				extractStoreAttachment(rootNode, attachedMessagedateRange, a.filename, a.rawContent, a.attachmentType,
						"At#" + count, writeFlag);
				count++;
				attachedFlag = true;
			} else if (writeFlag) {
				// standard attachment file
				extractFileOrInlineAttachment(messageNode, a, writeFlag);
			}
		}
		if (attachedFlag && writeFlag) {
			if (attachedMessagedateRange.isDefined()) {
				rootNode.addMetadata("StartDate", DateRange.getISODateString(attachedMessagedateRange.getStart()),
						true);
				rootNode.addMetadata("EndDate", DateRange.getISODateString(attachedMessagedateRange.getEnd()), true);
			}
			rootNode.write();
		}
	}

	/**
	 * Add this message in the folder accumulators for number of messages and
	 * total raw size of messages.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void countMessage() throws ExtractionException {
		// accumulate in folder statistics
		storeFolder.incFolderMessagesCount();
		storeFolder.addFolderMessagesRawSize(getMessageSize());
	}

	public MimeMessage getMimeFake() {
		MimeMessage mime = new FixIDMimeMessage(Session.getDefaultInstance(new Properties()));
		try {
			buildMimeHeader(mime);
			buildMimePart(mime);
			mime.saveChanges();
		} catch (MessagingException e) {
			logWarning("mailextract: Unable to generate mime fake of message " + subject);
			mime = null;
		} catch (ExtractionException e) {
			logWarning("mailextract: " + e.getMessage());
			mime = null;
		}
		return mime;
	}

	private static void setAddressList(MimeMessage mime, String tag, List<String> addressList)
			throws MessagingException {
		if (!addressList.isEmpty()) {
			String value = "";
			for (String tmp : addressList) {
				value += tmp + ",";
			}
			value = value.substring(0, value.length() - 1);
			mime.setHeader(tag, value);
		}
	}

	private void buildMimeHeader(MimeMessage mime) throws ExtractionException {
		try {
			// put all know headers, they will be change by the specific ones
			if ((mailHeader != null) && (mailHeader.size() > 0)) {
				String tag, value;
				for (String tmp : mailHeader) {
					if (tmp.indexOf(':') < 0)
						continue;
					tag = tmp.substring(0, tmp.indexOf(':'));
					value = tmp.substring(tmp.indexOf(':') + 1);
					mime.setHeader(tag, value);
				}
			}

			// Return-Path
			mime.setHeader("Return-Path", returnPath);
			// From
			mime.setHeader("From", from);
			// To
			setAddressList(mime, "To", recipientTo);
			// cc
			setAddressList(mime, "cc", recipientCc);
			// bcc
			setAddressList(mime, "bcc", recipientBcc);
			// Reply-To
			setAddressList(mime, "Reply-To", replyTo);
			// Date
			mime.setSentDate(sentDate);
			// Subject
			mime.setSubject(subject);
			// Message-ID
			mime.setHeader("Message-ID", messageUID);
			// In-Reply-To
			if ((inReplyToUID != null) && (!inReplyToUID.isEmpty()))
				mime.setHeader("In-Reply-To", inReplyToUID);

		} catch (MessagingException e) {
			throw new ExtractionException("Unable to generate mime header of message " + subject);
		}
	}

	private void addAttachmentPart(MimeMultipart root, boolean isInline) throws ExtractionException {
		try {
			// build attach part
			for (Attachment a : attachments) {
				boolean thisIsInline = ((a.attachmentType & MACRO_ATTACHMENT_TYPE_FILTER) == INLINE_ATTACHMENT);

				if ((thisIsInline && isInline) || ((!thisIsInline) && (!isInline))) {
					MimeBodyPart attachPart = new MimeBodyPart();

					// set Content-ID
					String cidName = null;
					if (a.contentID != null) {
						attachPart.setContentID("<" + a.contentID.trim() + ">");
						if (a.contentID.indexOf('@') < 0)
							cidName = a.contentID;
						else
							cidName = a.contentID.substring(0, a.contentID.indexOf('@'));
					} else
						cidName = "unknown";

					// set object and Content-Type
					String attachmentName = encodedFilename(a.filename, cidName);
					if ((a.mimeType == null) || (a.mimeType.isEmpty()))
						attachPart.setContent(a.rawContent,
								"application/octet-stream; name=\"" + attachmentName + "\"");
					else
						attachPart.setContent(a.rawContent, a.mimeType + "; name=\"" + attachmentName + "\"");
					// set Content-Disposition
					if ((a.attachmentType & MACRO_ATTACHMENT_TYPE_FILTER) == INLINE_ATTACHMENT)
						attachPart.setDisposition("inline; filename=\"" + attachmentName + "\"");
					else
						attachPart.setDisposition("attachment; filename=\"" + attachmentName + "\"");
					root.addBodyPart(attachPart);
				}
			}
		} catch (MessagingException e) {
			throw new ExtractionException(
					"Unable to generate " + (isInline ? "inlines" : "attachments") + " of message " + subject);
		}

	}

	private MimeMultipart newChild(MimeMultipart parent, String type) throws MessagingException {
		MimeMultipart child = new MimeMultipart(type);
		final MimeBodyPart mbp = new MimeBodyPart();
		parent.addBodyPart(mbp);
		mbp.setContent(child);
		return child;
	}

	private void buildMimePart(MimeMessage mime) throws ExtractionException {
		boolean hasInline = false;

		MimeMultipart rootMp = new MimeMultipart("mixed");
		{
			try {
				// search if there are inlines
				for (Attachment a : attachments) {
					if ((a.attachmentType & MACRO_ATTACHMENT_TYPE_FILTER) == INLINE_ATTACHMENT) {
						hasInline = true;
						break;
					}
				}
				// build message part
				MimeMultipart msgMp = newChild(rootMp, "alternative");
				{
					if (bodyContent[TEXT_BODY] != null) {
						MimeBodyPart part = new MimeBodyPart();
						part.setContent(bodyContent[TEXT_BODY], "text/plain; charset=utf-8");
						msgMp.addBodyPart(part);
					}
					if (bodyContent[HTML_BODY] != null) {
						MimeMultipart upperpart;
						if (hasInline)
							upperpart = newChild(msgMp, "related");
						else
							upperpart = msgMp;

						MimeBodyPart part = new MimeBodyPart();
						part.setContent(bodyContent[HTML_BODY], "text/html; charset=utf-8");
						upperpart.addBodyPart(part);

						if (hasInline) {
							addAttachmentPart(upperpart, true);
						}

					}
					if (bodyContent[RTF_BODY] != null) {
						MimeBodyPart part = new MimeBodyPart();
						part.setContent(bodyContent[RTF_BODY], "text/rtf; charset=utf-8");
						msgMp.addBodyPart(part);
					}
				}
			} catch (MessagingException e) {
				throw new ExtractionException("Unable to generate mime body part of message " + subject);
			}

			// add inline part of attachments if not added to HTML body
			if (bodyContent[HTML_BODY] == null)
				addAttachmentPart(rootMp, true);
			addAttachmentPart(rootMp, false);

			try {
				mime.setContent(rootMp);
			} catch (MessagingException e) {
				throw new ExtractionException("Unable to generate mime fake of message " + subject);
			}
		}
	}

	private String encodedFilename(String filename, String ifnone) {
		String tmp;
		if ((filename != null) && !filename.trim().isEmpty())
			tmp = filename;
		else
			tmp = ifnone;
		try {
			return MimeUtility.encodeWord(tmp, "utf-8", "B");
		} catch (UnsupportedEncodingException e) {
			// forget it
		}
		return "Unknown";
	}

	/**
	 * Prevent update Message-ID
	 * 
	 * @author inter6
	 *
	 */
	private class FixIDMimeMessage extends MimeMessage {

		public FixIDMimeMessage(Session session) {
			super(session);
		}

		@Override
		protected void updateMessageID() throws MessagingException {
			String[] ids = getHeader("Message-ID");
			if (ids == null || ids.length == 0 || ids[0] == null || ids[0].isEmpty()) {
				super.updateMessageID();
			}
		};

	}

}
