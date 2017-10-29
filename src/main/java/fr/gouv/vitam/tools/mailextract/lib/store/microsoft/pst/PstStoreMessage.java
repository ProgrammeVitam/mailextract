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

package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.pff.PSTConversationIndex.ResponseLevel;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessageAttachment;

public class PstStoreMessage extends MicrosoftStoreMessage {

	PSTMessage message;

	static final String EMBEDDED_MESSAGE = "pst.embeddedmsg";

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
		return ((message.getConversationIndex() != null) && (message.getConversationIndex().getGuid() != null));
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
	protected String getEmbeddedMessageScheme() {
		return EMBEDDED_MESSAGE;
	}

	@Override
	protected MicrosoftStoreMessageAttachment[] getNativeAttachments() {
		PstStoreMessageAttachment[] psmAttachment;
		
		psmAttachment = new PstStoreMessageAttachment[message.getNumberOfAttachments()];
		for (int i = 0; i < message.getNumberOfAttachments(); i++) {
			psmAttachment[i] = new PstStoreMessageAttachment(message, i);
		}
		return psmAttachment;
	}

}
