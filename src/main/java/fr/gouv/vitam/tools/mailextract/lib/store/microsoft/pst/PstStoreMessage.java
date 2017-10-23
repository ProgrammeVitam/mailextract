package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.pff.PSTConversationIndex.ResponseLevel;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage;

public class PstStoreMessage extends MicrosoftStoreMessage {

	PSTMessage message;

	public PstStoreMessage(StoreFolder mBFolder, PSTMessage message) {
		super(mBFolder);
		this.message = message;
	}

	@Override
	protected long getNativeMessageSize() {
		return message.getMessageSize();
	}

	@Override
	protected String getNativeSmtpTransportHeader() {
		return message.getTransportMessageHeaders();
	}

	@Override
	protected String getNativeSubject() {
		return message.getSubject();
	}

	@Override
	protected String getNativeInternetMessageId() {
		return message.getInternetMessageId();
	}

	@Override
	protected String getNativeSenderName() {
		return message.getSenderName();
	}

	@Override
	protected String getNativeSentRepresentingName() {
		return message.getSentRepresentingName();
	}

	@Override
	protected String getNativeSenderAddrType() {
		return message.getSenderAddrtype();
	}

	@Override
	protected String getNativeSenderEmailAddress() {
		return message.getSenderEmailAddress();
	}

	@Override
	protected String getNativeSentRepresentingAddrType() {
		return message.getSentRepresentingAddressType();
	}

	@Override
	protected String getNativeSentRepresentingEmailAddress() {
		return message.getSentRepresentingEmailAddress();
	}

	@Override
	protected String getNativeReturnPath() {
		return message.getReturnPath();
	}

	@Override
	protected Date getNativeMessageDeliveryTime() {
		return message.getMessageDeliveryTime();
	}

	@Override
	protected Date getNativeClientSubmitTime() {
		return message.getClientSubmitTime();
	}

	@Override
	protected String getNativeInReplyToId() {
		return message.getInReplyToId();
	}

	@Override
	protected boolean hasNativeConversationIndex() {
		return ((message.getConversationIndex() != null) && (message.getConversationIndex().getGuid()!=null));
	}

	@Override
	protected Date getNativeCIDeliveryTime() {
		return message.getConversationIndex().getDeliveryTime();
	}

	@Override
	protected UUID getNativeCIGuid() {
		return message.getConversationIndex().getGuid();
	}

	@Override
	protected int getNativeCINumberOfResponseLevels() {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.size();
	}

	@Override
	protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getDeltaCode();
	}

	@Override
	protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getTimeDelta();
	}

	@Override
	protected short getNativeCIResponseLevelRandom(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getRandom();
	}

	@Override
	protected int getNativeNumberOfRecipients() {
		try {
			return message.getNumberOfRecipients();
		} catch (PSTException | IOException e) {
			return 0;
		}
	}

	@Override
	protected String getNativeRecipientsSmtpAddress(int recipientNumber) {
		PSTRecipient recipient;

		try {
			recipient = message.getRecipient(recipientNumber);
		} catch (PSTException | IOException e) {
			return null;
		}
		return recipient.getSmtpAddress();
	}

	@Override
	protected String getNativeRecipientsEmailAddress(int recipientNumber) {
		PSTRecipient recipient;

		try {
			recipient = message.getRecipient(recipientNumber);
		} catch (PSTException | IOException e) {
			return null;
		}
		return recipient.getEmailAddress();
	}

	@Override
	protected String getNativeRecipientsDisplayName(int recipientNumber) {
		PSTRecipient recipient;

		try {
			recipient = message.getRecipient(recipientNumber);
		} catch (PSTException | IOException e) {
			return null;
		}
		return recipient.getDisplayName();
	}

	@Override
	protected int getNativeRecipientsType(int recipientNumber) {
		PSTRecipient recipient;

		try {
			recipient = message.getRecipient(recipientNumber);
		} catch (PSTException | IOException e) {
			return 0;
		}
		return recipient.getRecipientType();
	}

	@Override
	protected String getNativeBodyText() {
		return message.getBody();
	}

	@Override
	protected String getNativeBodyHTML() {
		return message.getBodyHTML();
	}

	@Override
	protected String getNativeRTFBody() {
		try {
			return message.getRTFBody();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected int getNativeNumberOfAttachments() {
		return message.getNumberOfAttachments();
	}

	@Override
	protected int getNativeAttachmentAttachMethod(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getAttachMethod();
		} catch (PSTException | IOException e) {
			return 0;
		}
	}

	@Override
	protected byte[] getNativeAttachmentByteArray(int attachmentNumber) {
		try {
			InputStream is = message.getAttachment(attachmentNumber).getFileInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1) {
				baos.write(buf, 0, bytesRead);
			}
			return baos.toByteArray();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getNativeAttachmentLongFilename(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getLongFilename();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getNativeAttachmentFilename(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getFilename();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getNativeAttachmentDisplayName(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getDisplayName();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected Date getNativeAttachmentCreationTime(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getCreationTime();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected Date getNativeAttachmentModificationTime(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getModificationTime();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getNativeAttachmentMimeTag(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getMimeTag();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getNativeAttachmentContentId(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getContentId();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected Object getNativeAttachmentEmbeddedMessage(int attachmentNumber) {
		try {
			return message.getAttachment(attachmentNumber).getEmbeddedPSTMessage();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	@Override
	protected String getEmbeddedMessageScheme() {
		return "pst.embeddedmsg";
	}

}
