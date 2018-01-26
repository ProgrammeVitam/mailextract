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

package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.pst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.pff.PSTMessage;
import com.pff.PSTAttachment;
import com.pff.PSTException;

import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessageAttachment;

/**
 * Class for attachment information (MicrosoftStoreMessageAttachment)
 * implementation in pst format.
 */
public class PstStoreMessageAttachment extends MicrosoftStoreMessageAttachment {

	public PstStoreMessageAttachment(PSTMessage message, int i) {
		PSTAttachment attachment;

		try {
			attachment = message.getAttachment(i);
			size = attachment.getAttachSize();
			attachMethod = attachment.getAttachMethod();
			creationTime = attachment.getCreationTime();
			modificationTime = attachment.getModificationTime();
			displayName = attachment.getDisplayName();
			try {
				InputStream is = attachment.getFileInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				int bytesRead;
				while ((bytesRead = is.read(buf)) != -1) {
					baos.write(buf, 0, bytesRead);
				}
				byteArray = baos.toByteArray();
			} catch (PSTException | IOException e) {
				byteArray = null;
			}
			filename = attachment.getFilename();
			longFilename = attachment.getLongFilename();
			try {
				embeddedMessage = attachment.getEmbeddedPSTMessage();
			} catch (PSTException | IOException e) {
				embeddedMessage = null;
			}

			mimeTag = attachment.getMimeTag();
			contentId = attachment.getContentId();
		} catch (PSTException | IOException ee) {
			// forget it
		}
	}
}
