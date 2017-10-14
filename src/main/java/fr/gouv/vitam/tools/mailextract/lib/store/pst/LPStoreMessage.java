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

package fr.gouv.vitam.tools.mailextract.lib.store.pst;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

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
public class LPStoreMessage extends StoreMessage {

	/** Native libpst message. */
	protected PSTMessage message;

	// /** The headers. */
	// LinkedHashMap<CaseUnsensString, String> headers;
	//

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
	public LPStoreMessage(StoreFolder mBFolder, PSTMessage message) throws ExtractionException {
		super(mBFolder);
		this.message = message;
		this.rfc822Headers=null;
	}

	private boolean hasRFC822Headers() {
		return (rfc822Headers != null);
	}
	
	private void analyzeHeaders(){
		String headerString;
		
		headerString=message.getTransportMessageHeaders();
		
		if ((headerString!=null)&&(!headerString.isEmpty()))
			try {
				rfc822Headers=new RFC822Headers(message.getTransportMessageHeaders());
			} catch (MessagingException e) {
				logWarning("mailextract.libpst: Can't decode smtp header in message " + subject);
			}
	}

	// get recipients from smtp header
	// or if empty from Microsoft Format in pst file
	private void getRecipients() {
		// smtp header value
		if (hasRFC822Headers()) {
			recipientTo = rfc822Headers.getAddressHeader("To");
			recipientCc = rfc822Headers.getAddressHeader("cc");
			recipientBcc = rfc822Headers.getAddressHeader("bcc");
		} else {
			recipientTo = new ArrayList<String>();
			recipientCc = new ArrayList<String>();
			recipientBcc = new ArrayList<String>();

			// pst file values
			int recipientNumber;
			PSTRecipient pstR;
			String normAddress;
			try {
				recipientNumber = message.getNumberOfRecipients();
			} catch (Exception e) {
				logWarning("mailextract.libpst: Can't determine recipient list in message " + subject);
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
					logWarning("mailextract.libpst: Can't get recipient number " + Integer.toString(i) + " in message "
							+ subject);
				}
			}
		}
	}

	// get sender name using all possible sources
	private String getSenderName() {
		String name;

		name = message.getSenderName();
		if ((name == null) || name.isEmpty())
			name = message.getSentRepresentingName();
		return name;
	}

	// get sender email address using all possible sources (sender and
	// SentRepresenting field), and using SMTP first
	private String getSenderEmailAddress() {
		String name = "";

		if (message.getSenderAddrtype().equalsIgnoreCase("SMTP"))
			name = message.getSenderEmailAddress().trim();
		if (name.isEmpty() && message.getSenderAddrtype().equalsIgnoreCase("SMTP"))
			name = message.getSentRepresentingEmailAddress().trim();
		if (name.isEmpty())
			name = message.getSenderEmailAddress().trim();
		if (name.isEmpty())
			name = message.getSentRepresentingEmailAddress().trim();

		return name;
	}

	// get from from smtp header
	// or if empty from Microsoft Format in pst file
	private String getFrom() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] fromList = rfc822Headers.getHeader("From");
			if (fromList != null) {
				if (fromList.length > 1)
					logWarning("mailextract.javamail: Multiple From addresses in header of message " + subject
							+ ", keep the first one");
				result = RFC822Headers.getHeaderValue(fromList[0]);
			}
		} else {
			// pst file value
			String fromAddr = getSenderEmailAddress();
			if (fromAddr != null && !fromAddr.isEmpty()) {
				String fromName = getSenderName();
				if (fromName != null && !fromName.isEmpty())
					result = fromName + " <" + fromAddr + ">";
				else
					result = fromAddr;
			}
		}
		
		if (result == null)
			logWarning("mailextract.javamail: No From address in header of message " + subject);

		return result;
	}

	// get reply address list from smtp header
	private List<String> getReplyTo() {
		List<String> result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			result = rfc822Headers.getAddressHeader("Reply-To");
		}
		// FIXME pst file value

		return result;
	}

	// get references address list from smtp header
	private List<String> getReferences() {
		List<String> result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			result = rfc822Headers.getReferences();
		}
		// FIXME pst file value with at list in-reply-to

		return result;
	}

	// get return-Path from SMTP
	// or if empty from Microsoft Format in pst file
	private String getReturnPath() {
		String result = null;

		if (hasRFC822Headers()) {
			// smtp header value
			String[] rpList = rfc822Headers.getHeader("Return-Path");
			if (rpList != null) {
				if (rpList.length > 1)
					logWarning("mailextract.javamail: Multiple Return-Path addresses in header of message " + subject
							+ ", keep the first one");
				result = RFC822Headers.getHeaderValue(rpList[0]);
			}
		} else {
			// pst file value
			result = message.getReturnPath();
			if (result.isEmpty())
				result = null;
		}
		
		if (result == null)
			logWarning("mailextract.javamail: No Return-Path address in header of message " + subject);
		
		return result;

	}
	
	private List<String> getStringsFormatHeader(){
		List<String> result;
		
		if (hasRFC822Headers()) {
			result=Collections.list(rfc822Headers.getAllHeaderLines());
		}
		else result=null;
		
		return result;
	}

	// Content analysis methods

	// get attachments with raw content and filename, mimetype... when possible
	private List<StoreMessageAttachment> getAttachments() {
		List<StoreMessageAttachment> lAttachment = new ArrayList<StoreMessageAttachment>();
		int attachmentNumber;
		PSTAttachment pstA;
		try {
			attachmentNumber = message.getNumberOfAttachments();
		} catch (Exception e) {
			logWarning("mailextract.libpst: Can't determine attachment list in message " + subject);
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
					logWarning("mailextract.libpst: Can't extract OLE attachment " + subject);
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
							INLINE_ATTACHMENT);
					lAttachment.add(attachment);
					break;
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_ONLY:
					// TODO reference cases
					logWarning("mailextract.libpst: Can't extract reference attachment " + subject);
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
							STORE_ATTACHMENT + EMBEDDEDPST_STORE_ATTACHMENT);
					lAttachment.add(attachment);
					break;
				}
			} catch (Exception e) {
				logWarning("mailextract.libpst: Can't get attachment number " + Integer.toString(i) + " in message "
						+ subject);
			}
		}
		return lAttachment;

	}

	// get message ID from SMTP or the conversation index for PST
	private String getMessageUId() {
		String result;

		result = message.getInternetMessageId();
		if ((result == null) || result.isEmpty()) {
			PSTConversationIndex pstCI = message.getConversationIndex();

			Instant inst = pstCI.getDeliveryTime().toInstant();
			ZonedDateTime zdt = ZonedDateTime.ofInstant(inst, ZoneOffset.UTC);
			result = "<" + pstCI.getGuid() + "@" + zdt.format(DateTimeFormatter.ISO_DATE_TIME);
			List<ResponseLevel> rlList = pstCI.getResponseLevels();
			for (ResponseLevel rl : rlList) {
				result += "+" + Integer.toHexString(rl.getDeltaCode());
				result += Long.toHexString(rl.getTimeDelta());
				result += Integer.toHexString(rl.getRandom());
			}
			result += ">";
		}
		return result;
	}

	// get in-reply-to ID from SMTP or from the messageUId which is a
	// conversation index for PST
	private String getInReplyToId() {
		String result;

		result = message.getInReplyToId();
		if ((result == null) || result.isEmpty()) {
			if ((messageUID != null) || messageUID.startsWith("PST:")) {
				if (messageUID.lastIndexOf('+') > messageUID.lastIndexOf('@')) {
					result = messageUID.substring(0, messageUID.lastIndexOf('+')) + ">";
				}
			}
		}
		return result;
	}

	public void doAnalyzeMessage() throws ExtractionException {
		// header metadata extraction
		// * special global
		subject = message.getSubject();

		// header content extraction
		analyzeHeaders();
		mailHeader = getStringsFormatHeader();

		// * recipients and co
		from = getFrom();
		replyTo = getReplyTo();

		getRecipients();
		returnPath = getReturnPath();

		// * dates
		receivedDate = message.getMessageDeliveryTime();
		sentDate = message.getClientSubmitTime();

		bodyContent[TEXT_BODY] = message.getBody();
		bodyContent[HTML_BODY] = message.getBodyHTML();
		try {
			bodyContent[RTF_BODY] = message.getRTFBody();
		} catch (PSTException | IOException e) {
			bodyContent[RTF_BODY] = null;
			// forget it
		}
		attachments = getAttachments();

		// no raw content, will be constructed at StoreMessage level
		mimeContent = null;

		messageUID = getMessageUId();
		inReplyToUID = getInReplyToId();
		references = getReferences();
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

}
