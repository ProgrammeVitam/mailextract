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

package fr.gouv.vitam.tools.mailextract.lib.javamail;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.MailBoxFolder;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.javamail.thundermbox.ThunderMboxFolder;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * MailBoxFolder sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox structure through MailExtract application, could also be used for POP3 and Gmail, via StoreExtractor (not tested). 
 */
public class JMMailBoxFolder extends MailBoxFolder {
	
	/** Native JavaMail folder. */
	protected Folder folder;

	// for the root folder
	private JMMailBoxFolder(StoreExtractor storeExtractor, final Folder folder) {
		super(storeExtractor);
		this.folder = folder;
		if (folder instanceof ThunderMboxFolder)
			((ThunderMboxFolder) folder).setLogger(storeExtractor.getLogger());
	}

	// for a folder with a father
	private JMMailBoxFolder(StoreExtractor storeExtractor, final Folder folder, MailBoxFolder father) {
		super(storeExtractor);
		this.folder = folder;
		if (folder instanceof ThunderMboxFolder)
			((ThunderMboxFolder) folder).setLogger(storeExtractor.getLogger());
		finalizeMailBoxFolder(father);
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
	 * @return the JM mail box folder
	 */
	public static JMMailBoxFolder createRootFolder(StoreExtractor storeExtractor, final Folder folder,
			ArchiveUnit rootArchiveUnit) {
		JMMailBoxFolder result = new JMMailBoxFolder(storeExtractor, folder);
		result.folderArchiveUnit = rootArchiveUnit;

		return result;
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doExtractFolderMessages()
	 */
	@Override
	protected void doExtractFolderMessages() throws ExtractionException {
		int msgtotal;
		Message message;

		try {
			folder.open(Folder.READ_ONLY);
			msgtotal = folder.getMessageCount();
			for (int i = 1; i <= msgtotal; i++) {
				message = folder.getMessage(i);
				if (!((MimeMessage) message).isSet(Flags.Flag.DELETED)) {
					JMMailBoxMessage jMMailBoxMessage = new JMMailBoxMessage(this, (MimeMessage) message);
					jMMailBoxMessage.analyzeMessage();
					dateRange.extendRange(jMMailBoxMessage.getSentDate());
					jMMailBoxMessage.extractMessage();
					jMMailBoxMessage.countMessage();
				}
			}
			folder.close(false);
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get messages from folder " + getFullName());
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doExtractSubFolders(int)
	 */
	@Override
	protected void doExtractSubFolders(int level) throws ExtractionException {
		JMMailBoxFolder mBSubFolder;

		try {
			final Folder[] subfolders = folder.list();

			for (final Folder subfolder : subfolders) {

				mBSubFolder = new JMMailBoxFolder(storeExtractor, subfolder, this);
				if (mBSubFolder.extractFolder(level + 1))
					incFolderSubFoldersCount();
				dateRange.extendRange(mBSubFolder.getDateRange());
			}
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get sub folders from folder " + getFullName());
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getFullName()
	 */
	@Override
	public String getFullName() {
		return folder.getFullName();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getName()
	 */
	@Override
	public String getName() {
		return folder.getName();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasMessages()
	 */
	@Override
	public boolean hasMessages() throws ExtractionException {
		try {
			return (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't determine if folder contains messages" + getFullName());
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasSubfolders()
	 */
	@Override
	public boolean hasSubfolders() throws ExtractionException {
		try {
			return (folder.getType() & Folder.HOLDS_FOLDERS) != 0;
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't determine if folder contains subfolders" + getFullName());
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListFolderMessages()
	 */
	@Override
	protected void doListFolderMessages() throws ExtractionException {
		int msgtotal;
		Message message;

		try {
			folder.open(Folder.READ_ONLY);
			msgtotal = folder.getMessageCount();
			for (int i = 1; i <= msgtotal; i++) {
				message = folder.getMessage(i);
				if (!((MimeMessage) message).isSet(Flags.Flag.DELETED)) {
					JMMailBoxMessage jMMailBoxMessage = new JMMailBoxMessage(this, (MimeMessage) message);
					jMMailBoxMessage.countMessage();
				}
			}
			folder.close(false);
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get messages from folder " + getFullName());
		}

	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListSubFolders(boolean)
	 */
	@Override
	protected void doListSubFolders(boolean stats) throws ExtractionException {
		JMMailBoxFolder mBSubFolder;

		try {
			final Folder[] subfolders = folder.list();

			for (final Folder subfolder : subfolders) {
				mBSubFolder = new JMMailBoxFolder(storeExtractor, subfolder, this);
				mBSubFolder.listFolder(stats);
				incFolderSubFoldersCount();
			}
		} catch (MessagingException e) {
			throw new ExtractionException("MailExtract: Can't get sub folders from folder " + getFullName());
		}
	}
}
