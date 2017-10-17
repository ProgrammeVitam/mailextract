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

package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;
import com.pff.PSTAttachment;
import com.pff.PSTConversationIndex;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;
import com.pff.PSTConversationIndex.ResponseLevel;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.utils.RFC822Headers;

/**
 * StoreMessage sub-class for mail boxes extracted through libpst library.
 * <p>
 * The java-libpst is a pure java library for the reading of Outlook PST and OST
 * files.
 * <p>
 * This library was originally based off the documentation created through the
 * fantastic reverse engineering effort made by the
 * [libpff](https://sourceforge.net/projects/libpff) project. The library has
 * been improved with information provided by the release of the official PST
 * specs by Microsoft.
 * <p>
 * Thanks to Richard Johnson http://github.com/rjohnsondev
 */
public class PstStoreMessage extends StoreMessage {

	/** Native libpst message. */
	protected PSTMessage message;

	/** The RFC822 headers if any. */
	RFC822Headers rfc822Headers;

	/**
	 * Instantiates a new LP store message.
	 *
	 * @param mBFolder
	 *            Containing MailBoxFolder
	 * @param message
	 *            Native libpst message
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public PstStoreMessage(StoreFolder mBFolder, PSTMessage message) throws ExtractionException {
		super(mBFolder);
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxMessage#getMessageSize()
	 */
	@Override
	public long getMessageSize() {
		return message.getMessageSize();
	}

	// General Headers function

	// test if there's a convenient analyzed rfc822Headers
	private boolean hasRFC822Headers() {
		return (rfc822Headers != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#prepareHeaders()
	 */
	// get the smtp transport header if any
	protected void prepareHeaders() {
		String headerString;

		if (!hasRFC822Headers()) {
			headerString = message.getTransportMessageHeaders();
			if ((headerString != null) && (!headerString.isEmpty()))
				try {
					rfc822Headers = new RFC822Headers(message.getTransportMessageHeaders(), this);
					mailHeader = Collections.list(rfc822Headers.getAllHeaderLines());
					return;
				} catch (MessagingException e) {
					logMessageWarning("mailextract.libpst: Can't decode smtp header");
				}
		}
		rfc822Headers = null;
		mailHeader = null;

	}

	// Subject specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeSubject()
	 */
	protected void analyzeSubject() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] sList = rfc822Headers.getHeader("Subject");
			if (sList != null) {
				if (sList.length > 1)
					logMessageWarning(
							"mailextract.pst: Multiple subjects, keep the first one in header");
				result = RFC822Headers.getHeaderValue(sList[0]);
			}
		} else {
			// pst file value
			result = message.getSubject();
			if (result.isEmpty())
				result = null;

		}

		if (result == null)
			logMessageWarning("mailextract.pst: No subject in header");

		subject = result;
	}

	// MessageID specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeMessageID()
	 */
	protected void analyzeMessageID() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] mList = rfc822Headers.getHeader("message-ID");
			if (mList != null) {
				if (mList.length > 1)
					logMessageWarning("mailextract.pst: Multiple message ID, keep the first one in header");
				result = RFC822Headers.getHeaderValue(mList[0]);
			}
		} else {
			// pst file value
			// generate a messageID from the conversationIndex
			result = message.getInternetMessageId();
			if (result.isEmpty()) {
				PSTConversationIndex pstCI = message.getConversationIndex();

				Instant inst = pstCI.getDeliveryTime().toInstant();
				ZonedDateTime zdt = ZonedDateTime.ofInstant(inst, ZoneOffset.UTC);
				result = "<PST:" + pstCI.getGuid() + "@" + zdt.format(DateTimeFormatter.ISO_DATE_TIME);
				List<ResponseLevel> rlList = pstCI.getResponseLevels();
				for (ResponseLevel rl : rlList) {
					result += "+" + Integer.toHexString(rl.getDeltaCode());
					result += Long.toHexString(rl.getTimeDelta());
					result += Integer.toHexString(rl.getRandom());
				}
				result += ">";
			}
		}

		if (result == null)
			logMessageWarning("mailextract.pst: No Message ID address in header");
		messageID = result;
	}

	// From specific functions

	// get sender name using all possible sources
	private String getSenderName() {
		String result;

		result = message.getSenderName().trim();
		if ((result == null) || result.isEmpty())
			result = message.getSentRepresentingName().trim();

		if (result.isEmpty())
			result = null;
		return result;
	}

	// get sender email address using all possible sources (sender and
	// SentRepresenting field), and using SMTP first
	private String getSenderEmailAddress() {
		String result = "";

		if (message.getSenderAddrtype().equalsIgnoreCase("SMTP"))
			result = message.getSenderEmailAddress().trim();
		if (result.isEmpty() && message.getSentRepresentingAddressType().equalsIgnoreCase("SMTP"))
			result = message.getSentRepresentingEmailAddress().trim();
		if (result.isEmpty())
			result = message.getSenderEmailAddress().trim();
		if (result.isEmpty())
			result = message.getSentRepresentingEmailAddress().trim();

		if (result.isEmpty())
			result = null;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeFrom()
	 */
	protected void analyzeFrom() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] fromList = rfc822Headers.getHeader("From");
			if (fromList != null) {
				if (fromList.length > 1)
					logMessageWarning("mailextract.pst: Multiple From addresses, keep the first one in header");
				result = RFC822Headers.getHeaderValue(fromList[0]);
			}
		} else {
			// pst file value
			String fromAddr = getSenderEmailAddress();
			if (fromAddr != null) {
				String fromName = getSenderName();
				if (fromName != null)
					result = fromName + " <" + fromAddr + ">";
				else
					result = fromAddr;
			}
		}

		if (result == null)
			logMessageWarning("mailextract.pst: No From address in header");

		from = result;
	}

	// Recipients (To,cc,bcc) specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeRecipients()
	 */
	protected void analyzeRecipients() {
		if (hasRFC822Headers()) {
			// smtp header value
			recipientTo = rfc822Headers.getAddressHeader("To");
			recipientCc = rfc822Headers.getAddressHeader("cc");
			recipientBcc = rfc822Headers.getAddressHeader("bcc");
		} else {
			// pst file values
			recipientTo = new ArrayList<String>();
			recipientCc = new ArrayList<String>();
			recipientBcc = new ArrayList<String>();

			int recipientNumber;
			PSTRecipient pstR;
			String normAddress;
			try {
				recipientNumber = message.getNumberOfRecipients();
			} catch (Exception e) {
				logMessageWarning("mailextract.libpst: Can't determine recipient list");
				recipientNumber = 0;
			}
			for (int i = 0; i < recipientNumber; i++) {
				try {
					pstR = message.getRecipient(i);
					// prefer smtp address
					String emailAddress = pstR.getSmtpAddress();
					if (emailAddress.isEmpty())
						emailAddress = pstR.getEmailAddress();
					normAddress = pstR.getDisplayName() + " <" + emailAddress + ">";
					switch (pstR.getRecipientType()) {
					case PSTRecipient.MAPI_TO:
						recipientTo.add(normAddress);
						break;
					case PSTRecipient.MAPI_CC:
						recipientCc.add(normAddress);
						break;
					case PSTRecipient.MAPI_BCC:
						recipientBcc.add(normAddress);
						break;
					}
				} catch (Exception e) {
					logMessageWarning("mailextract.libpst: Can't get recipient number " + Integer.toString(i));
				}
			}
		}
	}

	// ReplyTo specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReplyTo()
	 */
	protected void analyzeReplyTo() {
		List<String> result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			result = rfc822Headers.getAddressHeader("Reply-To");
		}
		// FIXME pst file value

		replyTo = result;
	}

	// Return-Path specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReturnPath()
	 */
	protected void analyzeReturnPath() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] rpList = rfc822Headers.getHeader("Return-Path");
			if (rpList != null) {
				if (rpList.length > 1)
					logMessageWarning(
							"mailextract.pst: Multiple Return-Path addresses, keep the first one in header");
				result = RFC822Headers.getHeaderValue(rpList[0]);
			}
		} else {
			// pst file value
			result = message.getReturnPath();
			if (result.isEmpty())
				result = null;
		}

		if (result == null)
			logMessageWarning("mailextract.pst: No Return-Path address in header");

		returnPath = result;
	}

	// Dates specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeDates()
	 */
	protected void analyzeDates() {
		receivedDate = message.getMessageDeliveryTime();
		sentDate = message.getClientSubmitTime();
	}

	// In-reply-to and References specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeInReplyToId(
	 * )
	 */
	protected void analyzeInReplyToId() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] irtList = rfc822Headers.getHeader("In-Reply-To");
			if (irtList != null) {
				if (irtList.length > 1)
					logMessageWarning(
							"mailextract.pst: Multiple In-Reply-To identifiers, keep the first one in header");
				result = RFC822Headers.getHeaderValue(irtList[0]);
			}
		} else {
			// pst file value
			result = message.getInReplyToId();
			if (result.isEmpty()) {
				if (messageID == null)
					analyzeMessageID();
				if ((messageID != null) && messageID.startsWith("<PST:")) {
					if (messageID.lastIndexOf('+') > messageID.lastIndexOf('@')) {
						result = messageID.substring(0, messageID.lastIndexOf('+')) + ">";
					} else
						result = null;
				}
			}
		}

		inReplyToUID = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReferences()
	 */
	protected void analyzeReferences() {
		List<String> result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			result = rfc822Headers.getReferences();
		}
		// FIXME pst file value with at least in-reply-to

		references = result;
	}

	// Content analysis methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeBodies()
	 */
	protected void analyzeBodies() {
		String result;

		// text
		result = message.getBody();
		if (result.isEmpty())
			result = null;
		bodyContent[TEXT_BODY] = result;

		// html
		result = message.getBodyHTML();
		if (result.isEmpty())
			result = null;
		bodyContent[HTML_BODY] = result;

		// rtf
		try {
			result = message.getRTFBody();
			if (result.isEmpty())
				result = null;
		} catch (PSTException | IOException e) {
			result = null;
			// forget it
		}
		bodyContent[RTF_BODY] = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeAttachments(
	 * )
	 */
	protected void analyzeAttachments() {
		List<StoreMessageAttachment> result = new ArrayList<StoreMessageAttachment>();
		int attachmentNumber;
		PSTAttachment pstA;
		try {
			attachmentNumber = message.getNumberOfAttachments();
		} catch (Exception e) {
			logMessageWarning("mailextract.libpst: Can't determine attachment list");
			attachmentNumber = 0;
		}
		for (int i = 0; i < attachmentNumber; i++) {
			try {
				StoreMessageAttachment attachment;

				pstA = message.getAttachment(i);
				switch (pstA.getAttachMethod()) {
				case PSTAttachment.ATTACHMENT_METHOD_NONE:
					break;
				// TODO OLE case you can access the IStorage object through
				// IAttach::OpenProperty(PR_ATTACH_DATA_OBJ, ...)
				case PSTAttachment.ATTACHMENT_METHOD_OLE:
					logMessageWarning("mailextract.libpst: Can't extract OLE attachment");
					break;
				case PSTAttachment.ATTACHMENT_METHOD_BY_VALUE:
					InputStream is = pstA.getFileInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						baos.write(buf, 0, bytesRead);
					}
					attachment = new StoreMessageAttachment(pstA.getLongFilename(), baos.toByteArray(),
							pstA.getCreationTime(), pstA.getModificationTime(), pstA.getMimeTag(), pstA.getContentId(),
							StoreMessageAttachment.INLINE_ATTACHMENT);
					result.add(attachment);
					break;
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_ONLY:
					// TODO reference cases
					logMessageWarning("mailextract.libpst: Can't extract reference attachment");
					break;
				case PSTAttachment.ATTACHMENT_METHOD_EMBEDDED:
					PSTMessage message = pstA.getEmbeddedPSTMessage();
					String name = pstA.getLongFilename();
					if (name.isEmpty())
						name = pstA.getFilename();
					if (name.isEmpty())
						name = pstA.getDisplayName();
					attachment = new StoreMessageAttachment(name, message, pstA.getCreationTime(),
							pstA.getModificationTime(), pstA.getMimeTag(), pstA.getContentId(),
							StoreMessageAttachment.STORE_ATTACHMENT
									+ StoreMessageAttachment.EMBEDDEDPST_STORE_ATTACHMENT);
					result.add(attachment);
					break;
				}
			} catch (Exception e) {
				logMessageWarning("mailextract.libpst: Can't get attachment number " + Integer.toString(i));
			}
		}

		attachments = result;
	}

	// Global message

	/**
	 * Gets the native mime content.
	 *
	 * @return the native mime content
	 */
	protected byte[] getNativeMimeContent() {
		return null;
	}

}
