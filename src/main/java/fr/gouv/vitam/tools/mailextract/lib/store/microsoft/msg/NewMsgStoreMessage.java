package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.msg;

import java.util.Date;
import java.util.List;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertiesChunk;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

public class NewMsgStoreMessage extends MicrosoftStoreMessage {
	
	//
	MAPIMessage message;

	public NewMsgStoreMessage(StoreFolder mBFolder, MAPIMessage message) throws ExtractionException {
		super(mBFolder);
		this.message=message;
	}

	static final int MESSAGE_SIZE = 0x0e08;
	
	private long getPropertyLong(MAPIProperty prop){
		long result=0;
		List<Chunk> lChunk=message.getMainChunks().getAll().get(prop);
		
		if (lChunk!=null) {
			PropertiesChunk pChunk=lChunk.get(0);
			result=pChunk.
		}
			result=(long) ((PropertiesChunk)lChunk.get(0)).
		return result;
	}
	private String getPropertyString(){}
	

	@Override
	protected long getNativeMessageSize() {
		message.getMainChunks().getAll().get(MAPIProperty.MESSAGE_SIZE)
		return 0;
	}

	@Override
	protected String getNativeStringItem(int item) {

		return null;
	}

	@Override
	protected byte[] getNativeBinaryItem(int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Date getNativeDateItem(int item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean hasNativeConversationIndex() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Date getNativeCIDeliveryTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeCIGuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNativeCINumberOfResponseLevels() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected short getNativeCIResponseLevelRandom(int responseLevelNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getNativeNumberOfRecipients() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getNativeRecipientsSmtpAddress(int recipientNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeRecipientsEmailAddress(int recipientNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeRecipientsDisplayName(int recipientNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNativeRecipientsType(int recipientNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getNativeBodyText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeBodyHTML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeRTFBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getNativeNumberOfAttachments() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getNativeAttachmentAttachMethod(int AttachmentNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected byte[] getNativeAttachmentByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeAttachmentFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Date getNativeAttachmentCreationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Date getNativeAttachmentModificationTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeAttachmentMimeTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeAttachmentContentId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MicrosoftStoreMessage getNativeAttachmentEmbeddedMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNativeAttachmentProtocole() {
		// TODO Auto-generated method stub
		return null;
	}

}
