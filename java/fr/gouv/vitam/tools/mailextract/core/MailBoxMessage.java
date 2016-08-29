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

package fr.gouv.vitam.tools.mailextract.core;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
	 * and then write them on disk.
	 * <p>
	 * This is "the" method where the extraction structure and content is mainly
	 * defined (see also {@link MailBoxFolder#extractFolder extractFolder} and
	 * {@link StoreExtractor#extractAllFolders extractAllFolders}).
	 * 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public final void extractMessage() throws ExtractionException {
		ArchiveUnit messageNode = null;
		ArchiveUnit attachmentNode, contentNode;

		// create message unit
		messageNode = new ArchiveUnit(mailBoxFolder.storeExtractor, mailBoxFolder.folderArchiveUnit, "Message",
				subject);
		messageNode.addArrayOneMetadata("Title", subject, true);
		messageNode.addSubKeyArrayListMetadata("Writer", "Identifier", from, true);
		messageNode.addSubKeyArrayListMetadata("Addressee", "Identifier", recipientTo, true);
		messageNode.addSubKeyArrayListMetadata("Recipient", "Identifier", recipientCcAndBcc, false);
		messageNode.addMetadata("OriginatingSystemId", messageUID, true);
		messageNode.addMetadata("OriginatingSystemId-ReplyTo", inReplyToUID, false);
		messageNode.addArrayMetadata("OriginatingSystemId-References", references, false);
		messageNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), true);
		messageNode.addMetadata("ReceivedDate", DateRange.getISODateString(receivedDate), false);
		messageNode.addMetadata("DescriptionLevel", "Item", true);

		// not in the Vitam specs... (to be discussed)
		messageNode.addSubKeyArrayListMetadata("Sender", "Identifier", sender, false);
		messageNode.addSubKeyArrayListMetadata("ReplyTo", "Identifier", replyTo, false);
		messageNode.addSubKeyArrayListMetadata("ReturnPath", "Identifier", returnPath, false);
		messageNode.addArrayMetadata("MailHeader", mailHeader, false);

		messageNode.write();

		// create body subunit/object group
		contentNode = new ArchiveUnit(mailBoxFolder.storeExtractor, messageNode, null, "Body");
		contentNode.addArrayOneMetadata("Title", "Corps du message " + messageUID, true);
		contentNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), true);
		contentNode.addMetadata("DescriptionLevel", "Item", true);

		contentNode.addObject(rawContent, "object", "BinaryMaster", 1);
		if ((textContent != null) && !textContent.isEmpty()) {
			contentNode.addObject(textContent, "object", "TextContent", 1);
		}
		contentNode.write();

		// create all attachments subunits/object groups
		if (attachments != null && !attachments.isEmpty()) {
			for (Attachment a : attachments) {
				attachmentNode = new ArchiveUnit(mailBoxFolder.storeExtractor, messageNode, "Attachment", a.filename);
				attachmentNode.addArrayOneMetadata("Title",
						"Document \"" + a.filename + "\" joint au message " + messageUID, true);
				attachmentNode.addObject(a.rawContent, a.filename, "BinaryMaster", 1);
				attachmentNode.addMetadata("DescriptionLevel", "Item", true);

				attachmentNode.write();
			}
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
