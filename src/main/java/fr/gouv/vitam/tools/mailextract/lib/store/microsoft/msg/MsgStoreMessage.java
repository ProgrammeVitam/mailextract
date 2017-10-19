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

public class MsgStoreMessage extends MicrosoftStoreMessage {

	MAPIMessage message;

	long size;

	MsgConversationIndex msgConversationIndex;
	MsgAttachment[] msgAttachments;

	public MsgStoreMessage(StoreFolder mBFolder, MAPIMessage message, long size) {
		super(mBFolder);
		this.message = message;
		this.size = size;
		getConversationIndex();
		getAttachments();
	}

	private void getConversationIndex() {
		byte[] byteConversationIndex = getNativeByteItem(MicrosoftStoreMessage.CONVERSATION_INDEX);
		if (byteConversationIndex != null) {
			msgConversationIndex = new MsgConversationIndex(byteConversationIndex);
			if (msgConversationIndex.getGuid() == null)
				msgConversationIndex = null;
		}
	}

	private void getAttachments() {
		AttachmentChunks[] allAttachmentChunks = message.getAttachmentFiles();

		msgAttachments = new MsgAttachment[allAttachmentChunks.length];
		for (int i = 0; i < allAttachmentChunks.length; i++) {
			msgAttachments[i] = new MsgAttachment(allAttachmentChunks[i]);
		}
	}

	@Override
	protected long getNativeMessageSize() {
		return size;
	}

	@Override
	protected String getNativeStringItem(int item) {
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

	@Override
	protected byte[] getNativeByteItem(int item) {
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

	@Override
	protected Date getNativeDateItem(int item) {
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
	protected int getNativeNumberOfAttachments() {
		return msgAttachments.length;
	}

	@Override
	protected int getNativeAttachmentAttachMethod(int attachmentNumber) {
		return (int) msgAttachments[attachmentNumber].attachMethod;
	}

	@Override
	protected byte[] getNativeAttachmentByteArray(int attachmentNumber) {
		return msgAttachments[attachmentNumber].byteArray;
	}

	@Override
	protected String getNativeAttachmentLongFilename(int attachmentNumber) {
		return msgAttachments[attachmentNumber].longFilename;
	}

	@Override
	protected String getNativeAttachmentFilename(int attachmentNumber) {
		return msgAttachments[attachmentNumber].filename;
	}

	@Override
	protected String getNativeAttachmentDisplayName(int attachmentNumber) {
		return msgAttachments[attachmentNumber].displayName;
	}

	@Override
	protected Date getNativeAttachmentCreationTime(int attachmentNumber) {
		return msgAttachments[attachmentNumber].creationTime;
	}

	@Override
	protected Date getNativeAttachmentModificationTime(int attachmentNumber) {
		return msgAttachments[attachmentNumber].modificationTime;
	}

	@Override
	protected String getNativeAttachmentMimeTag(int attachmentNumber) {
		return msgAttachments[attachmentNumber].mimeTag;
	}

	@Override
	protected String getNativeAttachmentContentId(int attachmentNumber) {
		return msgAttachments[attachmentNumber].contentId;
	}

	@Override
	protected Object getNativeAttachmentEmbeddedMessage(int attachmentNumber) {
		return msgAttachments[attachmentNumber].embeddedMessage;
	}

	@Override
	protected String getNativeProtocole() {
		return "msg";
	}

}
