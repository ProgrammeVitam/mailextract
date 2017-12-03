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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.StoragePropertiesChunk;
import org.apache.poi.hsmf.datatypes.StringChunk;

import fr.gouv.vitam.tools.mailextract.lib.store.microsoft.MicrosoftStoreMessageAttachment;

/**
 * Class MsgStoreMessageAttachment for attachment information
 * (MicrosoftStoreMessageAttachment) implementation in POI HSMF format.
 */
public class MsgStoreMessageAttachment extends MicrosoftStoreMessageAttachment {

	public MsgStoreMessageAttachment(AttachmentChunks attachmentChunks) {
		Chunk[] chunkArray = attachmentChunks.getAll();
		List<PropertyValue> lVal;
		StringChunk tmpSC;

		// lack of ATTACH_METHOD, ATTACH_SIZE, CREATION_TIME and
		// LAST_MODIFICATION_TIME in POI
		// get StoragePropertiesChunk for fixed values and find needed
		// properties
		// TODO contribute this to POI
		for (Chunk chunk : chunkArray) {
			if (chunk instanceof StoragePropertiesChunk) {
				StoragePropertiesChunk spChunk = (StoragePropertiesChunk) chunk;
				Map<MAPIProperty, List<PropertyValue>> mapProp = spChunk.getProperties();

				lVal = mapProp.get(MAPIProperty.ATTACH_SIZE);
				if (lVal == null)
					size = 0;
				else
					size = (int) lVal.get(0).getValue();

				lVal = mapProp.get(MAPIProperty.ATTACH_METHOD);
				if (lVal == null)
					attachMethod = 0;
				else
					attachMethod = (int) lVal.get(0).getValue();

				lVal = mapProp.get(MAPIProperty.CREATION_TIME);
				if (lVal != null) {
					Calendar cal = (Calendar) lVal.get(0).getValue();
					if (cal != null)
						creationTime = cal.getTime();
				}

				lVal = mapProp.get(MAPIProperty.LAST_MODIFICATION_TIME);
				if (lVal != null) {
					Calendar cal = (Calendar) lVal.get(0).getValue();
					if (cal != null)
						modificationTime = cal.getTime();
				}
			} else if ((chunk instanceof StringChunk) && (chunk.getChunkId() == MAPIProperty.DISPLAY_NAME.id)) {
				displayName = ((StringChunk) chunk).getValue();
			}
		}
		byteArray = attachmentChunks.getEmbeddedAttachmentObject();
		tmpSC = attachmentChunks.getAttachFileName();
		if (tmpSC != null)
			filename = tmpSC.getValue();
		try {
			embeddedMessage = attachmentChunks.getEmbeddedMessage();
		} catch (IOException e) {
			// forget it
		}
		tmpSC = attachmentChunks.getAttachLongFileName();
		if (tmpSC != null)
			longFilename = tmpSC.getValue();
		tmpSC = attachmentChunks.getAttachMimeTag();
		if (tmpSC != null)
			mimeTag = tmpSC.getValue();
		tmpSC = attachmentChunks.getAttachContentId();
		if (tmpSC != null)
			contentId = tmpSC.getValue();

	}

}
