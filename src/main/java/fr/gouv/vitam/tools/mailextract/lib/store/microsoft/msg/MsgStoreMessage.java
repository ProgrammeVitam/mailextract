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
package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.msg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.PropertiesChunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.MessagePropertiesChunk;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.RecipientChunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import com.pff.PSTRecipient;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.utils.RFC822Headers;

// TODO: Auto-generated Javadoc
/**
 * The Class MsgStoreMessage.
 */
public class MsgStoreMessage extends StoreMessage {

	/** The mapi message. */
	MAPIMessage mapiMessage;

	/** The size. */
	long size;

	/** The RFC822 headers if any. */
	RFC822Headers rfc822Headers;

	// get a String for the MAPIProperty field
	private String getMAPIString(MAPIProperty prop) {
		String result = null;
		List<Chunk> lChunk = mapiMessage.getMainChunks().getAll().get(prop);

		if (lChunk != null) {
			if (lChunk.size() > 1) {
				logMessageWarning(
						"mailextract.msg: Multiple MSG object [" + prop.name + "], keep the first one in header");
			}
			if (lChunk.get(0) instanceof StringChunk)
				try {
					result = mapiMessage.getStringFromChunk((StringChunk) lChunk.get(0));
				} catch (ChunkNotFoundException e) {
					logMessageWarning("mailextract.msg: Can't analyze MSG object type [" + prop.name + "]");
					result = null;
				}
			else
				logMessageWarning("mailextract.msg: Unexpected MSG object type [" + prop.name + "]");
		}

		return result;
	}

	/**
	 * Gets the first property value.
	 *
	 * @param chunk
	 *            the chunk
	 * @param prop
	 *            the prop
	 * @return the first property value
	 */
	// get a value from Chunk[] for MAPIProperty
	PropertyValue getFirstPropertyValue(Chunk chunk, MAPIProperty prop) {
		PropertyValue result;

		if (chunk == null)
			result = null;
		else {
			List<PropertyValue> lPropertyValue = ((PropertiesChunk) chunk).getValues(prop);
			if ((lPropertyValue == null) || lPropertyValue.size() == 0)
				result = null;
			else
				result = lPropertyValue.get(0);
		}

		return result;
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

		Chunks chunks=mapiMessage.getMainChunks();
		MessagePropertiesChunk chunk = chunks.getMessageProperties();
		List<PropertyValue> lProp=chunk.getValues(MAPIProperty.MESSAGE_SIZE);
		
		if (!hasRFC822Headers()) {
			headerString = getMAPIString(MAPIProperty.TRANSPORT_MESSAGE_HEADERS);
			if ((headerString != null) && (!headerString.isEmpty()))
				try {
					rfc822Headers = new RFC822Headers(headerString, this);
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
					logMessageWarning("mailextract.msg: Multiple subjects, keep the first one in header");
				result = RFC822Headers.getHeaderValue(sList[0]);
			}
		} else {
			// pst file value
			result = getMAPIString(MAPIProperty.SUBJECT);
			if (result.isEmpty())
				result = null;
		}

		if (result == null)
			logMessageWarning("mailextract.msg: No subject in header");

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
			result = getMAPIString(MAPIProperty.INTERNET_MESSAGE_ID);
			// TODO use Conversation Index like in LPStoreMessage
		}

		if (result == null)
			logMessageWarning("mailextract.pst: No Message ID address in header");
		messageID = result;
	}

	// From specific functions

	// get sender name using all possible sources
	private String getSenderName() {
		String result;

		result = getMAPIString(MAPIProperty.SENDER_NAME);
		if ((result == null) || result.isEmpty())
			result = getMAPIString(MAPIProperty.SENT_REPRESENTING_NAME);
		if ((result == null) || result.isEmpty())
			result = null;
		return result;
	}

	// get sender email address using all possible sources (sender and
	// SentRepresenting field), and using SMTP first
	private String getSenderEmailAddress() {
		String result = null, type;

		type = getMAPIString(MAPIProperty.SENDER_ADDRTYPE);
		if ((type != null) && type.equalsIgnoreCase("SMTP")) {
			result = getMAPIString(MAPIProperty.SENDER_EMAIL_ADDRESS);
		}
		if (result == null) {
			type = getMAPIString(MAPIProperty.SENT_REPRESENTING_ADDRTYPE);
			if ((type != null) && type.equalsIgnoreCase("SMTP")) {
				result = getMAPIString(MAPIProperty.SENT_REPRESENTING_EMAIL_ADDRESS);
			}
		}
		if (result == null)
			result = getMAPIString(MAPIProperty.SENDER_EMAIL_ADDRESS);
		if (result == null)
			result = getMAPIString(MAPIProperty.SENT_REPRESENTING_EMAIL_ADDRESS);

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
					logMessageWarning("mailextract.msg: Multiple From addresses, keep the first one in header");
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
			logMessageWarning("mailextract.msg: No From address in header");

		from = result;
	}

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
			RecipientChunks rChunk;
			StringChunk tmpSC;
			String emailAddress, normAddress;
			recipientNumber = mapiMessage.getRecipientDetailsChunks().length;
			for (int i = 0; i < recipientNumber; i++) {
				try {
					rChunk = mapiMessage.getRecipientDetailsChunks()[i];
					emailAddress = null;

					// prefer smtp address
					tmpSC = rChunk.recipientSMTPChunk;
					if (tmpSC != null)
						emailAddress = mapiMessage.getStringFromChunk(tmpSC);
					else {
						tmpSC = rChunk.recipientEmailChunk;
						if (tmpSC != null)
							emailAddress = mapiMessage.getStringFromChunk(tmpSC);
					}
					normAddress = rChunk.getRecipientName();
					if (emailAddress != null)
						normAddress += " <" + emailAddress + ">";
					switch ((int) (rChunk.getProperties().get(MAPIProperty.RECIPIENT_TYPE).get(0).getValue())) {
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

	/**
	 * Analyze message to get Reply-To metadata.
	 */
	protected void analyzeReplyTo() {
	}

	/**
	 * Analyze message to get Return-Path metadata.
	 */
	protected void analyzeReturnPath() {
	}

	/**
	 * Analyze message to get sent and received dates metadata.
	 */
	protected void analyzeDates() {
	}

	/**
	 * Analyze message to get In-Reply-To metadata.
	 */
	protected void analyzeInReplyToId() {
	}

	/**
	 * Analyze message to get References metadata.
	 */
	protected void analyzeReferences() {
	}

	// Content analysis methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeBodies()
	 */
	protected void analyzeBodies() {
		String result = null;

		// text
		try {
			result = mapiMessage.getTextBody();
		} catch (ChunkNotFoundException cnfe) {
			result = null;
		}
		if ((result != null) && (result.isEmpty()))
			result = null;
		bodyContent[TEXT_BODY] = result;

		// html
		try {
			result = mapiMessage.getHtmlBody();
		} catch (ChunkNotFoundException cnfe) {
			result = null;
		}
		if ((result != null) && (result.isEmpty()))
			result = null;
		bodyContent[HTML_BODY] = result;

		// rtf
		try {
			result = mapiMessage.getRtfBody();
		} catch (ChunkNotFoundException cnfe) {
			result = null;
		}
		if ((result != null) && (result.isEmpty()))
			result = null;
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
		attachments = result;
	}

	// Global message

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#
	 * getNativeMimeContent()
	 */
	protected byte[] getNativeMimeContent() {
		return null;
	}

	/**
	 * Instantiates a new msg store message.
	 *
	 * @param storeFolder
	 *            the store folder
	 * @param mapiMessage
	 *            the mapi message
	 * @param size
	 *            the size
	 */
	public MsgStoreMessage(MsgStoreFolder storeFolder, MAPIMessage mapiMessage, long size) {
		super(storeFolder);
		this.mapiMessage = mapiMessage;
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#getMessageSize()
	 */
	@Override
	public long getMessageSize() {
		return this.size;
	}
}
