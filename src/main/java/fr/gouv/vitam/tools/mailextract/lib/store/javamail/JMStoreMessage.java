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

import org.jsoup.*;
import org.jsoup.parser.Parser;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.formattools.HTMLTextExtractor;

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
			try {
				mimeContent = getMimeContent();
				result = mimeContent.length;
			} catch (ExtractionException e) {
				result = 0;
			}
			logWarning("mailextract.javamail: Can't get the size of message " + subject);
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
	static private String getStringAddress(InternetAddress address) throws AddressException {
		String result = "";

		if (address != null) {
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
		}
		return result;
	}

	// trim a header value to get rid of \r,\n,\t replaced by spaces
	static final private String innerTrim(String s) {
		s = s.replace("\n", " ");
		s = s.replace("\r", "");
		s = s.replace("\t", "");

		return s;
	}

	// Header analysis methods

	// get the subject
	private String getSubject() throws ExtractionException {
		String result;

		try {
			result = message.getSubject();
			if (result == null)
				result = "";
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't get message subject");
			result = "";
		}
		return result;
	}

	// get addresses in header with parsing control relaxed
	private List<String> getAddressHeader(String name) throws MessagingException {
		List<String> addressList = new ArrayList<String>();
		String addressHeaderString = message.getHeader(name, ", ");
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

	// get specific from address in header
	private String getFrom() throws MessagingException {
		List<String> aList = getAddressHeader("From");
		String from;

		if (aList.size() == 0) {
			logWarning("mailextract.javamail: No From address in header of message " + subject);
			from = "";
		} else {
			if (aList.size() > 1)
				logWarning("mailextract.javamail: Multiple From addresses in header of message " + subject
						+ ", keep the first one");
			from = aList.get(0);
		}
		return from;
	}

	// get specific from address in header
	private String getReturnPath() throws MessagingException {
		List<String> aList = getAddressHeader("Return-Path");
		String returnPath;

		if (aList.size() == 0) {
			logWarning("mailextract.javamail: No Return-Path address in header of message " + subject);
			returnPath = "";
		} else {
			if (aList.size() > 1)
				logWarning("mailextract.javamail: Multiple Return-Path addresses in header of message " + subject
						+ ", keep the first one");
			returnPath = aList.get(0);
		}
		return returnPath;
	}

	// get the messageID list of messages in same thread from References
	private List<String> getReferences() throws MessagingException {
		List<String> refList = new ArrayList<String>();
		String refString = message.getHeader("References", ", ");
		if (refString != null) {
			String[] l = refString.split(",");
			for (String s : l)
				refList.add(s);
		}
		return refList;
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

	// get all headers key: values decoded to UTF-8
	private List<String> getDecodedHeader() throws ExtractionException {
		List<String> result = new ArrayList<String>();
		String line, value;
		Header header;

		try {
			for (Enumeration<Header> headers = (message.getAllHeaders()); headers.hasMoreElements();) {
				header = headers.nextElement();
				line = header.getName() + ": ";
				try {
					value = MimeUtility.decodeText(header.getValue());
				} catch (UnsupportedEncodingException e) {
					value = header.getValue(); // use raw value
				}
				value = innerTrim(value);
				line += value;
				result.add(line);
			}
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract complete mail header from message " + subject);

		}
		return result;
	}

	// Content analysis methods

	// get the binary raw content as from smtp downloading
	private byte[] getMimeContent() throws ExtractionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			message.writeTo(baos);
		} catch (Exception e) {
			logWarning("mailextract.javamail: Can't extract raw mime content from message " + subject);
		}
		return baos.toByteArray();
	}

	// // methods to extract text content
	// private String getStringFromInputStream(InputStream inputStream) throws
	// IOException {
	// final int bufferSize = 1024;
	// final char[] buffer = new char[bufferSize];
	// final StringBuilder out = new StringBuilder();
	//
	// Reader in = new InputStreamReader(inputStream, "UTF-8");
	// while (true) {
	// int rsz = in.read(buffer, 0, buffer.length);
	// if (rsz < 0)
	// break;
	// out.append(buffer, 0, rsz);
	// }
	// return out.toString();
	// }

	// private String getPartTextContent(Part p) throws MessagingException,
	// IOException {
	// String result = null;
	//
	// if (p.isMimeType("text/*")) {
	// if (p.isMimeType("text/plain"))
	// result = (String) p.getContent();
	// else if (p.isMimeType("text/html"))
	// result =
	// HTMLTextExtractor.getInstance().getPlainText(Jsoup.parse((String)
	// p.getContent()));
	// else // other text dump
	// result = getStringFromInputStream((InputStream) p.getContent());
	// } else if (p.isMimeType("multipart/alternative")) {
	// Multipart mp = (Multipart) p.getContent();
	// for (int i = 0; i < mp.getCount(); i++) {
	// Part bp = mp.getBodyPart(i);
	// if (bp.isMimeType("text/plain")) {
	// String s = getPartTextContent(bp);
	// if (s != null) {
	// result = s;
	// break;
	// }
	// } else if (bp.isMimeType("text/html")) {
	// result =
	// HTMLTextExtractor.getInstance().getPlainText(Jsoup.parse((String)
	// p.getContent()));
	// break;
	// }
	// }
	// } else if (p.isMimeType("multipart/*")) {
	// Multipart mp = (Multipart) p.getContent();
	// for (int i = 0; i < mp.getCount(); i++) {
	// String s = getPartTextContent(mp.getBodyPart(i));
	// if (s != null) {
	// result = s;
	// break;
	// }
	// }
	// }
	//
	// // to force HTML unescape even for wrongly typed (not html) parts
	// result = Parser.unescapeEntities(result, true);
	//
	// return result;
	// }
	//
	private void getPartBodyContents(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			if (p.isMimeType("text/plain") && ((bodyContent[TEXT_BODY] == null) || bodyContent[TEXT_BODY].isEmpty()))
				bodyContent[TEXT_BODY] = (String) p.getContent();
			else if (p.isMimeType("text/html")
					&& ((bodyContent[HTML_BODY] == null) || bodyContent[HTML_BODY].isEmpty()))
				bodyContent[HTML_BODY] = (String) p.getContent();
			else if (p.isMimeType("text/rtf") && ((bodyContent[RTF_BODY] == null) || bodyContent[RTF_BODY].isEmpty()))
				bodyContent[RTF_BODY] = (String) p.getContent();
		} else if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				getPartBodyContents(bp);
			}
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				getPartBodyContents(mp.getBodyPart(i));
			}
		}
	}

	// private String getTextContent() {
	// try {
	// return getPartTextContent(message);
	// } catch (Exception e) {
	// logWarning("mailextract.javamail: Badly formatted mime message, can't
	// extract text content " + subject);
	// return null;
	// }
	// }
	private void getBodyContents() {
		try {
			getPartBodyContents(message);
			if (((bodyContent[TEXT_BODY] == null) || bodyContent[TEXT_BODY].isEmpty())
					&& (bodyContent[HTML_BODY] != null))
				bodyContent[TEXT_BODY] = HTMLTextExtractor.getInstance()
						.getPlainText(Jsoup.parse(bodyContent[HTML_BODY]));

			// to force HTML unescape even for wrongly typed (not html) parts
			if (bodyContent[TEXT_BODY] != null)
				bodyContent[TEXT_BODY] = Parser.unescapeEntities(bodyContent[TEXT_BODY], true);
		} catch (Exception e) {
			logWarning("mailextract.javamail: Badly formatted mime message, can't extract body contents " + subject);
		}
	}

	// get mime attachments with raw content and filename
	private List<Attachment> getAttachments() throws ExtractionException {
		List<Attachment> attachments = new ArrayList<Attachment>();
		String filename;
		int attachmentType;

		try {
			Object contentObject = message.getContent();

			if (contentObject instanceof Multipart) {
				Multipart multipart = (Multipart) contentObject;
				for (int i = 0; i < multipart.getCount(); i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					filename = bodyPart.getFileName();
					// skip not attachment part
					if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && (filename == null)) {
						continue; // dealing with attachments only
					}
					InputStream is = bodyPart.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buf)) != -1) {
						baos.write(buf, 0, bytesRead);
					}
					if (filename == null) {
						filename = "noname";
					}

					// search for file dates if any
					Date creationDate = null;
					Date modificationDate = null;
					String mimeType = null;
					String[] headers;
					ContentDisposition disposition;
					String contentID=null;
					String date;

					headers = bodyPart.getHeader("Content-Disposition");
					// if no disposition header consider as attached file
					if ((headers == null) || (headers.length == 0)) {
						attachmentType = FILE_ATTACHMENT;
					} else {
						disposition = new ContentDisposition(headers[0]);
						if (Part.INLINE.equalsIgnoreCase(disposition.getDisposition()))
							attachmentType = INLINE_ATTACHMENT;
						else
							attachmentType = FILE_ATTACHMENT;
						date = disposition.getParameter("creation-date");
						if ((date != null) && (!date.isEmpty()))
							creationDate = mailDateFormat.parse(date);
						date = disposition.getParameter("modification-date");
						if ((date != null) && (!date.isEmpty()))
							modificationDate = mailDateFormat.parse(date);
						mimeType = disposition.getParameter("content-type");
					}

					// detect if declared attached message
					headers = bodyPart.getHeader("Content-type");
					if (headers.length != 0) {
						if (headers[0].indexOf("rfc822") > 0)
							attachmentType = EML_STORE_ATTACHMENT + STORE_ATTACHMENT;
					}
					
					// get contentId for inline attachment
					headers = bodyPart.getHeader("Content-ID");
					if ((headers!=null) && (headers.length != 0)) {
						contentID=headers[0];
					}

					attachments.add(new Attachment(MimeUtility.decodeText(filename), baos.toByteArray(), creationDate,
							modificationDate, mimeType, contentID, attachmentType));
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			logWarning("mailextract.javamail: Badly formatted mime message, can't extract all attachments " + subject);
			return attachments;
		}
		return attachments;

	}

	// main analyze method

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#doAnalyzeMessage()
	 */
	public void doAnalyzeMessage() throws ExtractionException {
		// header metadata extraction
		// * special global
		subject = getSubject();

		mailHeader = getDecodedHeader();
		// * recipients and co
		try {
			from = getFrom();
			replyTo = getAddressHeader("Reply-To");
			recipientTo = getAddressHeader("To");
			recipientCc = getAddressHeader("Cc");
			recipientBcc = getAddressHeader("Bcc");
			returnPath = getReturnPath();
		} catch (AddressException e) {
			try {
				logWarning("mailextract.javamail: Can't extract addres " + MimeUtility.decodeText(e.getRef())
						+ " from message " + subject);
			} catch (UnsupportedEncodingException ee) {
			}
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract addresses from message " + subject);
		}

		// dates
		try {
			sentDate = message.getSentDate();
			receivedDate = getReceivedDate();
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract dates from message " + subject);
		}

		// message-id
		try {
			messageUID = message.getMessageID();
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract message uniqID from message " + subject);
		}
		
		// in-reply-to
		try {
			String[] l = message.getHeader("In-Reply-To");
			if (l != null && l.length > 0)
				inReplyToUID = l[0];
			else
				inReplyToUID = "";
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract in-reply-to uniqID from message " + subject);
		}
		
		// references
		try {
			references = getReferences();
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract message UniqID references from message " + subject);
		}
		
		// sender
		try {
			sender = getAddressHeader("Sender");
		} catch (MessagingException e) {
			logWarning("mailextract.javamail: Can't extract sender addresses from message " + subject);
		}

		// global content extraction
		mimeContent = getMimeContent();
		getBodyContents();
		attachments = getAttachments();

	}

}
