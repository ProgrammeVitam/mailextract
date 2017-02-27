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

package fr.gouv.vitam.tools.mailextract.lib.core;

import java.util.logging.Logger;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * Abstract class for a mail box folder.
 * <p>
 * It defines high-level methods for extracting and listing recursively folders
 * using low-level methods from the non-abstract sub-classes, according to
 * folder inner organisation. Each subclass has to be able to run through
 * folders and to get messages.
 * <p>
 * The descriptive metadata kept for a folder are:
 * <ul>
 * <li>folder name (Title metadata),</li>
 * <li>the minimum of all the SentDate of messages in this folder or in one of
 * it's descendant, (StartDate metadata),</li>
 * <li>the maximum of all the SentDate of messages in this folder or in one of
 * it's descendant, (EndDate metadata),</li> There's also a DescriptionLevel
 * defined as RecordGrp.
 * </ul>
 */
public abstract class MailBoxFolder {

	/**
	 * Operation store extractor.
	 * <p>
	 * Context for operation on a defined mailbox.
	 **/
	protected StoreExtractor storeExtractor;

	/**
	 * Folder Archive Unit.
	 * <p>
	 * Context for disk writing this folder metadata.
	 **/
	protected ArchiveUnit folderArchiveUnit;

	/**
	 * Folder date range.
	 * <p>
	 * It is computed as the min and max of all the dates of messages in the
	 * folder sub-hierarchy.
	 **/
	protected DateRange dateRange;

	// private fields for folder statistics
	private int folderMessagesCount;
	private int folderSubFoldersCount;
	private long folderMessagesRawSize;

	/**
	 * Instantiates a new mail box folder.
	 *
	 * @param storeExtractor
	 *            the store extractor
	 */
	protected MailBoxFolder(StoreExtractor storeExtractor) {
		this.storeExtractor = storeExtractor;
		this.folderArchiveUnit = null;
		this.dateRange = new DateRange();
	}

	/**
	 * Finalize mail box folder.
	 * <p>
	 * Called at the end of the subclass MailBoxFolder construction to add the
	 * folder {@link ArchiveUnit} with the name depending on father and on the
	 * subclass folder inner name.
	 *
	 * @param father
	 *            the father
	 */
	protected void finalizeMailBoxFolder(MailBoxFolder father) {
		folderArchiveUnit = new ArchiveUnit(storeExtractor, father.folderArchiveUnit,
				"Folder", getName());

	}

	/**
	 * Gets the logger created during the store extractor construction, and used
	 * in all mailextract classes.
	 * 
	 * <p>
	 * For convenience each class which may have some log actions has it's own
	 * getLogger method always returning the store extractor logger.
	 *
	 * @return logger
	 */
	public Logger getLogger() {
		return storeExtractor.getLogger();
	}

	/**
	 * Gets the current operation store extractor.
	 * 
	 * @return storeExtractor
	 */
	public StoreExtractor getStoreExtractor() {
		return storeExtractor;
	}

	/**
	 * Gets the date range.
	 *
	 * @return the date range
	 */
	public DateRange getDateRange() {
		return dateRange;
	}

	/**
	 * Gets the unit node.
	 *
	 * @return the unit node
	 */
	public ArchiveUnit getArchiveUnit() {
		return folderArchiveUnit;
	}

	/**
	 * Gets the full name.
	 * <p>
	 * It depends on the inner representation of folder in subclasses, so
	 * abstract.
	 *
	 * @return the full name, is empty if operation root folder
	 */
	public abstract String getFullName();

	/**
	 * Gets the name.
	 * <p>
	 * It depends on the inner representation of folder in subclasses, so
	 * abstract.
	 *
	 * @return the name, is empty if operation root folder
	 */
	public abstract String getName();

	/**
	 * Increment folder messages count.
	 */
	public void incFolderMessagesCount() {
		folderMessagesCount++;
	}

	/**
	 * Gets the folder messages count.
	 *
	 * @return the folder messages count
	 */
	public int getFolderMessagesCount() {
		return folderMessagesCount;
	}

	/**
	 * Increment the folder subfolders count.
	 */
	public void incFolderSubFoldersCount() {
		folderSubFoldersCount++;
	}

	/**
	 * Gets the folder subfolders count.
	 *
	 * @return the folder sub folders count
	 */
	public int getFolderSubFoldersCount() {
		return folderSubFoldersCount;
	}

	/**
	 * Adds to the folder messages raw size.
	 *
	 * @param rawSize
	 *            the raw size
	 */
	public void addFolderMessagesRawSize(long rawSize) {
		folderMessagesRawSize += rawSize;
	}

	/**
	 * Gets the folder messages raw size.
	 * <p>
	 * The "raw" size is the sum of the size of messages in this specific
	 * folder, not in subfolders.
	 *
	 * @return the folder messages raw size
	 */
	public long getFolderMessagesRawSize() {
		return folderMessagesRawSize;
	}

	/**
	 * Checks if this folder contains messages.
	 *
	 * @return true, if successful
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 **/
	public abstract boolean hasMessages() throws ExtractionException;

	/**
	 * Checks if this folder contains subfolders.
	 *
	 * @return true, if successful
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public abstract boolean hasSubfolders() throws ExtractionException;

	/**
	 * Extract all messages and subfolders as {@link #extractFolder
	 * extractFolder} for the root folder.
	 * <p>
	 * There's a special treatment of the Unit Node for root metadata,
	 * completion and writing being done by the store extractor. This folder is
	 * never dropped, .
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void extractFolderAsRoot() throws ExtractionException {
		// log process on folder
		storeExtractor.getLogger().fine("mailextract: Extract folder [root]");
		// extract all messages in the folder to the unit directory
		extractFolderMessages();
		// extract all subfolders in the folder to the unit directory
		extractSubFolders(0);
		// accumulate in store extractor statistics out of recursion
		storeExtractor.incTotalFoldersCount();
		storeExtractor.addTotalMessagesCount(getFolderMessagesCount());
		storeExtractor.addTotalRawSize(getFolderMessagesRawSize());
	}

	/**
	 * Extract all messages and subfolders.
	 * <p>
	 * This is a method where the extraction structure and content is partially
	 * defined (see also {@link MailBoxMessage#extractMessage extractMessage}
	 * and {@link StoreExtractor#extractAllFolders extractAllFolders})
	 * <p>
	 * It writes on the disk, recursively, all the messages and subfolders in
	 * this folder. For the detailed structure of extraction see class
	 * {@link StoreExtractor}.
	 * 
	 * @param level
	 *            distance from the root folder (used for drop options)
	 * @return true, if not empty
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public boolean extractFolder(int level) throws ExtractionException {
		boolean result = false;

		// log process on folder
		storeExtractor.getLogger().fine("mailextract: Extract folder " + getFullName());
		// extract all messages in the folder to the unit directory
		extractFolderMessages();
		// extract all subfolders in the folder to the unit directory
		extractSubFolders(level);
		if (folderMessagesCount + folderSubFoldersCount != 0 || (!storeExtractor
				.hasOptions(StoreExtractor.CONST_DROP_EMPTY_FOLDERS)
				&& !(level == 1 && storeExtractor.hasOptions(StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS)))) {
			// get specific folder metadata to the unit
			// compute and add to the folder ArchiveUnit the expected folder metadata
			folderArchiveUnit.addMetadata("DescriptionLevel", "RecordGrp", true);
			folderArchiveUnit.addMetadata("Title", getName(), true);
			folderArchiveUnit.addMetadata("Description", "Dossier de messagerie :"+getFullName(), true);
			if (dateRange.isDefined()) {
				folderArchiveUnit.addMetadata("StartDate", DateRange.getISODateString(dateRange.getStart()), true);
				folderArchiveUnit.addMetadata("EndDate", DateRange.getISODateString(dateRange.getEnd()), true);
			}
			folderArchiveUnit.write();
			result = true;
		} else
			storeExtractor.getLogger().fine("mailextract: Empty folder " + getFullName() + " is droped");
		// accumulate in store extractor statistics out of recursion
		storeExtractor.incTotalFoldersCount();
		storeExtractor.addTotalMessagesCount(getFolderMessagesCount());
		storeExtractor.addTotalRawSize(getFolderMessagesRawSize());

		return result;
	}

	// encapsulate the subclasses real processing method
	private void extractFolderMessages() throws ExtractionException {
		folderMessagesCount = 0;
		folderMessagesRawSize = 0;
		if (hasMessages())
			doExtractFolderMessages();
	}

	/**
	 * Extract folder messages (protocol specific).
	 * <p>
	 * It extracts folder messages, count these messages with
	 * {@link #incFolderMessagesCount incFolderMessagesCount}, and accumulate
	 * their raw size with {@link #addFolderMessagesRawSize
	 * addFolderMessagesRawSize}.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected abstract void doExtractFolderMessages() throws ExtractionException;

	// encapsulate the subclasses real processing method
	private void extractSubFolders(int level) throws ExtractionException {
		folderSubFoldersCount = 0;
		if (hasSubfolders())
			doExtractSubFolders(level);
	}

	/**
	 * Extract subfolders (protocol specific).
	 * <p>
	 * It recursively call {@link #extractFolder extractFolder} and count
	 * extracted sub folders with {@link #incFolderSubFoldersCount
	 * incFolderSubFoldersCount}. Must implement drop options.
	 *
	 * @param level
	 *            distance from the root folder (used for drop options)
	 * 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected abstract void doExtractSubFolders(int level) throws ExtractionException;

	/**
	 * List all folders with or without statistics.
	 * <p>
	 * It lists on the console, one by line, the complete list of folders from
	 * the root folder defined in store extractor. If statistics are required,
	 * at the beginning of each line is added the number of messages directly in
	 * the folder and the raw size of theese messages.
	 * 
	 * @param stats
	 *            if true, add folder statistics
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void listFolder(boolean stats) throws ExtractionException {
		// define a specific name "[root]" for the root folder
		String fullName;
		fullName = getFullName();
		if (fullName == null || fullName.isEmpty())
			fullName = "[root]";
		// log process on folder
		storeExtractor.getLogger().fine("mailextract: List folder " + fullName);
		if (stats) {
			// inspect all messages in the folder for statistics
			listFolderMessages();
			// expose this folder statistics
			String size = Double
					.toString(Math.round(((double) folderMessagesRawSize) * 100.0 / (1024.0 * 1024.0)) / 100.0);
			System.out.println(Integer.toString(folderMessagesCount) + " messages\t " + size + " MBytes\t " + fullName);
			getLogger().fine("mailextract: " + Integer.toString(folderMessagesCount) + " messages\t " + size
					+ " MBytes\t " + fullName);
		} else {
			System.out.println(fullName);
		}
		// extract all subfolders in the folder to the unit directory
		listSubFolders(stats);

		// accumulate in store extractor statistics out of recursion
		storeExtractor.incTotalFoldersCount();
		if (stats) {
			storeExtractor.addTotalMessagesCount(getFolderMessagesCount());
			storeExtractor.addTotalRawSize(getFolderMessagesRawSize());
		}
	}

	// encapsulate the subclasses real processing method
	private void listFolderMessages() throws ExtractionException {
		folderMessagesCount = 0;
		folderMessagesRawSize = 0;
		if (hasMessages())
			doExtractFolderMessages();
	}

	/**
	 * List folder messages (protocol specific).
	 * <p>
	 * If stats where asked for, this method is invoked for counting messages
	 * with {@link #incFolderMessagesCount incFolderMessagesCount}, and
	 * accumulating raw size with {@link #addFolderMessagesRawSize
	 * addFolderMessagesRawSize}.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected abstract void doListFolderMessages() throws ExtractionException;

	// encapsulate the subclasses real processing method
	private void listSubFolders(boolean stats) throws ExtractionException {
		folderSubFoldersCount = 0;
		if (hasSubfolders())
			doListSubFolders(stats);
	}

	/**
	 * List subfolders (protocol specific).
	 * <p>
	 * It recursively call {@link #listFolder listFolder} and count sub folders
	 * with {@link #incFolderSubFoldersCount incFolderSubFoldersCount}.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	protected abstract void doListSubFolders(boolean stats) throws ExtractionException;

}
