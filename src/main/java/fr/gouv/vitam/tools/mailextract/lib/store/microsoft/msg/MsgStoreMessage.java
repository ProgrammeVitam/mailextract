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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessageAttachment;

public class MsgStoreMessage extends MicrosoftStoreMessage {

	MAPIMessage message;

	long size;

	MsgConversationIndex msgConversationIndex;

	static final String EMBEDDED_MESSAGE = "msg.embeddedmsg";

	public static final int CONVERSATION_INDEX = 0x0071;
	public static final int SMTP_TRANSPORT_HEADER = 0x007d;
	public static final int SUBJECT = 0x0037;
	public static final int INTERNET_MESSAGE_ID = 0x1035;
	public static final int SENDER_NAME = 0x0c1a;
	public static final int SENT_REPRESENTING_NAME = 0x0042;
	public static final int SENDER_ADDR_TYPE = 0x0c1e;
	public static final int SENDER_EMAIL_ADDRESS = 0x0c1f;
	public static final int SENT_REPRESENTING_ADDR_TYPE = 0x0064;
	public static final int SENT_REPRESENTING_EMAIL_ADDRESS = 0x0065;
	public static final int RETURN_PATH = 0x1046;
	public static final int MESSAGE_DELIVERY_TIME = 0x0e06;
	public static final int CLIENT_SUBMIT_TIME = 0x0039;
	public static final int IN_REPLY_TO_ID = 0x1042;
	public static final int MESSAGE_SIZE = 0x0e08;

	public MsgStoreMessage(StoreFolder mBFolder, MAPIMessage message, long size) {
		super(mBFolder);
		this.message = message;
		this.size = size;
		getConversationIndex();
	}

	private void getConversationIndex() {
		byte[] byteConversationIndex = getByteItem(CONVERSATION_INDEX);
		if (byteConversationIndex != null) {
			msgConversationIndex = new MsgConversationIndex(byteConversationIndex);
			if (msgConversationIndex.getGuid() == null)
				msgConversationIndex = null;
		}
	}

	@Override
	protected long getNativeMessageSize() {
		return size;
	}

	private String getStringItem(int item) {
		String result = "";
		MAPIProperty prop = MAPIProperty.get(item);
		List<Chunk> lChunk = message.getMainChunks().getAll().get(prop);

		if (lChunk != null) {
			if (lChunk.size() > 1) {
				logMessageWarning("mailextract.msg: Multiple MSG object [" + prop.name + "], keep the first one");
			}
			if (lChunk.get(0) instanceof StringChunk) {
				StringChunk sChunk = (StringChunk) lChunk.get(0);
				result = sChunk.getValue();
			} else
				logMessageWarning("mailextract.msg: Unexpected MSG object type [" + prop.name + "]");
		}
		return result;
	}

	private byte[] getByteItem(int item) {
		byte[] result = null;
		MAPIProperty prop = MAPIProperty.get(item);
		List<Chunk> lChunk = message.getMainChunks().getAll().get(prop);

		if (lChunk != null) {
			if (lChunk.size() > 1) {
				logMessageWarning("mailextract.msg: Multiple MSG object [" + prop.name + "], keep the first one");
			}
			if (lChunk.get(0) instanceof ByteChunk) {
				ByteChunk bChunk = (ByteChunk) lChunk.get(0);
				result = bChunk.getValue();
			} else
				logMessageWarning("mailextract.msg: Unexpected MSG object type [" + prop.name + "]");
		}
		return result;
	}

	private Date getDateItem(int item) {
		Date result = null;
		MAPIProperty prop = MAPIProperty.get(item);
		List<PropertyValue> lVal = message.getMainChunks().getMessageProperties().getProperties().get(prop);

		if (lVal != null) {
			Calendar cal = (Calendar) lVal.get(0).getValue();
			if (cal != null)
				result = cal.getTime();
		}
		return result;
	}

	@Override
	protected String getNativeSmtpTransportHeader() {
		return getStringItem(SMTP_TRANSPORT_HEADER);
	}

	@Override
	protected String getNativeSubject() {
		return getStringItem(SUBJECT);
	}

	@Override
	protected String getNativeInternetMessageId() {
		return getStringItem(INTERNET_MESSAGE_ID);
	}

	@Override
	protected String getNativeSenderName() {
		return getStringItem(SENDER_NAME);
	}

	@Override
	protected String getNativeSentRepresentingName() {
		return getStringItem(SENT_REPRESENTING_NAME);
	}

	@Override
	protected String getNativeSenderAddrType() {
		return getStringItem(SENDER_ADDR_TYPE);
	}

	@Override
	protected String getNativeSenderEmailAddress() {
		return getStringItem(SENDER_EMAIL_ADDRESS);
	}

	@Override
	protected String getNativeSentRepresentingAddrType() {
		return getStringItem(SENT_REPRESENTING_ADDR_TYPE);
	}

	@Override
	protected String getNativeSentRepresentingEmailAddress() {
		return getStringItem(SENT_REPRESENTING_EMAIL_ADDRESS);
	}

	@Override
	protected String getNativeReturnPath() {
		return getStringItem(RETURN_PATH);
	}

	@Override
	protected Date getNativeMessageDeliveryTime() {
		return getDateItem(MESSAGE_DELIVERY_TIME);
	}

	@Override
	protected Date getNativeClientSubmitTime() {
		return getDateItem(CLIENT_SUBMIT_TIME);
	}

	@Override
	protected String getNativeInReplyToId() {
		return getStringItem(IN_REPLY_TO_ID);
	}

	@Override
	protected boolean hasNativeConversationIndex() {
		return msgConversationIndex != null;
	}

	@Override
	protected Date getNativeCIDeliveryTime() {
		return msgConversationIndex.getDeliveryTime();
	}

	@Override
	protected UUID getNativeCIGuid() {
		return msgConversationIndex.getGuid();
	}

	@Override
	protected int getNativeCINumberOfResponseLevels() {
		return msgConversationIndex.getResponseLevels().size();
	}

	@Override
	protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber) {
		return msgConversationIndex.getResponseLevels().get(responseLevelNumber).deltaCode;
	}

	@Override
	protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber) {
		return msgConversationIndex.getResponseLevels().get(responseLevelNumber).timeDelta;
	}

	@Override
	protected short getNativeCIResponseLevelRandom(int responseLevelNumber) {
		return msgConversationIndex.getResponseLevels().get(responseLevelNumber).random;
	}

	@Override
	protected int getNativeNumberOfRecipients() {
		return message.getRecipientDetailsChunks().length;
	}

	@Override
	protected String getNativeRecipientsSmtpAddress(int recipientNumber) {
		return message.getRecipientDetailsChunks()[recipientNumber].recipientSMTPChunk.getValue();
	}

	@Override
	protected String getNativeRecipientsEmailAddress(int recipientNumber) {
		return message.getRecipientDetailsChunks()[recipientNumber].recipientEmailChunk.getValue();
	}

	@Override
	protected String getNativeRecipientsDisplayName(int recipientNumber) {
		return message.getRecipientDetailsChunks()[recipientNumber].recipientNameChunk.getValue();
	}

	@Override
	protected int getNativeRecipientsType(int recipientNumber) {
		return (int) message.getRecipientDetailsChunks()[recipientNumber].getProperties()
				.get(MAPIProperty.RECIPIENT_TYPE).get(0).getValue();
	}

	@Override
	protected String getNativeBodyText() {
		String result;
		try {
			result = message.getTextBody();
		} catch (ChunkNotFoundException e) {
			result = "";
		}
		return result;
	}

	@Override
	protected String getNativeBodyHTML() {
		String result;
		try {
			result = message.getHtmlBody();
		} catch (ChunkNotFoundException e) {
			result = "";
		}
		return result;
	}

	@Override
	protected String getNativeRTFBody() {
		String result;
		try {
			result = message.getRtfBody();
		} catch (ChunkNotFoundException e) {
			result = "";
		}
		return result;
	}

	@Override
	protected String getEmbeddedMessageScheme() {
		return EMBEDDED_MESSAGE;
	}

	@Override
	protected MicrosoftStoreMessageAttachment[] getNativeAttachments() {
		MsgStoreMessageAttachment[] msgAttachments;
		AttachmentChunks[] allAttachmentChunks = message.getAttachmentFiles();

		msgAttachments = new MsgStoreMessageAttachment[allAttachmentChunks.length];
		for (int i = 0; i < allAttachmentChunks.length; i++) {
			msgAttachments[i] = new MsgStoreMessageAttachment(allAttachmentChunks[i]);
		}
		return msgAttachments;
	}

}
