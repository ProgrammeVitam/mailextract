package fr.gouv.vitam.tools.mailextract.lib.store.msg;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.PropertyValue.LongPropertyValue;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;

public class MsgStoreMessage extends StoreMessage {

	MAPIMessage mapiMessage;
	long size;

	/** The headers. */
	LinkedHashMap<CaseUnsensString, String> headers;

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

	public MsgStoreMessage(MsgStoreFolder storeFolder, MAPIMessage mapiMessage, long size) {
		super(storeFolder);
		this.mapiMessage = mapiMessage;
		this.size = size;
	}

	@Override
	public long getMessageSize() {
		return this.size;
	}

	// FIXME duplicate
	// get all headers HashMap but with non case sensitive keys
	// don't have to differentiate to,To and TO header...
	private LinkedHashMap<CaseUnsensString, String> getHeaders() {
		LinkedHashMap<CaseUnsensString, String> headers = new LinkedHashMap<CaseUnsensString, String>();
		String line, nextline, value;
		CaseUnsensString key;
		int i, nexti;

		try {
			String[] headersLine = mapiMessage.getHeaders();

			for (i = 0; i < headersLine.length;) {
				line = headersLine[i];
				if (line.isEmpty()) {
					i = i + 1;
					continue;
				} else if (line.charAt(0) == '{')
					break;
				nexti = i + 1;
				if (nexti < headersLine.length) {
					nextline = headersLine[nexti];
					// take in header value all line after beginning by space or
					// \t
					while (nexti < headersLine.length && nextline != null && !nextline.isEmpty()
							&& (nextline.charAt(0) == ' ' || nextline.charAt(0) == '\t')) {
						line += " " + nextline.trim();
						nexti = nexti + 1;
						nextline = headersLine[nexti];
					}
				}
				int index = line.indexOf(':');
				if (index > 0) {
					// should always be if not line dropped
					key = new CaseUnsensString(line.substring(0, index).trim());
					value = line.substring(index + 1).trim();
					if (headers.containsKey(key))
						headers.put(key, headers.get(key) + "," + value);
					else
						headers.put(key, value);
				}
				i = nexti;
			}
		} catch (ChunkNotFoundException e) {
			logWarning("mailextract.libpst: Can't extract complete mail header from message " + subject);
		}
		return headers;
	}

	// FIXME duplicate
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

	// FIXME duplicate
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

	// FIXME duplicate
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

	// FIXME duplicate
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

	// FIXME duplicate
	// get from with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private String getFrom() {
		// TODO msg file value

		// String from = getSenderEmailAddress();
		// if (from != null && !from.isEmpty()) {
		// from = getSenderName() + " <" + from + ">";
		// }

		String from = null;
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

	// FIXME duplicate
	// get recipients with both values in Microsoft Format from pst file
	// and in smtp format from header if any
	private void getRecipients() {
		recipientTo = new ArrayList<String>();
		recipientCc = new ArrayList<String>();
		recipientBcc = new ArrayList<String>();

		// pst file values
		RecipientChunks[] recipientChunks;
		try {
			recipientChunks = mapiMessage.getRecipientDetailsChunks();
			for (RecipientChunks rc : recipientChunks) {
				// prefer smtp address
				String emailAddress = rc.getRecipientEmailAddress();
				String normAddress = rc.getRecipientName() + " <" + emailAddress + ">";
				//System.out.println("----Adress: " + normAddress);
				// get Recipient Type
				List<PropertyValue> pvlist = rc.getProperties().get(MAPIProperty.RECIPIENT_TYPE);
				int type;
				if ((pvlist != null) && !pvlist.isEmpty()) {
					LongPropertyValue pv = (LongPropertyValue) pvlist.get(0);
					type = (int) pv.getValue();
				} else
					type = 100;
				//System.out.println("--------type: " + type);
				switch (type) {
				case 1:// To
					recipientTo.add(normAddress);
					break;
				case 2:// CC
					recipientCc.add(normAddress);
					break;
				case 3:// CC
					recipientBcc.add(normAddress);
					break;
				}
			}
		} catch (Exception e) {
			logWarning("mailextract.libpst: Can't determine recipient list in message " + subject);
		}
	}

	@Override
	protected void doAnalyzeMessage() throws ExtractionException {
		// TODO Auto-generated method stub
		// header metadata extraction

		// Chunk[] tmp = mapiMessage.getMainChunks().getChunks();
		//
		// for (Chunk c : tmp) {
		// System.out.format("Entry is: %04x%n", c.getChunkId());
		// System.out.format("----Value is: %s%n", c);
		// }

		try {
			subject = mapiMessage.getSubject();

			headers = getHeaders();
			mailHeader = getDecodedHeader();

			// * recipients and co
			from = getFrom();
			// replyTo = getReplyTo();

			getRecipients();
			// returnPath = getReturnPath();
			// for (CaseUnsensString s : headers.keySet())
			// System.out.format("Header line:%n\ttag=%s%n\tvalue=%s%n", s,
			// headers.get(s));

		} catch (ChunkNotFoundException e) {
			subject = "";
		}

		try {
			bodyContent[TEXT_BODY] = mapiMessage.getTextBody();
		} catch (ChunkNotFoundException e) {
			bodyContent[TEXT_BODY] = null;
		}
		try {
			bodyContent[HTML_BODY] = mapiMessage.getHtmlBody();
		} catch (ChunkNotFoundException e) {
			bodyContent[HTML_BODY] = null;
		}
		try {
			bodyContent[RTF_BODY] = mapiMessage.getRtfBody();
		} catch (ChunkNotFoundException e) {
			bodyContent[RTF_BODY] = null;
		}

		attachments = new ArrayList<Attachment>();

		// // header content extraction
		// headers = getHeaders();
		// mailHeader = getDecodedHeader();
		//
		// // * recipients and co
		// from = getFrom();
		// replyTo = getReplyTo();
		//
		// getRecipients();
		// returnPath = getReturnPath();
		//
		// // * dates
		// receivedDate = message.getMessageDeliveryTime();
		// sentDate = message.getClientSubmitTime();
		//
		// bodyContent[TEXT_BODY] = message.getBody();
		// bodyContent[HTML_BODY] = message.getBodyHTML();
		// try {
		// bodyContent[RTF_BODY] = message.getRTFBody();
		// } catch (PSTException | IOException e) {
		// bodyContent[RTF_BODY] = null;
		// // forget it
		// }
		// attachments = getAttachments();
		//
		// // no raw content, will be constructed at StoreMessage level
		// mimeContent = null;
		//
		// messageUID = getMessageUId();
		// inReplyToUID = getInReplyToId();
		// references = getReferences();
		//
		//System.out.println("Message "+subject+ " analyzed");
	}

}
