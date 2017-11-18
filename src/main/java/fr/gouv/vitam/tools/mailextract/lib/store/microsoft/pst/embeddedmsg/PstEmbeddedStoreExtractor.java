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

import java.io.PrintStream;
import java.util.logging.Logger;

import com.pff.PSTMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.types.EmbeddedStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * StoreExtractor sub-class for embedded messages extracted through libpst
 * library.
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
public class PstEmbeddedStoreExtractor extends StoreExtractor implements EmbeddedStoreExtractor{

	/**
	 * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
	 * <p>
	 * This is in default list.
	 */
	static public void subscribeStoreExtractor()
	{
		addExtractionRelation(null,"pst.embeddedmsg", true, PstEmbeddedStoreExtractor.class);
	}
	
	// Attachment to complete with decoded form
	private StoreMessageAttachment attachment;

	/**
	 * Instantiates a new LP embedded message store extractor.
	 *
	 * @param attachment
	 *            the attachment
	 * @param destPathString
	 *            the dest path string
	 * @param options
	 *            Options (flag composition of CONST_)
	 * @param rootStoreExtractor
	 *            the creating store extractor in nested extraction, or null if
	 *            root one
	 * @param logger
	 *            Logger used (from {@link java.util.logging.Logger})
	 * @param osExtractList 
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public PstEmbeddedStoreExtractor(StoreMessageAttachment attachment, ArchiveUnit rootNode,
			StoreExtractorOptions options, StoreExtractor rootStoreExtractor, Logger logger, PrintStream psExtractList)
			throws ExtractionException {
		super("pst.embeddedmsg", "", rootNode.getFullName(), options, rootStoreExtractor, logger, psExtractList);

		this.attachment=attachment;
		setRootFolder(PstEmbeddedStoreFolder.createRootFolder((PSTMessage)attachment.getStoreContent(),this,rootNode));
	}

	@Override
	public StoreMessageAttachment getAttachment() {
		return attachment;
	}

}
