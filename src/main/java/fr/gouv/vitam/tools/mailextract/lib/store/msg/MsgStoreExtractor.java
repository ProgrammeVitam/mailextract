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
package fr.gouv.vitam.tools.mailextract.lib.store.msg;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.poi.hsmf.MAPIMessage;
import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;

/**
 * StoreExtractor sub-class for message file extracted through POI library.
 * <p>
 * The POI Apache is a pure java library for the reading Microsoft documents.
 * It's used to access the formatted msg file.
 * <p>
 * The Apache POI Project's mission is to create and maintain Java APIs for
 * manipulating various file formats based upon the Office Open XML standards
 * (OOXML) and Microsoft's OLE 2 Compound Document format (OLE2). In short, you
 * can read and write MS Excel files using Java. In addition, you can read and
 * write MS Word and MS PowerPoint files using Java. Apache POI is your Java
 * Excel solution (for Excel 97-2008). We have a complete API for porting other
 * OOXML and OLE2 formats and welcome others to participate.
 */
public class MsgStoreExtractor extends StoreExtractor {

	// /**
	// * Instantiates a new msg store extractor.
	// *
	// * @param protocol
	// * Protocol used for extraction (pstfile)
	// * @param server
	// * Server of target account ((hostname|ip)[:port
	// * @param user
	// * User account name
	// * @param password
	// * Password, can be null if not used
	// * @param container
	// * Path to the local extraction target (Thunderbird or Outlook)
	// * @param folder
	// * Path of the extracted folder in the account mail box, can be
	// * null if default root folder
	// * @param destRootPath
	// * Root path of the extraction directory
	// * @param destName
	// * Name of the extraction directory
	// * @param options
	// * Options (flag composition of CONST_)
	// * @param options
	// * Options (flag composition of CONST_)
	// * @param rootStoreExtractor
	// * the creating store extractor in nested extraction, or null if
	// * root one
	// * @param logger
	// * Logger used (from {@link java.util.logging.Logger})
	// * @throws ExtractionException
	// * Any unrecoverable extraction exception (access trouble, major
	// * format problems...)
	// */
	public MsgStoreExtractor(String protocol, String server, String user, String password, String container,
			String folder, String destRootPath, String destName, int options, StoreExtractor rootStoreExtractor,
			Logger logger) throws ExtractionException {
		super(protocol, null, null, null, container, folder, destRootPath, destName, options, rootStoreExtractor,
				logger);
		MAPIMessage message;
		long size = 0;

		try {
			File messageFile = new File(container);
			message = new MAPIMessage(messageFile);
			size = Files.size(messageFile.toPath());
		} catch (Exception e) {
			throw new ExtractionException(
					"mailExtract.msg: can't open " + container + ", doesn't exist or is not a msg file");
		}

		ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
		rootAnalysisMBFolder = MsgStoreFolder.createRootFolder(this, message, size, rootNode);
	}
}
