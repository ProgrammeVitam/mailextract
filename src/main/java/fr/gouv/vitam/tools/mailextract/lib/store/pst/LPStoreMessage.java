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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import com.pff.PSTAttachment;
import com.pff.PSTConversationIndex;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;
import com.pff.PSTConversationIndex.ResponseLevel;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;

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

	/** The headers. */
	LinkedHashMap<CaseUnsensString, String> headers;

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
	}

	// Header analysis methods

	// direct headers analysis to complement libpst when useful
	// use utility class CaseUnsensString to mix and search without
	// case sensivity header lines like in JavaMail
	private class CaseUnsensString {

		String inner;
		String lower;

		CaseUnsensString(String s) {
			inner = s;
			lower = inner.toLowerCase();
		}

		public int hashCode() {
			return lower.hashCode();
		}

		public String toString() {
			return inner;
		}

		public boolean equals(Object o) {
			if (o instanceof CaseUnsensString)
				return lower.equals(((CaseUnsensString) o).lower);
			else
				return false;
		}
	}

	// get all headers HashMap but with non case sensitive keys
	// don't have to differentiate to,To and TO header...
	private LinkedHashMap<CaseUnsensString, String> getHeaders() {
		LinkedHashMap<CaseUnsensString, String> headers = new LinkedHashMap<CaseUnsensString, String>();
		String line, nextline, value;
		CaseUnsensString key;

		try {
			// ByteArrayInputStream bais = new
			// ByteArrayInputStream(message.toString().getBytes());
			ByteArrayInputStream bais = new ByteArrayInputStream(message.getTransportMessageHeaders().getBytes());
			BufferedReader br = new BufferedReader(new InputStreamReader(bais));

			line = br.readLine();
			while (line != null) {
				if (line.isEmpty()) {
					line = br.readLine();
					continue;
				} else if (line.charAt(0) == '{')
					break;
				nextline = br.readLine();
				// take in header value all line after beginning by space or \t
				while (nextline != null && !nextline.isEmpty()
						&& (nextline.charAt(0) == ' ' || nextline.charAt(0) == '\t')) {
					line += " " + nextline.trim();
					nextline = br.readLine();
				}
				int i = line.indexOf(':');
				if (i > 0) {
					// should always be if not line dropped
					key = new CaseUnsensString(line.substring(0, i).trim());
					value = line.substring(i + 1).trim();
					if (headers.containsKey(key))
						headers.put(key, headers.get(key) + "," + value);
					else
						headers.put(key, value);
				}
				line = nextline;
			}
		} catch (IOException e) {
			logWarning("mailextract.libpst: Can't extract complete mail header from message " + subject);
		}
		return headers;
	}

	// use headers and JavaMail to complement libpst results

	// utilities
	static private String getElementalStringAddress(InternetAddress address) {
		String result;
		String s;

		if (address != null) {
			s = address.getPersonal();
			if (s != null)
				result = s + " ";
			else
				result = "";
			s = address.getAddress();
			if (s != null)
				result += "<" + s + ">";
		} else
			result = "";
		return result;
	}

	static private String getStringAddress(InternetAddress address) {
		String result;

		if (address != null) {
			try {
				result = getElementalStringAddress(address);
				// special case of group address (RFC 2822)
				if (address.isGroup()) {
					result += ":";
					InternetAddress[] group = address.getGroup(false);
					for (int k = 0; k < group.length; k++) {
						if (k > 0)
							result += ",";
						result += getElementalStringAddress(group[k]);
					}
				}
			} catch (AddressException e) {
				// not supposed to be
				result = "";
			}
		} else
			result = "";
		return result;
	}

	// get all headers decoded
	private List<String> getDecodedHeader() throws ExtractionException {
		List<String> result = null;
		String value;

		if (headers != null) {
			Set<CaseUnsensString> keySet = headers.keySet();
			result = new ArrayList<String>();
			for (CaseUnsensString key : keySet) {
				try {
					value = MimeUtility.decodeText(headers.get(key));
				} catch (UnsupportedEncodingException e) {
					// keep original value
					value = headers.get(key);
				}
				result.add(key + ": " + value);
			}
		}
		return result;
	}

	// to get references
	private List<String> getReferences() {
		List<String> refList = null;
		if (headers != null) {
			String refString = headers.get(new CaseUnsensString("References"));
			if (refString != null) {
				String[] l = refString.split(",");
				refList = new ArrayList<String>();
				for (String s : l)
					refList.add(s);
			}
		}
		return refList;
	}

	// to get addresses in headers with parsing control relaxed
	// Used only for ReplyTo
	private List<String> getAddressHeader(String name) {
		List<String> addressList = new ArrayList<String>();
		String addressHeaderString = headers.get(new CaseUnsensString(name));
		if (addressHeaderString != null) {
			InternetAddress[] iAddressArray = null;
			try {
				iAddressArray = InternetAddress.parseHeader(addressHeaderString, false);
			} catch (AddressException e) {
				try {
					// try at least to Mime decode
					addressHeaderString = MimeUtility.decodeText(addressHeaderString);
				} catch (UnsupportedEncodingException uee) {
					// too bad
				}
				logWarning("mailextract.javamail: Wrongly formatted address " + addressHeaderString + " in header "
						+ name + " of message " + subject + ", keep raw address list in metadata");
				addressList.add(addressHeaderString);
				return addressList;
			}
			if (iAddressArray != null) {
				for (InternetAddress ia : iAddressArray) {
					addressList.add(getStringAddress(ia));
				}
			}
		}
		return addressList;
	}

	// get recipients with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private void getRecipients() {
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

	// get from with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private String getFrom() {
		// pst file value
		String from = getSenderEmailAddress();
		if (from != null && !from.isEmpty()) {
			from = getSenderName() + " <" + from + ">";
		}

		// if no result let's try with header values
		if (from == null || from.isEmpty()) {
			List<String> headerFrom = getAddressHeader("From");
			if (headerFrom.size() == 0) {
				logWarning("mailextract.javamail: No From address in header of message " + subject);
				from = "";
			} else {
				if (headerFrom.size() > 1)
					logWarning("mailextract.javamail: Multiple From addresses in header of message " + subject
							+ ", keep the first one");
				from = headerFrom.get(0);
			}
		}
		return from;
	}

	// get from with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private List<String> getReplyTo() {
		// FIXME pst file value
		List<String> replyTo = null;

		// if no result let's try with header values
		if (replyTo == null || replyTo.isEmpty()) {
			replyTo = getAddressHeader("Reply-To");
		}

		return replyTo;
	}

	// Content analysis methods

	// get attachments with raw content and filename, mimetype... when possible
	private List<Attachment> getAttachments() {
		List<Attachment> lAttachment = new ArrayList<Attachment>();
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
				Attachment attachment;

				pstA = message.getAttachment(i);
				switch (pstA.getAttachMethod()) {
				case PSTAttachment.ATTACHMENT_METHOD_NONE:
					break;
				case PSTAttachment.ATTACHMENT_METHOD_BY_VALUE:
					// TODO insure OLE case is the same
				case PSTAttachment.ATTACHMENT_METHOD_OLE:
					InputStream is = pstA.getFileInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						baos.write(buf, 0, bytesRead);
					}
					attachment = new Attachment(pstA.getLongFilename(), baos.toByteArray(), pstA.getCreationTime(),
							pstA.getModificationTime(), pstA.getMimeTag(), pstA.getContentId(), INLINE_ATTACHMENT);
					lAttachment.add(attachment);
					break;
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE:
				case PSTAttachment.ATTACHMENT_METHOD_BY_REFERENCE_ONLY:
					// TODO treat reference cases
					logWarning("mailextract.libpst: Can't extract reference attachment " + subject);
					break;
				case PSTAttachment.ATTACHMENT_METHOD_EMBEDDED:
					attachment = new Attachment(pstA.getLongFilename(), "Embedded Message - to be done".getBytes(),
							pstA.getCreationTime(), pstA.getModificationTime(), pstA.getMimeTag(), pstA.getContentId(),
							STORE_ATTACHMENT + MSG_STORE_ATTACHMENT);
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

	// get return-Path from SMTP or generate a fake one
	private String getReturnPath() {
		// pst file value
		String returnPath = message.getReturnPath();

		// if no result let's try with header values
		if (returnPath == null || returnPath.isEmpty()) {
			List<String> headerRP = getAddressHeader("Return-Path");
			if (!headerRP.isEmpty()) {
				if (headerRP.size() > 1)
					logWarning("mailextract.javamail: Multiple Return-Path addresses in header of message " + subject
							+ ", keep the first one");
				returnPath = headerRP.get(0);
			}
		}

		// may be a local mail, then generate a fake return-path
		if ((!message.isUnsent()) && returnPath.isEmpty())
			returnPath = "pst@localhost";

		return returnPath;

	}

	public void doAnalyzeMessage() throws ExtractionException {
		// header metadata extraction
		// * special global
		subject = message.getSubject();
		if (subject.equals("Vitam - livraison de documentation (version provisoire) de la part de Édouard VASSEUR"))
			System.out.println("Message trouvé");

		// header content extraction
		headers = getHeaders();
		mailHeader = getDecodedHeader();

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
