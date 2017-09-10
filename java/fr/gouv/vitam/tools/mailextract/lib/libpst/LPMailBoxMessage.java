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

package fr.gouv.vitam.tools.mailextract.lib.libpst;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import com.pff.PSTAttachment;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.MailBoxFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.MailBoxMessage;

/**
 * MailBoxMessage sub-class for mail boxes extracted through libpst library.
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
public class LPMailBoxMessage extends MailBoxMessage {

	/** Native libpst message. */
	protected PSTMessage message;

	/** The headers. */
	LinkedHashMap<CaseUnsensString, String> headers;

	/**
	 * Instantiates a new LP mail box message.
	 *
	 * @param mBFolder
	 *            Containing MailBoxFolder
	 * @param message
	 *            Native libpst message
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public LPMailBoxMessage(MailBoxFolder mBFolder, PSTMessage message) throws ExtractionException {
		super(mBFolder);
		this.message = message;
	}

	// utility method to generate a List<String> containing only one String
	private static final List<String> listString(String value) {
		List<String> ls = new ArrayList<String>();
		ls.add(value);
		return ls;
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
			ByteArrayInputStream bais = new ByteArrayInputStream(message.toString().getBytes());
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
		LinkedHashSet<String> toSet = new LinkedHashSet<String>();
		LinkedHashSet<String> ccAndBccSet = new LinkedHashSet<String>();
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
				String emailAddress = pstR.getEmailAddress();
				normAddress = pstR.getDisplayName() + " <" + emailAddress + ">";
				if (pstR.getRecipientType() == PSTRecipient.MAPI_TO)
					toSet.add(normAddress);
				else
					ccAndBccSet.add(normAddress);

			} catch (Exception e) {
				logWarning("mailextract.libpst: Can't get recipient number " + Integer.toString(i) + " in message "
						+ subject);
			}
		}

		// completed with header values
		List<String> toList = getAddressHeader("To");
		if (toList != null && !toList.isEmpty())
			for (String s : toList)
				toSet.add(s);
		List<String> ccList = getAddressHeader("Cc");
		if (ccList != null && !ccList.isEmpty())
			for (String s : ccList)
				ccAndBccSet.add(s);
		List<String> bccList = getAddressHeader("Bcc");
		if (bccList != null && !bccList.isEmpty())
			for (String s : bccList)
				ccAndBccSet.add(s);

		recipientTo = new ArrayList<String>();
		recipientCcAndBcc = new ArrayList<String>();

		for (String s : toSet)
			recipientTo.add(s);
		for (String s : ccAndBccSet)
			recipientCcAndBcc.add(s);

	}

	// get from with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private List<String> getFrom() {
		List<String> from = new ArrayList<String>();

		// pst file value
		String pstFrom = message.getSentRepresentingEmailAddress();
		if (pstFrom != null && !pstFrom.isEmpty()) {
			pstFrom = message.getSentRepresentingName() + " <" + pstFrom + ">";
			from.add(pstFrom);
		}

		// completed with header values
		List<String> headerFrom = getAddressHeader("From");
		if (headerFrom != null && !headerFrom.isEmpty())
			for (String s : headerFrom) {
				if (!s.equals(pstFrom))
					from.add(s);
			}
		return from;
	}

	// Content analysis methods

	// get attachments with raw content and filename
	// TODO may be can get more metadata...
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
				pstA = message.getAttachment(i);
//				if (pstA.getAttachMethod()==5)
//					System.out.println("AttachMethod="+pstA.getAttachMethod());
				InputStream is = pstA.getFileInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buf)) != -1) {
					baos.write(buf, 0, bytesRead);
				}
				Attachment attachment = new Attachment(pstA.getLongFilename(), baos.toByteArray(),pstA.getCreationTime(),pstA.getModificationTime(),NO_ATTACHED_MESSAGE);
				lAttachment.add(attachment);
			} catch (Exception e) {
				logWarning("mailextract.libpst: Can't get attachment number " + Integer.toString(i) + " in message "
						+ subject);
			}
		}
		return lAttachment;

	}

	// encode a String in quoted-printable, default MIME encoding
	private static String encodeQP(String s) {
		String result;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			OutputStream encodedOut = MimeUtility.encode(baos, "quoted-printable");
			encodedOut.write(s.getBytes());
		} catch (Exception e) {
		}
		result = baos.toString();

		return result;
	}

	// get the inner representation of message in MIME format but without attachments
	// a small summary is added at the end to describe attachments
	private byte[] getRawContent() {
		String s, boundary;
		int i, j;

		// Get the Header and keep only the part before "\n\n"
		s = message.getTransportMessageHeaders();
		if (s.indexOf("\r\n\r\n") > 0)
			s = s.substring(0, s.indexOf("\r\n\r\n") + 1); // +1 added to have a
															// CRLF
		if (s.indexOf("\n\n") > 0) // Different kind of CRLF ;=)
			s = s.substring(0, s.indexOf("\n\n") + 1);

		// Remove multipart headers to avoid conflicts

		// Remove 'Content-Type'
		i = s.indexOf("Content-Type");
		if (i > 0) {
			j = s.indexOf("\n", i);
			if (j > 0)
				s = s.substring(0, i) + s.substring(j + 1);
			else
				s = s.substring(0, i);
		}

		// Remove 'Content-Transfer-Encoding'
		i = s.indexOf("Content-Transfer-Encoding");
		if (i > 0) {
			j = s.indexOf("\n", i);
			if (j > 0)
				s = s.substring(0, i) + s.substring(j + 1);
			else
				s = s.substring(0, i);
		}

		// Remove 'MIME-Version'
		i = s.indexOf("MIME-Version");
		if (i > 0) {
			i = s.lastIndexOf('\n', i) + 1;
			j = s.indexOf("\n", i);
			if (j > 0)
				s = s.substring(0, i) + s.substring(j + 1);
			else
				s = s.substring(0, i);
		}

		// Remove 'boundary'
		i = s.indexOf("boundary");
		if (i > 0) {
			i = s.lastIndexOf('\n', i) + 1;
			j = s.indexOf("\n", i);
			if (j > 0)
				s = s.substring(0, i) + s.substring(j + 1);
			else
				s = s.substring(0, i);
		}

		// Get attachment description strings for different formats
		String line, sAttach, sAttachHTML, sAttachRTF;
		sAttach = "";
		sAttachHTML = "";
		sAttachRTF = "";

		if (!this.attachments.isEmpty()) {
			line = "================================================";
			sAttach = "\r\n" + line + "\r\n";
			sAttachHTML = "<BR>" + line + "<BR>\r\n";
			line = "Le message extrait contient " + this.attachments.size() + " fichier(s) attaché(s) ";
			sAttach += line + "\r\n";
			sAttachHTML += line + "<BR>\r\n";
			for (Attachment a : attachments) {
				sAttach += "\r\n - " + a.getFilename()+""+a.getRawContent().length+" octets";
				sAttachHTML += "<br> - " + a.getFilename()+""+a.getRawContent().length+" octets";
			}
			line = "================================================";
			sAttach += "\r\n" + line + "\r\n";
			sAttachHTML += "<BR>" + line + "<BR>\r\n";
			sAttachRTF = "{\\rtf1\\ansi\\deff0\\r\\n" + sAttach + "}";
		}

		// Add all the body form plain, HTML and RTF
		String part;

		boundary = "Part01234546789876543210123456789mailextract";
		s += "MIME-Version: 1.0\r\nContent-Type: multipart/alternative;charset=\"UTF-8\";\r\n\tboundary=\"" + boundary
				+ "\"\r\n";

		// plain part
		part = message.getBody();
		if (part.length() > 0) {
			s += "\r\n--" + boundary + "\r\n";
			part += sAttach;
			part = encodeQP(part);
			s = s + "Content-Type: text/plain; charset=\"UTF-8\"; format-flowed\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n"
					+ part + "\r\n";
		}

		// RTF part
		try {
			part = message.getRTFBody();
			if (part.length() > 0) {
				s += "\r\n--" + boundary + "\r\n";
				part += sAttachRTF;
				part = encodeQP(part);
				s = s + "Content-Type: text/rtf; charset=\"UTF-8\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n"
						+ part + "\r\n";
			}
		} catch (Exception e) {
		}

		// HTML part
		part = message.getBodyHTML();
		if (part.length() > 0) {
			s += "\r\n--" + boundary + "\r\n";
			part += sAttachHTML;
			part = encodeQP(part);
			s = s + "Content-Type: text/html; charset=\"UTF-8\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n"
					+ part + "\r\n";
		}

		s += "\r\n--" + boundary + "--\r\n\r\n";

		return s.getBytes();
	}

	// main analyze method

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxMessage#analyzeMessage()
	 */
	public void analyzeMessage() throws ExtractionException {
		// List<String> cc, bcc;

		// header metadata extraction
		// * special global
		subject = message.getSubject();
		getLogger().finer("mailextract.javamail: Extract message " + subject);

		// header content extraction
		headers = getHeaders();
		mailHeader = getDecodedHeader();

		// * recipients and co
		from = getFrom();
		replyTo = getAddressHeader("Reply-To");
		getRecipients();
		returnPath = listString(message.getReturnPath());

		// * dates
		receivedDate = message.getMessageDeliveryTime();
		sentDate = message.getClientSubmitTime();

		textContent = message.getBody();
		attachments = getAttachments();

		// finally reconstruct an eml form after attachments extraction
		rawContent = getRawContent();

		messageUID = message.getInternetMessageId();
		inReplyToUID = message.getInReplyToId();
		references = getReferences();
		sender = listString(message.getSenderName() + " <" + message.getSenderEmailAddress() + ">");
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
