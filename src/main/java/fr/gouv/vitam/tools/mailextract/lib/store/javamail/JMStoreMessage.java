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

package fr.gouv.vitam.tools.mailextract.lib.store.javamail;

import java.io.*;
import java.util.*;
import java.text.ParseException;

import javax.mail.*;
import javax.mail.internet.*;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.utils.RFC822Headers;

/**
 * StoreMessage sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox structure through MailExtract application,
 * could also be used for POP3 and Gmail, via StoreExtractor (not tested).
 */
public class JMStoreMessage extends StoreMessage {

	/** Native JavaMail message. */
	protected MimeMessage message;

	// format to parse dates in Receive header
	static private MailDateFormat mailDateFormat = new MailDateFormat();

	/**
	 * Instantiates a new JM mail box message.
	 *
	 * @param mBFolder
	 *            Containing MailBoxFolder
	 * @param message
	 *            Native JavaMail message
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public JMStoreMessage(StoreFolder mBFolder, MimeMessage message) throws ExtractionException {
		super(mBFolder);
		this.message = message;
//		this.nature = StoreMessage.MESSAGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#getMessageSize()
	 */
	@Override
	public long getMessageSize() {
		// geMessageSize of JavaMail is quite approximative...
		long result;

		if (mimeContent != null)
			result = mimeContent.length;
		else {
			mimeContent = getNativeMimeContent();
			result = mimeContent.length;
			logMessageWarning("mailextract.javamail: Can't get the size");
			result = 0;
		}
		return result;
	}

	// utilities

	// simple InternetAddress to metadata String
	static private String getElementalStringAddress(InternetAddress address) {
		String result = "";
		String s;

		if (address != null) {
			s = address.getPersonal();
			if (s != null)
				result = s + " ";
			s = address.getAddress();
			if (s != null)
				result += "<" + s + ">";
		}
		return result;
	}

	// any (simple or group) InternetAddress to metadata String
	static private String getStringAddress(InternetAddress address) {
		String result = "";

		if (address != null) {
			result = getElementalStringAddress(address);
			// special case of group address (RFC 2822)
			if (address.isGroup()) {
				try {
					InternetAddress[] group = address.getGroup(false);
					result += ":";
					for (int k = 0; k < group.length; k++) {
						if (k > 0)
							result += ",";
						result += getElementalStringAddress(group[k]);
					}
				} catch (AddressException e) {
					// too bad
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#prepareHeaders()
	 */
	protected void prepareAnalyze() {
		List<String> result = null;
		String line, value;
		Header header;

		try {
			Enumeration<Header> headers = message.getAllHeaders();
			if ((headers != null) && headers.hasMoreElements()) {
				result = new ArrayList<String>();
				while (headers.hasMoreElements()) {
					header = headers.nextElement();
					line = header.getName() + ": ";
					try {
						value = MimeUtility.decodeText(header.getValue());
					} catch (UnsupportedEncodingException e) {
						value = header.getValue(); // use raw value
					}
					// value = innerTrim(value);
					line += value;
					result.add(line);
				}
			}
		} catch (MessagingException e) {
			logMessageWarning("mailextract.javamail: Can't extract complete mail header");
		}
		mailHeader = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeSubject()
	 */
	protected void analyzeSubject() {
		String result = null;

		try {
			result = message.getSubject();
		} catch (MessagingException e) {
			logMessageWarning("mailextract.javamail: Can't get message subject");
		}
		subject = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeMessageID()
	 */
	protected void analyzeMessageID() {
		String result = null;

		try {
			result = message.getMessageID();
		} catch (MessagingException e) {
			logMessageWarning("mailextract.javamail: Can't extract message ID");
		}
		messageID = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeFrom()
	 */
	protected void analyzeFrom() {
		String result = null;
		List<String> aList = getAddressHeader("From");

		if ((aList == null) || (aList.size() == 0)) {
			logMessageWarning("mailextract.javamail: No From address in header");
		} else {
			if (aList.size() > 1)
				logMessageWarning("mailextract.javamail: Multiple From addresses, keep the first one in header");
			result = aList.get(0);
		}
		from = result;
	}

	// get addresses in header with parsing control relaxed
	private List<String> getAddressHeader(String name) {
		List<String> result = null;
		String addressHeaderString = null;

		try {
			addressHeaderString = message.getHeader(name, ", ");
		} catch (MessagingException me) {
			logMessageWarning("mailextract.javamail: Can't access to [" + name + "] address header");
		}

		if (addressHeaderString != null) {
			result = new ArrayList<String>();
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
				logMessageWarning("mailextract.javamail: Wrongly formatted address " + addressHeaderString
						+ ", keep raw address list in metadata in header " + name);
				result.add(addressHeaderString);
				return result;
			}
			if (iAddressArray != null) {
				for (InternetAddress ia : iAddressArray) {
					result.add(getStringAddress(ia));
				}
			} else
				result = null;
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeRecipients()
	 */
	protected void analyzeRecipients() {
		recipientTo = getAddressHeader("To");
		recipientCc = getAddressHeader("Cc");
		recipientBcc = getAddressHeader("Bcc");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReplyTo()
	 */
	protected void analyzeReplyTo() {
		replyTo = getAddressHeader("Reply-To");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReturnPath()
	 */
	protected void analyzeReturnPath() {
		String result = null;
		List<String> aList = getAddressHeader("Return-Path");

		if ((aList == null) || (aList.size() == 0)) {
			// logMessageWarning("mailextract.javamail: No Return-Path address
			// in header");
		} else {
			if (aList.size() > 1)
				logMessageWarning("mailextract.javamail: Multiple Return-Path, keep the first one addresses in header");
			result = aList.get(0);
		}

		returnPath = result;
	}

	// Received date, either in a specific header field either determined from
	// information about smtp relaying (Recieved header field in smtp).
	private Date getReceivedDate() throws MessagingException {
		Date result;

		result = message.getReceivedDate();
		if (result == null) {
			String receivedHeader = message.getHeader("Received", ",");
			if (receivedHeader != null) {
				int i = receivedHeader.indexOf(';');
				if (i != -1) // supposed to always be
				{
					receivedHeader = receivedHeader.substring(i + 1);
					try {
						result = mailDateFormat.parse(receivedHeader);
					} catch (ParseException e) {
						// too bad no date
					}
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeDates()
	 */
	protected void analyzeDates() {
		try {
			sentDate = message.getSentDate();
			receivedDate = getReceivedDate();
		} catch (MessagingException e) {
			logMessageWarning("mailextract.javamail: Can't extract dates");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeInReplyToId(
	 * )
	 */
	protected void analyzeInReplyToId() {
		String result = null;
		try {
			String[] irtList = message.getHeader("In-Reply-To");

			if (irtList != null) {
				if (irtList.length > 1)
					logMessageWarning(
							"mailextract.javamail: Multiple In-Reply-To identifiers, keep the first one in header");
				result = RFC822Headers.getHeaderValue(irtList[0]);
			}
		} catch (MessagingException me) {
			logMessageWarning("mailextract.javamail: Can't access to In-Reply-To header");
		}

		inReplyToUID = result;
	}

	/**
	 * Gets the header value.
	 *
	 * @param line
	 *            the line
	 * @return the header value
	 */
	// utility function to get the value part of an header string
	public static String getHeaderValue(String line) {
		int i = line.indexOf(':');
		if (i < 0)
			return line;
		// skip whitespace after ':'
		int j;
		for (j = i + 1; j < line.length(); j++) {
			char c = line.charAt(j);
			if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
				break;
		}
		return line.substring(j);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeReferences()
	 */
	protected void analyzeReferences() {
		List<String> result = null;
		try {
			String refHeader = message.getHeader("References", " ");

			if (refHeader != null) {
				result = new ArrayList<String>();
				String[] refList = getHeaderValue(refHeader).split(" ");
				for (String tmp : refList)
					try {
						result.add(MimeUtility.decodeText(tmp));
					} catch (UnsupportedEncodingException uee) {
						// too bad
					}
			}
		} catch (MessagingException me) {
			logMessageWarning("mailextract.javamail: Can't access to In-Reply-To header");
		}

		references = result;
	}

	/*
	 * Content analysis methods to be implemented for each StoreMessage
	 * implementation
	 */

	// append to bodyContent after creating it if needed
	private void appendBodyContent(int type, String s) {
		if (s != null) {
			if (bodyContent[type] == null)
				bodyContent[type] = s;
			else {
				bodyContent[type] += "/n" + s;
			}
		}
	}

	private static String getInputStreamContent(InputStream is) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		try {
			while ((length = is.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		} catch (IOException e) {
			return null;
		}
	}

	// recursively search in MimeParts of the message de body contents in
	// different versions
	private void getPartBodyContents(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			if (p.isMimeType("text/plain")
					&& ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
				if (p.getContent() instanceof InputStream)
					appendBodyContent(TEXT_BODY, getInputStreamContent((InputStream) p.getContent()));
				else if (p.getContent() instanceof String)
					appendBodyContent(TEXT_BODY, (String) p.getContent());
			} else if (p.isMimeType("text/html")
					&& ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
				if (p.getContent() instanceof InputStream)
					appendBodyContent(HTML_BODY, getInputStreamContent((InputStream) p.getContent()));
				else if (p.getContent() instanceof String)
					appendBodyContent(HTML_BODY, (String) p.getContent());
			} else if (p.isMimeType("text/rtf")
					&& ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
				if (p.getContent() instanceof InputStream)
					appendBodyContent(RTF_BODY, getInputStreamContent((InputStream) p.getContent()));
				else if (p.getContent() instanceof String)
					appendBodyContent(RTF_BODY, (String) p.getContent());
			}
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				getPartBodyContents(mp.getBodyPart(i));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeBodies()
	 */
	protected void analyzeBodies() {
		try {
			getPartBodyContents(message);
		} catch (Exception e) {
			logMessageWarning("mailextract.javamail: Badly formatted mime message, can't extract body contents");
		}
	}

	// recursively search in MimeParts all attachments
	private void getAttachments(List<StoreMessageAttachment> lStoreMessageAttachment, BodyPart p)
			throws MessagingException, IOException {

		if ((p.isMimeType("text/plain") || p.isMimeType("text/html") || p.isMimeType("text/rtf"))
				&& ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition())))
			// test if it's a bodyContent then not an attachment
			return;
		else if (!p.isMimeType("multipart/*")) {
			// any other non multipart is an attachment

			try {
				addAttachment(lStoreMessageAttachment, p);
			} catch (IOException | MessagingException | ParseException e) {
				logMessageWarning("mailextract.javamail: Can't extract a badly formatted attachement");
			}

		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				getAttachments(lStoreMessageAttachment, mp.getBodyPart(i));
			}
		}
	}

	// rawcontent of a part
	private byte[] getPartRawContent(BodyPart bp) throws IOException, MessagingException {
		InputStream is = bp.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int bytesRead;
		while ((bytesRead = is.read(buf)) != -1) {
			baos.write(buf, 0, bytesRead);
		}
		return baos.toByteArray();
	}

	// add one attachment
	private void addAttachment(List<StoreMessageAttachment> lStoreMessageAttachment, BodyPart bodyPart)
			throws IOException, MessagingException, ParseException {
		String[] headers;
		ContentDisposition disposition = null;
		ContentType contenttype = null;
		String date;

		// all attachment definition vars
		String aName = null;
		Date aCreationDate = null;
		Date aModificationDate = null;
		String aMimeType = null;
		String aContentID = null;
		int aType;

		// by default is an attachment
		aType = StoreMessageAttachment.FILE_ATTACHMENT;

		// get all we can from disposition
		headers = bodyPart.getHeader("Content-Disposition");
		if ((headers != null) && (headers.length > 0)) {
			disposition = new ContentDisposition(headers[0]);
			if (Part.INLINE.equalsIgnoreCase(disposition.getDisposition()))
				aType = StoreMessageAttachment.INLINE_ATTACHMENT;
			date = disposition.getParameter("creation-date");
			if ((date != null) && (!date.isEmpty()))
				aCreationDate = mailDateFormat.parse(date);
			date = disposition.getParameter("modification-date");
			if ((date != null) && (!date.isEmpty()))
				aModificationDate = mailDateFormat.parse(date);
			aName = disposition.getParameter("filename");
		}

		// get all we can from content-type if any
		headers = bodyPart.getHeader("Content-Type");
		if ((headers != null) && (headers.length > 0)) {
			// some kind of mimeType normalization
			try {
				contenttype = new ContentType(headers[0]);
				if (contenttype.getSubType().equalsIgnoreCase("RFC822"))
					aType = StoreMessageAttachment.STORE_ATTACHMENT;
				aMimeType = contenttype.getBaseType();
				if (aName == null)
					aName = contenttype.getParameter("name");
			} catch (Exception e) {
				aMimeType = headers[0];
				if (aMimeType.indexOf(';') != -1)
					aMimeType = aMimeType.substring(0, aMimeType.indexOf(';'));
				int j = aMimeType.lastIndexOf('/');
				if ((j != -1) && (j < aMimeType.length()))
					aMimeType = "application/" + aMimeType.substring(j + 1);
				else
					aMimeType = "application/octet-stream";
			}
		} else {
			// if no mimetype force to general case
			aMimeType = "application/octet-stream";
		}

		// get contentId for inline attachment
		headers = bodyPart.getHeader("Content-ID");
		if ((headers != null) && (headers.length != 0))
			aContentID = headers[0];

		// define a filename if not defined in headers
		if (aName == null)
			aName = "noname";

		if (aType == StoreMessageAttachment.STORE_ATTACHMENT)
			lStoreMessageAttachment.add(new StoreMessageAttachment(getPartRawContent(bodyPart), "eml",
					MimeUtility.decodeText(aName), aCreationDate, aModificationDate, aMimeType, aContentID, aType));
		else
			lStoreMessageAttachment.add(new StoreMessageAttachment(getPartRawContent(bodyPart), "file",
					MimeUtility.decodeText(aName), aCreationDate, aModificationDate, aMimeType, aContentID, aType));
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

		try {
			Object contentObject = message.getContent();

			if (contentObject instanceof Multipart) {

				Multipart multipart = (Multipart) contentObject;
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					getAttachments(result, bodyPart);

					// filename = bodyPart.getFileName();
					// // skip not attachment part
					// if
					// (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
					// && (filename == null)) {
					// continue; // dealing with attachments only
					// }
					//
					// addAttachment(result, bodyPart);
				}
			}
		} catch (Exception e) {
			logMessageWarning("mailextract.javamail: Badly formatted mime message, can't extract all attachments");
		}

		if (result.size() == 0)
			result = null;

		attachments = result;
	}

	@Override
	protected void analyzeAppointmentInformation() {
		// no normalized appointment information in messages
		return;
	}

	/*
	 * Global message
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#
	 * getNativeMimeContent()
	 */
	protected byte[] getNativeMimeContent() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			message.writeTo(baos);
		} catch (Exception e) {
			logMessageWarning("mailextract.javamail: Can't extract raw mime content");
		}

		return baos.toByteArray();
	}
}
