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

/**
 * StoreExtractorOptions class for all extraction options.
 */
public class StoreExtractorOptions {
	
	public static final int MODEL_V1=1;
	public static final int MODEL_V2=2;

	/** The keep only deep empty folders flag. */
	public boolean keepOnlyDeepEmptyFolders;

	/** The drop empty folders flag. */
	public boolean dropEmptyFolders;

	/** The warning msg problem flag. */
	public boolean warningMsgProblem;

	/** The names length. */
	public int namesLength;

	/** The default charset name. */
	public String defaultCharsetName;

	/** The extract list flag. */
	public boolean extractList;

	/** The extract message text version file flag. */
	public boolean extractMessageTextFile;

	/** The extract message text metadata flag. */
	public boolean extractMessageTextMetadata;

	/** The extract attachment file text version file flag. */
	public boolean extractFileTextFile;

	/** The extract attachment file text metadata flag. */
	public boolean extractFileTextMetadata;

	/** The model of extraction on disk. */
	public int model;

	/**
	 * Instantiates a new store extractor options.
	 */
	public StoreExtractorOptions() {
		keepOnlyDeepEmptyFolders = false;
		dropEmptyFolders = false;
		warningMsgProblem = false;
		namesLength = 12;
		extractList = false;
		extractMessageTextFile = false;
		extractMessageTextMetadata = false;
		extractFileTextFile = false;
		extractFileTextMetadata = false;
		defaultCharsetName="windows-1252";
		model = 2;
	}

	/**
	 * Instantiates a new store extractor options.
	 *
	 * @param keepOnlyDeepEmptyFolders   the keep only deep empty folders
	 * @param dropEmptyFolders           the drop empty folders
	 * @param warningMsgProblem          the warning msg problem
	 * @param namesLength                the names length
	 * @param defaultCharsetName         the default charset name
	 * @param extractList                the extract list
	 * @param extractMessageTextFile     the extract message text file
	 * @param extractMessageTextMetadata the extract message text metadata
	 * @param extractFileTextFile        the extract file text file
	 * @param extractFileTextMetadata    the extract file text metadata
	 * @param model 					 the model
	 */
	public StoreExtractorOptions(boolean keepOnlyDeepEmptyFolders, boolean dropEmptyFolders, boolean warningMsgProblem,
			int namesLength, String defaultCharsetName, boolean extractList, boolean extractMessageTextFile, boolean extractMessageTextMetadata,
			boolean extractFileTextFile, boolean extractFileTextMetadata, int model) {
		this.keepOnlyDeepEmptyFolders = keepOnlyDeepEmptyFolders;
		this.dropEmptyFolders = dropEmptyFolders;
		this.warningMsgProblem = warningMsgProblem;
		this.namesLength = namesLength;
		this.defaultCharsetName=defaultCharsetName;
		this.extractList = extractList;
		this.extractMessageTextFile = extractMessageTextFile;
		this.extractMessageTextMetadata = extractMessageTextMetadata;
		this.extractFileTextFile = extractFileTextFile;
		this.extractFileTextMetadata = extractFileTextMetadata;
		this.model = model;
	}
}
