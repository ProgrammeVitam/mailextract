/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.gouv.vitam.tools.mailextract.lib.store.microsoft.msg;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.PropertyValue;
import org.apache.poi.hsmf.datatypes.StoragePropertiesChunk;
import org.apache.poi.hsmf.datatypes.StringChunk;

/**
 * Class containing attachment information.
 * 
 * @author Richard Johnson
 */
public class MsgAttachment {

	public static final int ATTACHMENT_METHOD_NONE = 0;
	public static final int ATTACHMENT_METHOD_BY_VALUE = 1;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE = 2;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE = 3;
	public static final int ATTACHMENT_METHOD_BY_REFERENCE_ONLY = 4;
	public static final int ATTACHMENT_METHOD_EMBEDDED = 5;
	public static final int ATTACHMENT_METHOD_OLE = 6;

	AttachmentChunks attachmentChunks;
	byte[] byteArray;
	String filename="";
	Date creationTime;
	Date modificationTime;
	MAPIMessage embeddedMessage;
	int attachMethod;
	int size;
	String longFilename="";
	String displayName="";
	String mimeTag="";
	String contentId="";

	public MsgAttachment(AttachmentChunks attachmentChunks) {
		Chunk[] chunkArray = attachmentChunks.getAll();
		List<PropertyValue> lVal;
		StringChunk tmpSC;

		this.attachmentChunks = attachmentChunks;
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
					size = (int)lVal.get(0).getValue();

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
			}
			else if ((chunk instanceof StringChunk) && (chunk.getChunkId()==MAPIProperty.DISPLAY_NAME.id)){
				displayName=((StringChunk) chunk).getValue();
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
