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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;

import fr.gouv.vitam.tools.mailextract.lib.formattools.FileTextExtractor;
import fr.gouv.vitam.tools.mailextract.lib.formattools.HTMLTextExtractor;
import fr.gouv.vitam.tools.mailextract.lib.formattools.RFC822Identificator;
import fr.gouv.vitam.tools.mailextract.lib.javamail.JMStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * Abstract class for a mail box message.
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
 * forwarded) by the current message (OriginatingSystemId-ReplyTo
 * metadata),</li>
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
public abstract class MailBoxMessage {

	/** Mail box folder. containing this message. **/
	protected MailBoxFolder mailBoxFolder;

	/** Raw binary content of the message, as is in the origin mailbox. */
	protected byte[] rawContent;

	/** Text version of the message body. */
	protected String textContent;

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
	 * List of "From" addresses.
	 */
	protected List<String> from;

	/** List of "To" recipients addresses. */
	protected List<String> recipientTo;

	/** List of "Cc" and "Bcc" recipients addresses. */
	protected List<String> recipientCcAndBcc;

	/** List of "Reply-To" addresses. */
	protected List<String> replyTo;

	/** List of "Return-Path" addresses. */
	protected List<String> returnPath;

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

	/**
	 * Utility class to encapsulate an attachment file with content and metadata
	 * (for now only filename) size and filename.
	 */
	protected class Attachment {

		/** Binary raw content. */
		byte[] rawContent;

		/** Filename. */
		String filename;

		/**
		 * Instantiates a new attachment.
		 *
		 * @param filename
		 *            Filename
		 * @param rawContent
		 *            Binary raw content
		 */
		public Attachment(String filename, byte[] rawContent) {
			this.filename = filename;
			this.rawContent = rawContent;
		}

	}

	/**
	 * Instantiates a new mail box message.
	 *
	 * @param mailBoxFolder
	 *            Mail box folder containing this message
	 */
	protected MailBoxMessage(MailBoxFolder mailBoxFolder) {
		this.mailBoxFolder = mailBoxFolder;
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
		return mailBoxFolder.getLogger();
	}

	/**
	 * Gets the current operation store extractor.
	 * 
	 * @return storeExtractor
	 */
	public StoreExtractor getStoreExtractor() {
		return mailBoxFolder.getStoreExtractor();
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
		if (mailBoxFolder.getStoreExtractor().hasOptions(StoreExtractor.CONST_WARNING_MSG_PROBLEM))
			getLogger().warning(msg);
		else
			getLogger().finest(msg);
	}

	/**
	 * Strip beginning < and ending > from a string
	 */
	private static String getTag(String str){
		str.trim();
		if (str.charAt(0)=='<') {
			str=str.substring(1);
		}
		if (str.charAt(str.length()-1)=='>'){
			str=str.substring(0, str.length()-1);
		}
		return str;
	}
	
	/**
	 * Analyze message to collect metadata and content information (protocol
	 * specific).
	 * 
	 * <p>
	 * This is the main method for sub classes, where all metadata and
	 * information has to be extracted in standard representation out of the
	 * inner representation of the message.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public abstract void analyzeMessage() throws ExtractionException;

	/**
	 * Create the Archive Unit structures with all content and metadata needed,
	 * and then write them on disk if writeFlag is true
	 * <p>
	 * This is "the" method where the extraction structure and content is mainly
	 * defined (see also {@link MailBoxFolder#extractFolder extractFolder} and
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
		String description="[Vide]";
		String content;

		// create message unit
		if ((subject==null) || subject.isEmpty())
			subject="[Vide]";
		messageNode = new ArchiveUnit(mailBoxFolder.storeExtractor, mailBoxFolder.folderArchiveUnit, "Message",
				subject);

		// metadata in SEDA 2.0-ontology order
		messageNode.addMetadata("DescriptionLevel", "Item", true);
		messageNode.addMetadata("Title", subject, true);
		
			// strip messageUID from < and >
		messageUID=getTag(messageUID);
		messageNode.addMetadata("OriginatingSystemId", messageUID, true);

		description="Message extrait du contexte "+mailBoxFolder.storeExtractor.getDescription();
		messageNode.addMetadata("Description", description, true);
		messageNode.addPersonMetadataList("Writer", from, true);
		messageNode.addPersonMetadataList("Addressee", recipientTo, true);
		messageNode.addPersonMetadataList("Recipient", recipientCcAndBcc, false);
		messageNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), true);
		messageNode.addMetadata("ReceivedDate", DateRange.getISODateString(receivedDate), false);

		/* wait for multivalued unknown metadata good treatment in Vitam
		// not in SEDA ontology
		 
		if ((inReplyToUID!=null) && !inReplyToUID.isEmpty())
			messageNode.addMetadata("OriginatingSystemId-ReplyTo", inReplyToUID, false);
		messageNode.addSameMetadataList("OriginatingSystemId-References", references, false);
		
		// not in SEDA ontology nor in the Vitam specs... (to be discussed)
		messageNode.addPersonMetadataList("Sender", sender, false);
		messageNode.addPersonMetadataList("ReplyTo", replyTo, false);
		messageNode.addPersonMetadataList("ReturnPath", returnPath, false);
		 */
		
		// extract text content in file format and in metadata
		if (textContent != null) {
			content = textContent.trim();
			if (!content.isEmpty()) {
//			String trimed = textContent.trim();
//			if (!trimed.isEmpty()) {
//				content=
//				int begBeg, begEnd, endBeg, endEnd, len;
//
//				// extract description from text format
//				len = trimed.length();
//				begBeg = 0;
//				if (len <= 160) {
//					endBeg = len;
//					description="Début du texte [" + trimed.substring(begBeg, endBeg) + "]";
//				} else {
//					endBeg = 160;
//					endEnd = len;
//					begEnd = Math.max(endBeg, endEnd - 160);
//					description="Début du texte [" + trimed.substring(begBeg, endBeg) + "]"
//							+ System.lineSeparator() + "Fin du texte [" + trimed.substring(begEnd, endEnd) + "]";
//				}
//				// add object text content
//				messageNode.addObject(trimed, messageUID,".txt", "TextContent", 1);
				messageNode.addMetadata("TextContent", content, true);
				messageNode.addObject(content, messageUID+".txt", "TextContent", 1);
			}
		} 
		// add object binary master
		messageNode.addObject(rawContent, messageUID+".eml", "BinaryMaster", 1);

		if (writeFlag)
			messageNode.write();

		if (attachments != null && !attachments.isEmpty()) {
			// create all attachments subunits/object groups
			extractMessageAttachments(messageNode, writeFlag);
		}
	}

	/** Extract one standard message attachement. */
	private final void extractOneMessageAttachment(ArchiveUnit messageNode, String filename, byte[] rawContent,
			boolean writeFlag) throws ExtractionException {
		ArchiveUnit attachmentNode;
		String textExtract;

		attachmentNode = new ArchiveUnit(mailBoxFolder.storeExtractor, messageNode, "Attachment", filename);
		attachmentNode.addMetadata("DescriptionLevel", "Item", true);
		attachmentNode.addMetadata("Title", filename, true);
		attachmentNode.addMetadata("Description", "Document \"" + filename + "\" joint au message " + messageUID, true);
		attachmentNode.addObject(rawContent, filename, "BinaryMaster", 1);
		// Text extraction
		try {
			textExtract = FileTextExtractor.getInstance().getText(rawContent);
			if (!((textExtract == null) || textExtract.isEmpty()))
				attachmentNode.addObject(textExtract.getBytes(), filename+".txt", "TextContent", 1);
		} catch (ExtractionException ee) {
			logWarning(
					"mailextract: Can't extract text content from attachment " + filename + " in message " + subject);
		}
		if (writeFlag)
			attachmentNode.write();
	}

	/** Recursively extract attached message. */
	private final void extractAttachedMessage(ArchiveUnit rootNode, DateRange attachedMessagedateRange, String filename,
			byte[] rawContent, boolean writeFlag) throws ExtractionException {
		Level memLevel;

		memLevel = getLogger().getLevel();
		getLogger().setLevel(Level.OFF);
		try {
			JMStoreExtractor rfc822Extractor = new JMStoreExtractor(rawContent, rootNode.getRootPath(),
					rootNode.getName(),

					getStoreExtractor().options, getLogger());
			rfc822Extractor.rootAnalysisMBFolder.extractFolderAsRoot(writeFlag);
			getStoreExtractor().addTotalAttachedMessagesCount(
					rfc822Extractor.getTotalMessagesCount() + rfc822Extractor.getTotalAttachedMessagesCount());
			attachedMessagedateRange.extendRange(rfc822Extractor.rootAnalysisMBFolder.getDateRange());
		} finally {
			getLogger().setLevel(memLevel);
		}
	}

	/** Extract all message attachments. */
	private final void extractMessageAttachments(ArchiveUnit messageNode, boolean writeFlag)
			throws ExtractionException {
		ArchiveUnit rootNode;
		boolean attachedMessage;
		DateRange attachedMessagedateRange;
		boolean isRFC822;

		// prepare an ArchiveUnit to keep all attached message that can be
		// recursively extracted
		rootNode = new ArchiveUnit(mailBoxFolder.storeExtractor, messageNode.getFullName(), "Attached Messages");
		rootNode.addMetadata("DescriptionLevel", "Item", true);
		rootNode.addMetadata("Title", "Messages attachés", true);
		rootNode.addMetadata("Description", "Ensemble des messages attachés joint au message " + messageUID, true);
		attachedMessage = false;
		attachedMessagedateRange = new DateRange();

		for (Attachment a : attachments) {
			// message identification
			try {
				isRFC822 = RFC822Identificator.getInstance().isRFC822(a.rawContent);
			} catch (ExtractionException e) {
				logWarning("mailextract: Error during mimetype identification of file " + a.filename + "in message "
						+ subject);
				isRFC822 = false;
			}
			if (isRFC822) {
				// recursive extraction of a message in attachment...
				attachedMessage = true;
				logWarning("mailextract: Attached message extraction from message " + subject);
				extractAttachedMessage(rootNode, attachedMessagedateRange, a.filename, a.rawContent, writeFlag);
			} else if (writeFlag) {
				// standard attachment file
				extractOneMessageAttachment(messageNode, a.filename, a.rawContent, writeFlag);
			}
		}
		if (attachedMessage && writeFlag) {
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
	// to units/objects extraction method
	public void countMessage() throws ExtractionException {
		// accumulate in folder statistics
		mailBoxFolder.incFolderMessagesCount();
		mailBoxFolder.addFolderMessagesRawSize(getMessageSize());
	}
}
