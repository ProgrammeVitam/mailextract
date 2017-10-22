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

import org.apache.poi.hsmf.MAPIMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * The Class MsgStoreFolder.
 */
public class MsgStoreFolder extends StoreFolder {

	/** The msg store message. */
	MsgStoreMessage msgStoreMessage;

	/**
	 * Instantiates a new msg store folder.
	 *
	 * @param storeExtractor
	 *            the store extractor
	 * @param message
	 *            the message
	 * @param size
	 *            the size
	 */
	public MsgStoreFolder(MsgStoreExtractor storeExtractor, MAPIMessage message, long size) {
		super(storeExtractor);

		this.msgStoreMessage = new MsgStoreMessage(this, message, size);
	}

	/**
	 * Creates the root folder.
	 *
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param message
	 *            the message
	 * @param size
	 *            the size
	 * @param rootArchiveUnit
	 *            Root ArchiveUnit
	 * @return the LP store folder
	 */
	public static MsgStoreFolder createRootFolder(MsgStoreExtractor storeExtractor, MAPIMessage message, long size,
			ArchiveUnit rootArchiveUnit) {
		MsgStoreFolder result = new MsgStoreFolder(storeExtractor, message, size);
		result.folderArchiveUnit = rootArchiveUnit;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#hasMessages()
	 */
	public boolean hasMessages() throws ExtractionException {
		return (true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#hasSubfolders()
	 */
	public boolean hasSubfolders() throws ExtractionException {
		return (false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#
	 * doExtractFolderMessages(boolean)
	 */
	@Override
	protected void doExtractFolderMessages(boolean writeFlag) throws ExtractionException {
		msgStoreMessage.analyzeMessage();
		dateRange.extendRange(msgStoreMessage.getSentDate());
		msgStoreMessage.extractMessage(writeFlag);
		msgStoreMessage.countMessage();

		// return to attachment the binary form if exists
		StoreMessageAttachment attachment = ((MsgStoreExtractor) storeExtractor).getAttachment();
		if (attachment != null) {
			attachment.setStoreContent(msgStoreMessage.getMimeContent());
			attachment.setMimeType("message/rfc822");
			if ((attachment.getName() == null) || attachment.getName().isEmpty())
				attachment.setName(msgStoreMessage.getSubject() + ".eml");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#doExtractSubFolders(
	 * int, boolean)
	 */
	@Override
	protected void doExtractSubFolders(int level, boolean writeFlag) throws ExtractionException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#getFullName()
	 */
	@Override
	public String getFullName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#getName()
	 */
	@Override
	public String getName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#doListFolderMessages
	 * (boolean)
	 */
	@Override
	protected void doListFolderMessages(boolean stats) throws ExtractionException {
		msgStoreMessage.analyzeMessage();
		dateRange.extendRange(msgStoreMessage.getSentDate());
		msgStoreMessage.extractMessage(false);
		msgStoreMessage.countMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#doListSubFolders(
	 * boolean)
	 */
	@Override
	protected void doListSubFolders(boolean stats) throws ExtractionException {
	}
}
