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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.pff.PSTAppointment;
import com.pff.PSTConversationIndex.ResponseLevel;
import com.pff.PSTException;
import com.pff.PSTMessage;
import com.pff.PSTRecipient;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAppointment;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessageAttachment;

/**
 * StoreMessage sub-class for mail boxes extracted through pst library.
 */
public class PstStoreMessage extends MicrosoftStoreMessage {

	/** The message. */
	PSTMessage message;

	/** The Constant EMBEDDED_MESSAGE. */
	static final String EMBEDDED_MESSAGE = "pst.embeddedmsg";

	/**
	 * Instantiates a new pst store message.
	 *
	 * @param mBFolder
	 *            the m B folder
	 * @param message
	 *            the message
	 */
	public PstStoreMessage(StoreFolder mBFolder, PSTMessage message) {
		super(mBFolder);
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeMessageSize()
	 */
	@Override
	protected long getNativeMessageSize() {
		return message.getMessageSize();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSmtpTransportHeader()
	 */
	@Override
	protected String getNativeSmtpTransportHeader() {
		return message.getTransportMessageHeaders();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSubject()
	 */
	@Override
	protected String getNativeSubject() {
		return message.getSubject();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeInternetMessageId()
	 */
	@Override
	protected String getNativeInternetMessageId() {
		return message.getInternetMessageId();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSenderName()
	 */
	@Override
	protected String getNativeSenderName() {
		return message.getSenderName();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingName()
	 */
	@Override
	protected String getNativeSentRepresentingName() {
		return message.getSentRepresentingName();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSenderAddrType()
	 */
	@Override
	protected String getNativeSenderAddrType() {
		return message.getSenderAddrtype();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSenderEmailAddress()
	 */
	@Override
	protected String getNativeSenderEmailAddress() {
		return message.getSenderEmailAddress();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingAddrType()
	 */
	@Override
	protected String getNativeSentRepresentingAddrType() {
		return message.getSentRepresentingAddressType();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingEmailAddress()
	 */
	@Override
	protected String getNativeSentRepresentingEmailAddress() {
		return message.getSentRepresentingEmailAddress();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeReturnPath()
	 */
	@Override
	protected String getNativeReturnPath() {
		return message.getReturnPath();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeMessageDeliveryTime()
	 */
	@Override
	protected Date getNativeMessageDeliveryTime() {
		return message.getMessageDeliveryTime();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeClientSubmitTime()
	 */
	@Override
	protected Date getNativeClientSubmitTime() {
		return message.getClientSubmitTime();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeInReplyToId()
	 */
	@Override
	protected String getNativeInReplyToId() {
		return message.getInReplyToId();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#hasNativeConversationIndex()
	 */
	@Override
	protected boolean hasNativeConversationIndex() {
		return ((message.getConversationIndex() != null) && (message.getConversationIndex().getGuid() != null));
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCIDeliveryTime()
	 */
	@Override
	protected Date getNativeCIDeliveryTime() {
		return message.getConversationIndex().getDeliveryTime();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCIGuid()
	 */
	@Override
	protected UUID getNativeCIGuid() {
		return message.getConversationIndex().getGuid();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCINumberOfResponseLevels()
	 */
	@Override
	protected int getNativeCINumberOfResponseLevels() {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.size();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelDeltaCode(int)
	 */
	@Override
	protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getDeltaCode();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelTimeDelta(int)
	 */
	@Override
	protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getTimeDelta();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelRandom(int)
	 */
	@Override
	protected short getNativeCIResponseLevelRandom(int responseLevelNumber) {
		List<ResponseLevel> lResponseLevel = message.getConversationIndex().getResponseLevels();

		if (lResponseLevel == null)
			return 0;
		else
			return lResponseLevel.get(responseLevelNumber).getRandom();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeNumberOfRecipients()
	 */
	@Override
	protected int getNativeNumberOfRecipients() {
		try {
			return message.getNumberOfRecipients();
		} catch (PSTException | IOException e) {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsSmtpAddress(int)
	 */
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

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsEmailAddress(int)
	 */
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

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsDisplayName(int)
	 */
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

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsType(int)
	 */
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

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeBodyText()
	 */
	@Override
	protected String getNativeBodyText() {
		return message.getBody();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeBodyHTML()
	 */
	@Override
	protected String getNativeBodyHTML() {
		return message.getBodyHTML();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeRTFBody()
	 */
	@Override
	protected String getNativeRTFBody() {
		try {
			return message.getRTFBody();
		} catch (PSTException | IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getEmbeddedMessageScheme()
	 */
	@Override
	protected String getEmbeddedMessageScheme() {
		return EMBEDDED_MESSAGE;
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessage#getNativeAttachments()
	 */
	@Override
	protected MicrosoftStoreMessageAttachment[] getNativeAttachments() {
		PstStoreMessageAttachment[] psmAttachment;

		psmAttachment = new PstStoreMessageAttachment[message.getNumberOfAttachments()];
		for (int i = 0; i < message.getNumberOfAttachments(); i++) {
			psmAttachment[i] = new PstStoreMessageAttachment(message, i);
		}
		return psmAttachment;
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreMessage#analyzeAppointmentInformation()
	 */
	@Override
	protected void analyzeAppointmentInformation() {
		StoreMessageAppointment appointment = null;
		if (message instanceof PSTAppointment) {
			PSTAppointment pstAppointment = (PSTAppointment) message;

			ZonedDateTime zdtBeg = null;
			Date dBeg = pstAppointment.getStartTime();
			if (dBeg != null) {
				zdtBeg = ZonedDateTime.ofInstant(dBeg.toInstant().minusMillis(0), ZoneOffset.UTC);
			}

			ZonedDateTime zdtEnd = null;
			Date dEnd = pstAppointment.getEndTime();
			if (dEnd != null) {
				zdtEnd = ZonedDateTime.ofInstant(dEnd.toInstant().minusMillis(0), ZoneOffset.UTC);
			}

			String appointmentID = null, appointmentLocation=null;
			try {
				appointmentID = pstAppointment.getCleanGlobalObjectId().toString();
				appointmentLocation =pstAppointment.getLocation();
			} catch (Exception e) {
				// catch a null pointer exception when there's no ID or location
			}

			appointment = new StoreMessageAppointment(appointmentID, appointmentLocation, zdtBeg, zdtEnd);
		}
		this.appointment = appointment;
	}

}
