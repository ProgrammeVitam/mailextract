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

package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst.embeddedmsg;

import com.pff.PSTMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst.PstStoreMessage;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * StoreFolder sub-class for mail boxes extracted through libpst library.
 * <p>
 * The java-libpst is a pure java library for the reading of Outlook PST and OST
 * files.
 * <p>
 * This library was originally based off the documentation created through the
 * fantastic reverse engineering effort made by the
 * [libpff](https://sourceforge.net/projects/libpff) project. The library has
 * been improved with information provided by the release of the official PST
 * specs by Microsoft.
 * <p>
 * Thanks to Richard Johnson http://github.com/rjohnsondev
 */
public class PstEmbeddedStoreFolder extends StoreFolder {

	// Embedded message
	private PstStoreMessage lpStoreMessage;

	// Attachment of embedded message
	private StoreMessageAttachment attachment;

	// name and fullName computed from constructors
	private String fullName;
	private String name;

	// for the root folder
	private PstEmbeddedStoreFolder(StoreExtractor storeExtractor, StoreMessageAttachment attachment) {
		super(storeExtractor);
		try {
			this.lpStoreMessage = new PstStoreMessage(this, (PSTMessage) (attachment.getObjectContent()));
		} catch (ExtractionException e) {
			// no way
		}
		this.fullName = "";
		this.name = "";
		this.attachment = attachment;
	}

	/**
	 * Creates the root folder.
	 *
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param pstFolder
	 *            Root native libpst folder
	 * @param rootArchiveUnit
	 *            Root ArchiveUnit
	 * @return the LP store folder
	 */
	public static PstEmbeddedStoreFolder createRootFolder(PstEmbeddedStoreExtractor storeExtractor,
			StoreMessageAttachment attachment, ArchiveUnit rootArchiveUnit) {
		PstEmbeddedStoreFolder result = new PstEmbeddedStoreFolder(storeExtractor, attachment);
		result.folderArchiveUnit = rootArchiveUnit;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#
	 * doExtractFolderMessages()
	 */
	@Override
	protected void doExtractFolderMessages(boolean writeFlag) throws ExtractionException {
		lpStoreMessage.analyzeMessage();
		dateRange.extendRange(lpStoreMessage.getSentDate());
		lpStoreMessage.extractMessage(writeFlag);
		lpStoreMessage.countMessage();
		attachment.setRawContent(lpStoreMessage.getMimeContent());
		attachment.setMimeType("message/rfc822");
		if ((attachment.getName()==null) || attachment.getName().isEmpty())
			attachment.setName(lpStoreMessage.getSubject()+".eml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doExtractSubFolders(
	 * int)
	 */
	@Override
	protected void doExtractSubFolders(int level, boolean writeFlag) throws ExtractionException {
		// no subfolders
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getFullName()
	 */
	@Override
	public String getFullName() {
		return fullName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasMessages()
	 */
	@Override
	public boolean hasMessages() throws ExtractionException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasSubfolders()
	 */
	@Override
	public boolean hasSubfolders() throws ExtractionException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListFolderMessages()
	 */
	@Override
	protected void doListFolderMessages(boolean stats) throws ExtractionException {
		lpStoreMessage.countMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListSubFolders(
	 * boolean)
	 */
	@Override
	protected void doListSubFolders(boolean stats) throws ExtractionException {
		// no subfolder
	}
}
