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
package fr.gouv.vitam.tools.mailextract.lib.libpst;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTException;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * StoreExtractor sub-class for mail boxes extracted through libpst library.
 * <p>
 * The java-libpst is a pure java library for the reading of Outlook PST and OST
 * files.
 * <p>
 * This library was originally based off the documentation created through the
 * fantastic reverse engineering effort made by the
 * [libpff](https://sourceforge.net/projects/libpff) project. The library has
 * been improved with information provided by the release of the official PST
 * specs by Microsoft.
 * <p> Thanks to Richard Johnson http://github.com/rjohnsondev
 */
public class LPStoreExtractor extends StoreExtractor {

	private PSTFile pstFile;

	/**
	 * Instantiates a new LP store extractor.
	 *
	 * @param protocol
	 *            Protocol used for extraction (pstfile)
	 * @param server
	 *            Server of target account ((hostname|ip)[:port
	 * @param user
	 *            User account name
	 * @param password
	 *            Password, can be null if not used
	 * @param container
	 *            Path to the local extraction target (Thunderbird or Outlook
	 * @param folder
	 *            Path of the extracted folder in the account mail box, can be
	 *            null if default root folder
	 * @param destRootPath
	 *            Root path of the extraction directory
	 * @param destName
	 *            Name of the extraction directory
	 * @param options
	 *            Options (flag composition of CONST_)
	 * @param logger
	 *            Logger used (from {@link java.util.logging.Logger})
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public LPStoreExtractor(String protocol, String server, String user, String password, String container, String folder, String destRootPath, String destName,
			int options, Logger logger) throws ExtractionException {
		super(protocol, null, null, null, container, folder, destRootPath, destName, options, logger);

		try {
			pstFile = new PSTFile(container);
		} catch (Exception e) {
			throw new ExtractionException("mailExtract.libpst: can't open " + container + ", doesn't exist or is not a pst file");
		}

		ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
		LPMailBoxFolder lPRootMailBoxFolder;

		try {
			PSTFolder pstFolder = findChildFolder(pstFile.getRootFolder(), folder);

			if (pstFolder==null)
				throw new ExtractionException("mailExtract.libpst: Can't find the root folder " + folder + " in pst file");
				
			lPRootMailBoxFolder = LPMailBoxFolder.createRootFolder(this, pstFolder, rootNode);

			rootAnalysisMBFolder = lPRootMailBoxFolder;
		} catch (IOException e) {
			throw new ExtractionException("mailExtract.libpst: Can't use " + container + " pst file");
		} catch (PSTException e) {
			throw new ExtractionException("mailExtract.libpst: Can't find extraction root folder " + folder);
		}
	}

	private static PSTFolder getNamedSubFolder(PSTFolder father, String folderName) throws PSTException, IOException {
		Vector<PSTFolder> pstFolderChilds = father.getSubFolders();
		PSTFolder result = null;

		for (PSTFolder p : pstFolderChilds) {
			if (p.getDisplayName().equals(folderName)) {
				result = p;
				break;
			}
		}
		return result;
	}

	private static PSTFolder findChildFolder(PSTFolder father, String folderFullName) throws PSTException, IOException {
		String regex;
		PSTFolder result = father;

		if (folderFullName == null)
			return result;
		else {
			regex = File.separator;
			if (regex.equals("\\"))
				regex = "\\\\";
			String[] folderHierarchy = folderFullName.split(regex);
			for (int i = 0; i < folderHierarchy.length; i++) {
				if (!folderHierarchy[i].isEmpty()) {
					result = getNamedSubFolder(result, folderHierarchy[i]);
					if (result == null)
						break;
				}
			}
			return result;
		}
	}


}
