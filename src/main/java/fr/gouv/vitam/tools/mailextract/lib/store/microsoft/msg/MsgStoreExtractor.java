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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.apache.poi.hsmf.MAPIMessage;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreMessageAttachment;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.types.EmbeddedStoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

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
public class MsgStoreExtractor extends StoreExtractor implements EmbeddedStoreExtractor {

	/**
	 * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
	 * <p>
	 * This is in default list.
	 */
	static public void subscribeStoreExtractor()
	{
		addExtractionRelation("application/vnd.ms-outlook","msg", false, MsgStoreExtractor.class);
		addExtractionRelation(null,"msg.embeddedmsg", false, MsgStoreExtractor.class);
	}

	// Attachment to complete with decoded form
	private StoreMessageAttachment attachment;

	/**
	 * Instantiates a new msg store extractor.
	 *
	 * @param urlString
	 *            the url string
	 * @param folder
	 *            the folder
	 * @param destPathString
	 *            the dest path string
	 * @param options
	 *            the options
	 * @param rootStoreExtractor
	 *            the root store extractor
	 * @param logger
	 *            the logger
	 * @param osExtractList 
	 * @throws ExtractionException
	 *             the extraction exception
	 */
	public MsgStoreExtractor(String urlString, String folder, String destPathString, StoreExtractorOptions options,
			StoreExtractor rootStoreExtractor, Logger logger, PrintStream psExtractList) throws ExtractionException {
		super(urlString, folder, destPathString, options, rootStoreExtractor, logger,psExtractList);
		MAPIMessage message;
		long size = 0;

		try {
			File messageFile = new File(path);
			message = new MAPIMessage(messageFile);
			size = Files.size(messageFile.toPath());
		} catch (Exception e) {
			throw new ExtractionException(
					"mailExtract.msg: can't open " + path + ", doesn't exist or is not a msg file");
		}

		ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
		setRootFolder(MsgStoreFolder.createRootFolder(this, message, size, rootNode));
	}

	/**
	 * Instantiates a new embedded msg store extractor.
	 *
	 * @param attachment
	 *            the attachment
	 * @param scheme
	 *            the scheme
	 * @param destPathString
	 *            the dest path string
	 * @param options
	 *            the options
	 * @param rootStoreExtractor
	 *            the root store extractor
	 * @param logger
	 *            the logger
	 * @throws ExtractionException
	 *             the extraction exception
	 */
	public MsgStoreExtractor(StoreMessageAttachment attachment, ArchiveUnit rootNode, StoreExtractorOptions options,
			StoreExtractor rootStoreExtractor, Logger logger,PrintStream psExtractList) throws ExtractionException {
		super("msg.embeddedmsg", "", rootNode.getFullName(), options, rootStoreExtractor, logger, psExtractList);
		MAPIMessage message;

		this.attachment = attachment;
		if (attachment.getStoreContent() instanceof MAPIMessage)
			message = (MAPIMessage) attachment.getStoreContent();
		else if (attachment.getStoreContent() instanceof byte[]) {
			ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) attachment.getStoreContent());
			try {
				message = new MAPIMessage(bais);
			} catch (IOException e) {
				throw new ExtractionException("mailextract.msg: Can't extract msg store");
			}
		} else
			throw new ExtractionException("mailextract.msg: Can't extract msg store");

		setRootFolder(MsgStoreFolder.createRootFolder(this, message, 0, rootNode));
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.lib.store.types.EmbeddedStoreExtractor#getAttachment()
	 */
	@Override
	public StoreMessageAttachment getAttachment() {
		return attachment;
	}

	/** The Constant MSG_MN. */
	static final byte[] MSG_MN = new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1,
			0x1A, (byte) 0xE1 };

	/**
	 * Gets the verified scheme.
	 *
	 * @param content
	 *            the content
	 * @return the verified scheme
	 */
	public static String getVerifiedScheme(byte[] content) {
		if (hasMagicNumber(content, MSG_MN)) {
			return "msg";
		} else
			return null;
	}
}
