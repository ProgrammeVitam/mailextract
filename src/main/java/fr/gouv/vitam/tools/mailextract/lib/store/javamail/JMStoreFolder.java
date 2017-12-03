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

package fr.gouv.vitam.tools.mailextract.lib.store.javamail;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.javamail.mbox.MboxFolder;
import fr.gouv.vitam.tools.mailextract.lib.store.javamail.thunderbird.ThunderbirdFolder;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * StoreFolder sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox, eml structure through MailExtract application,
 * could also be used for POP3 and Gmail, via StoreExtractor (not tested).
 */
public class JMStoreFolder extends StoreFolder {

	/** Native JavaMail folder. */
	protected Folder folder;

	// for the root folder
	private JMStoreFolder(StoreExtractor storeExtractor, final Folder folder) {
		this(storeExtractor, folder, null);
	}

	// for a folder with a father
	private JMStoreFolder(StoreExtractor storeExtractor, final Folder folder, StoreFolder father) {
		super(storeExtractor);
		this.folder = folder;
		if (folder instanceof ThunderbirdFolder)
			((ThunderbirdFolder) folder).setLogger(storeExtractor.getLogger());
		else if (folder instanceof MboxFolder)
			((MboxFolder) folder).setLogger(storeExtractor.getLogger());
		if (father != null)
			finalizeStoreFolder(father);
	}

	/**
	 * Creates the root folder from which all extraction or listing is done.
	 *
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param folder
	 *            Root native JavaMail folder
	 * @param rootArchiveUnit
	 *            Root ArchiveUnit
	 * @return the JM store folder
	 */
	public static JMStoreFolder createRootFolder(StoreExtractor storeExtractor, final Folder folder,
			ArchiveUnit rootArchiveUnit) {
		JMStoreFolder result = new JMStoreFolder(storeExtractor, folder);
		result.folderArchiveUnit = rootArchiveUnit;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#
	 * doExtractFolderMessages(boolean)
	 */
	@Override
	protected void doExtractFolderElements(boolean writeFlag) throws ExtractionException {
		int msgtotal;
		Message message;

		try {
			folder.open(Folder.READ_ONLY);
			msgtotal = folder.getMessageCount();
			for (int i = 1; i <= msgtotal; i++) {
				message = folder.getMessage(i);
				if (!((MimeMessage) message).isSet(Flags.Flag.DELETED)) {
					JMStoreMessage jMStoreMessage = new JMStoreMessage(this, (MimeMessage) message);
					jMStoreMessage.analyzeMessage();
					dateRange.extendRange(jMStoreMessage.getSentDate());
					jMStoreMessage.extractMessage(writeFlag);
					jMStoreMessage.countMessage();
				}
			}
			folder.close(false);
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get messages from folder " + getFullName());
		}

		// no need to return to attachment the binary form if embedded as it's
		// already the extraction source
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
		JMStoreFolder mBSubFolder;

		try {
			final Folder[] subfolders = folder.list();

			for (final Folder subfolder : subfolders) {

				mBSubFolder = new JMStoreFolder(storeExtractor, subfolder, this);
				if (mBSubFolder.extractFolder(level + 1, writeFlag))
					incFolderSubFoldersCount();
				dateRange.extendRange(mBSubFolder.getDateRange());
			}
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get sub folders from folder " + getFullName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#getFullName()
	 */
	@Override
	public String getFullName() {
		return folder.getFullName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#getName()
	 */
	@Override
	public String getName() {
		return folder.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#hasMessages()
	 */
	@Override
	public boolean hasElements() throws ExtractionException {
		try {
			return (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't determine if folder contains messages" + getFullName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#hasSubfolders()
	 */
	@Override
	public boolean hasSubfolders() throws ExtractionException {
		try {
			return (folder.getType() & Folder.HOLDS_FOLDERS) != 0;
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't determine if folder contains subfolders" + getFullName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.mailextract.lib.core.StoreFolder#doListFolderMessages
	 * (boolean)
	 */
	@Override
	protected void doListFolderElements(boolean stats) throws ExtractionException {
		int msgtotal;
		Message message;

		try {
			folder.open(Folder.READ_ONLY);
			msgtotal = folder.getMessageCount();
			for (int i = 1; i <= msgtotal; i++) {
				message = folder.getMessage(i);
				if (!((MimeMessage) message).isSet(Flags.Flag.DELETED)) {
					JMStoreMessage jMStoreMessage = new JMStoreMessage(this, (MimeMessage) message);
					jMStoreMessage.analyzeMessage();
					if (stats)
						jMStoreMessage.extractMessage(false);
					jMStoreMessage.countMessage();

				}
			}
			folder.close(false);
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get messages from folder " + getFullName());
		}

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
		JMStoreFolder mBSubFolder;

		try {
			final Folder[] subfolders = folder.list();

			for (final Folder subfolder : subfolders) {
				mBSubFolder = new JMStoreFolder(storeExtractor, subfolder, this);
				mBSubFolder.listFolder(stats);
				incFolderSubFoldersCount();
			}
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get sub folders from folder " + getFullName());
		}
	}
}
