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

package fr.gouv.vitam.tools.mailextract.lib.store.microsoft;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;

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
public abstract class MicrosoftStoreMessage extends StoreMessage {

	/** The RFC822 headers if any. */
	protected RFC822Headers rfc822Headers;

	/** The attachments list. */
	protected MicrosoftStoreMessageAttachment[] nativeAttachments;

	/**
	 * Instantiates a new LP store message.
	 *
	 * @param mBFolder
	 *            Containing MailBoxFolder
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public MicrosoftStoreMessage(StoreFolder mBFolder) {
		super(mBFolder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxMessage#getMessageSize()
	 */
	@Override
	public long getMessageSize() {
		long result;
		result = getNativeMessageSize();
		if ((result == 0) && (mimeContent != null))
			result = mimeContent.length;
		return result;
	}

	// Field codes
	// public static final int MESSAGE_CLASS = 0x001a;
	// public static final int SMTP_TRANSPORT_HEADER = 0x007d;
	// public static final int SUBJECT = 0x0037;
	// public static final int INTERNET_MESSAGE_ID = 0x1035;
	// public static final int SENDER_NAME = 0x0c1a;
	// public static final int SENT_REPRESENTING_NAME = 0x0042;
	// public static final int SENDER_ADDR_TYPE = 0x0c1e;
	// public static final int SENDER_EMAIL_ADDRESS = 0x0c1f;
	// public static final int SENT_REPRESENTING_ADDR_TYPE = 0x0064;
	// public static final int SENT_REPRESENTING_EMAIL_ADDRESS = 0x0065;
	// public static final int RETURN_PATH = 0x1046;
	// public static final int MESSAGE_DELIVERY_TIME = 0x0e06;
	// public static final int CLIENT_SUBMIT_TIME = 0x0039;
	// public static final int IN_REPLY_TO_ID = 0x1042;
	// public static final int MESSAGE_SIZE = 0x0e08;

	// // return the native message size
	// abstract protected long getNativeMessageSize();
	//
	// // return empty String if no item
	// abstract protected String getNativeStringItem(int item);
	//
	// // return null if no item
	// abstract protected byte[] getNativeByteItem(int item);
	//
	// // return null if no item
	// abstract protected Date getNativeDateItem(int item);

	// Native message fields access functions

	abstract protected String getNativeSmtpTransportHeader();

	abstract protected String getNativeSubject();

	abstract protected String getNativeInternetMessageId();

	abstract protected String getNativeSenderName();

	abstract protected String getNativeSentRepresentingName();

	abstract protected String getNativeSenderAddrType();

	abstract protected String getNativeSenderEmailAddress();

	abstract protected String getNativeSentRepresentingAddrType();

	abstract protected String getNativeSentRepresentingEmailAddress();

	abstract protected String getNativeReturnPath();

	abstract protected Date getNativeMessageDeliveryTime();

	abstract protected Date getNativeClientSubmitTime();

	abstract protected String getNativeInReplyToId();

	abstract protected long getNativeMessageSize();

	// Native message ConversationIndex access functions
	abstract protected boolean hasNativeConversationIndex();

	abstract protected Date getNativeCIDeliveryTime();

	abstract protected UUID getNativeCIGuid();

	abstract protected int getNativeCINumberOfResponseLevels();

	abstract protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber);

	abstract protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber);

	abstract protected short getNativeCIResponseLevelRandom(int responseLevelNumber);

	// Native message Recipients access functions
	abstract protected int getNativeNumberOfRecipients();

	abstract protected String getNativeRecipientsSmtpAddress(int recipientNumber);

	abstract protected String getNativeRecipientsEmailAddress(int recipientNumber);

	abstract protected String getNativeRecipientsDisplayName(int recipientNumber);

	abstract protected int getNativeRecipientsType(int recipientNumber);

	// Native message body access functions
	abstract protected String getNativeBodyText();

	abstract protected String getNativeBodyHTML();

	abstract protected String getNativeRTFBody();

	// Native message attachment access functions
	abstract protected MicrosoftStoreMessageAttachment[] getNativeAttachments();
	// abstract protected int getNativeNumberOfAttachments();
	//
	// abstract protected int getNativeAttachmentAttachMethod(int
	// attachmentNumber);
	//
	// abstract protected byte[] getNativeAttachmentByteArray(int
	// attachmentNumber);
	//
	// abstract protected String getNativeAttachmentLongFilename(int
	// attachmentNumber);
	//
	// abstract protected String getNativeAttachmentFilename(int
	// attachmentNumber);
	//
	// abstract protected String getNativeAttachmentDisplayName(int
	// attachmentNumber);
	//
	// abstract protected Date getNativeAttachmentCreationTime(int
	// attachmentNumber);
	//
	// abstract protected Date getNativeAttachmentModificationTime(int
	// attachmentNumber);
	//
	// abstract protected String getNativeAttachmentMimeTag(int
	// attachmentNumber);
	//
	// abstract protected String getNativeAttachmentContentId(int
	// attachmentNumber);
	//
	// abstract protected Object getNativeAttachmentEmbeddedMessage(int
	// attachmentNumber);

	abstract protected String getEmbeddedMessageScheme();

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
	protected void prepareAnalyze() {
		String headerString;

		if (!hasRFC822Headers()) {
			headerString = getNativeSmtpTransportHeader();
			if ((headerString != null) && (!headerString.isEmpty()))
				try {
					rfc822Headers = new RFC822Headers(headerString, this);
					mailHeader = Collections.list(rfc822Headers.getAllHeaderLines());
				} catch (MessagingException e) {
					logMessageWarning("mailextract.microsoft: Can't decode smtp header");
					rfc822Headers = null;
					mailHeader = null;
				}
		}
		nativeAttachments = getNativeAttachments();
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
					logMessageWarning("mailextract.microsoft: Multiple subjects, keep the first one in header");
				result = RFC822Headers.getHeaderValue(sList[0]);
			}
		} else {
			// pst file value
			result = getNativeSubject();

			// FIXME filtering in LibPST (aim 0x0101, 0105 and 0110 values)
			// to be verified why and if usefull in msg
			if ((result.length() >= 2) && result.charAt(0) == 0x01) {
				if (result.length() == 2) {
					result = "";
				} else {
					result = result.substring(2, result.length());
				}
			}
			if (result.isEmpty())
				result = null;
		}
		if (result == null)
			logMessageWarning("mailextract.microsoft: No subject in header");

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
					logMessageWarning("mailextract.microsoft: Multiple message ID, keep the first one in header");
				result = RFC822Headers.getHeaderValue(mList[0]);
			}
		} else {
			// pst file value
			// generate a messageID from the conversationIndex
			try {
				result = getNativeInternetMessageId();
				if (result.isEmpty()) {
					if (hasNativeConversationIndex()) {
						Instant inst = getNativeCIDeliveryTime().toInstant();
						ZonedDateTime zdt = ZonedDateTime.ofInstant(inst, ZoneOffset.UTC);
						result = "<MIC:" + getNativeCIGuid() + "@" + zdt.format(DateTimeFormatter.ISO_DATE_TIME);
						int responseLevelNumber = getNativeCINumberOfResponseLevels();
						for (int i = 0; i < responseLevelNumber; i += 1) {
							result += "+" + Integer.toHexString(getNativeCIResponseLevelDeltaCode(i));
							result += Long.toHexString(getNativeCIResponseLevelTimeDelta(i));
							result += Integer.toHexString(getNativeCIResponseLevelRandom(i));
						}
						result += ">";
					}
				}
			}
			// FIXME AN pst to test
			catch (Exception e) {
				result = null;
			}
		}
		if (result == null) {
			logMessageWarning("mailextract.microsoft: No Message ID address in header");
			result = "NoMessageID";
		}
		messageID = result;
	}

	// From specific functions

	// get sender name using all possible sources
	private String getSenderName() {
		String result;

		result = getNativeSenderName();
		if ((result == null) || result.isEmpty())
			result = getNativeSentRepresentingName();

		if (result.isEmpty())
			result = null;
		return result;
	}

	// get sender email address using all possible sources (sender and
	// SentRepresenting field), and using SMTP first
	private String getSenderEmailAddress() {
		String result = "";

		if (getNativeSenderAddrType().equalsIgnoreCase("SMTP"))
			result = getNativeSenderEmailAddress();
		if (result.isEmpty() && getNativeSentRepresentingAddrType().equalsIgnoreCase("SMTP"))
			result = getNativeSentRepresentingEmailAddress();
		if (result.isEmpty())
			result = getNativeSenderEmailAddress();
		if (result.isEmpty())
			result = getNativeSentRepresentingEmailAddress();

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
					logMessageWarning("mailextract.microsoft: Multiple From addresses, keep the first one in header");
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
			logMessageWarning("mailextract.microsoft: No From address in header");

		from = result;
	}

	// Recipients (To,cc,bcc) specific functions

	public static final int MAPI_TO = 1;
	public static final int MAPI_CC = 2;
	public static final int MAPI_BCC = 3;

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
			String normAddress;
			try {
				recipientNumber = getNativeNumberOfRecipients();
			} catch (Exception e) {
				logMessageWarning("mailextract.microsoft: Can't determine recipient list");
				recipientNumber = 0;
			}
			for (int i = 0; i < recipientNumber; i++) {
				try {
					// prefer smtp address
					String emailAddress = getNativeRecipientsSmtpAddress(i);
					if (emailAddress.isEmpty())
						emailAddress = getNativeRecipientsEmailAddress(i);
					normAddress = getNativeRecipientsDisplayName(i) + " <" + emailAddress + ">";
					switch (getNativeRecipientsType(i)) {
					case MAPI_TO:
						recipientTo.add(normAddress);
						break;
					case MAPI_CC:
						recipientCc.add(normAddress);
						break;
					case MAPI_BCC:
						recipientBcc.add(normAddress);
						break;
					}
				} catch (Exception e) {
					logMessageWarning("mailextract.microsoft: Can't get recipient number " + Integer.toString(i));
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
		// FIXME microsoft file value

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
							"mailextract.microsoft: Multiple Return-Path addresses, keep the first one in header");
				result = RFC822Headers.getHeaderValue(rpList[0]);
			}
		}
		// if not in the SMTP header there's no microsoft version

		if (result == null) {
			// logMessageWarning("mailextract.microsoft: No Return-Path address
			// in header");
		}
		returnPath = result;
	}

	// Dates specific functions

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeDates()
	 */
	protected void analyzeDates() {
		receivedDate = getNativeMessageDeliveryTime();
		sentDate = getNativeClientSubmitTime();
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
							"mailextract.microsoft: Multiple In-Reply-To identifiers, keep the first one in header");
				result = RFC822Headers.getHeaderValue(irtList[0]);
			}
		} else {
			// pst file value
			result = getNativeInReplyToId();
			if (result.isEmpty()) {
				if (messageID == null)
					analyzeMessageID();
				if ((messageID != null) && messageID.startsWith("<MIC:")) {
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
		result = getNativeBodyText();
		if (result.isEmpty())
			result = null;
		bodyContent[TEXT_BODY] = result;

		// html
		result = getNativeBodyHTML();
		if (result.isEmpty())
			result = null;
		bodyContent[HTML_BODY] = result;

		// rtf
		result = getNativeRTFBody();
		if (result.isEmpty())
			result = null;
		bodyContent[RTF_BODY] = result;
	}

	public static final int ATTACHMENT_METHOD_NONE = 0;
	public static final int ATTACHMENT_METHOD_BY_VALUE = 1;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE = 2;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE = 3;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE_ONLY = 4;
	public static final int ATTACHMENT_METHOD_EMBEDDED = 5;
	public static final int ATTACHMENT_METHOD_OLE = 6;

	// try to get the best attachment name
	private String getAttachementFilename(int attachmentNumber) {
		String result = "";

		result = nativeAttachments[attachmentNumber].longFilename;
		if (result.isEmpty())
			result = nativeAttachments[attachmentNumber].filename;
		if (result.isEmpty())
			result = nativeAttachments[attachmentNumber].displayName;

		return result;
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
		try {
			attachmentNumber = nativeAttachments.length;
		} catch (Exception e) {
			logMessageWarning("mailextract.microsoft: Can't determine attachment list");
			attachmentNumber = 0;
		}
		for (int i = 0; i < attachmentNumber; i++) {
			try {
				StoreMessageAttachment attachment;

				switch (nativeAttachments[i].attachMethod) {
				case ATTACHMENT_METHOD_NONE:
					break;
				// TODO OLE case you can access the IStorage object through
				// IAttach::OpenProperty(PR_ATTACH_DATA_OBJ, ...)
				case ATTACHMENT_METHOD_OLE:
					logMessageWarning("mailextract.microsoft: Can't extract OLE attachment");
					break;
				case ATTACHMENT_METHOD_BY_VALUE:
					attachment = new StoreMessageAttachment(nativeAttachments[i].byteArray, "file",
							getAttachementFilename(i), nativeAttachments[i].creationTime,
							nativeAttachments[i].modificationTime, nativeAttachments[i].mimeTag,
							nativeAttachments[i].contentId, StoreMessageAttachment.INLINE_ATTACHMENT);
					result.add(attachment);
					break;
				case ATTACHMENT_METHOD_BY_REFERENCE:
				case ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE:
				case ATTACHMENT_METHOD_BY_REFERENCE_ONLY:
					// TODO reference cases
					logMessageWarning("mailextract.microsoft: Can't extract reference attachment");
					break;
				case ATTACHMENT_METHOD_EMBEDDED:
					attachment = new StoreMessageAttachment(nativeAttachments[i].embeddedMessage,
							getEmbeddedMessageScheme(), getAttachementFilename(i), nativeAttachments[i].creationTime,
							nativeAttachments[i].modificationTime, nativeAttachments[i].mimeTag,
							nativeAttachments[i].contentId, StoreMessageAttachment.STORE_ATTACHMENT);
					result.add(attachment);
					break;
				}
			} catch (Exception e) {
				logMessageWarning("mailextract.microsoft: Can't get attachment number " + Integer.toString(i));
			}
		}

		attachments = result;
	}

	// Global message

	/**
	 * Gets the native mime content.
	 *
	 * @return the native mime content which is always null
	 */
	protected byte[] getNativeMimeContent() {
		return null;
	}

}
