package fr.gouv.vitam.tools.mailextract.lib.core;

import java.util.Date;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * Utility class to encapsulate an attachment file with content and metadata
 * (for now only filename) size and filename.
 */
public class StoreMessageAttachment {

	// /** Binary raw content. */
	// byte[] rawContent;
	//
	// /** other object content. */
	// Object objectContent;
	//
	/** Attachment content. */
	Object attachmentContent;

	/** Attachment store scheme or null if only an attachment file */
	String attachmentStoreScheme;

	/** Name. */
	String name;

	/** File dates. */
	Date creationDate, modificationDate;

	/** Type of attachment **/
	String mimeType;

	/** Content-ID **/
	String contentID;

	/** Attachment type. */
	int attachmentType;

	// /** Macro types of attachment. */

	/** The Constant FILE_ATTACHMENT. */
	public static final int FILE_ATTACHMENT = 0x00;

	/** The Constant INLINE_ATTACHMENT. */
	public static final int INLINE_ATTACHMENT = 0x01;

	/** The Constant STORE_ATTACHMENT. */
	public static final int STORE_ATTACHMENT = 0x02;

	/**
	 * Instantiates a new attachment with binary content.
	 * 
	 * <p>
	 * The MimeType is normalized to application/* if unknown
	 * </p>
	 *
	 * @param storeContent
	 *            Object to be used by the store extractor or byte[] if simple
	 *            binary
	 * @param storeScheme
	 *            Store scheme defining store extractor or "file" if simple
	 *            binary
	 * @param name
	 *            Name
	 * @param creationDate
	 *            Creation Date
	 * @param modificationDate
	 *            Last modification Date
	 * @param mimeType
	 *            MimeType
	 * @param contentID
	 *            Mime multipart content ID usefull for inline
	 * @param attachmentType
	 *            Type of attachment (inline, simple file, another store...)
	 */

	public StoreMessageAttachment(Object storeContent, String attachmentStoreScheme, String name, Date creationDate,
			Date modificationDate, String mimeType, String contentID, int attachmentType) {
		this.attachmentContent = storeContent;
		this.attachmentStoreScheme = attachmentStoreScheme;
		this.name = name;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.contentID = contentID;
		this.attachmentType = attachmentType;
		setMimeType(mimeType);
	}

	public byte[] getRawAttachmentContent() throws ExtractionException {
		if (attachmentContent instanceof byte[])
			return (byte[]) attachmentContent;
		else
			throw new ExtractionException("mailextract: this attachment has no binary form");
	}

	 public String getScheme() {
	 return attachmentStoreScheme;
	 }
	
	public Object getStoreContent() {
		return attachmentContent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStoreContent(Object attachmentContent) {
		this.attachmentContent = attachmentContent;
	}

	public void setMimeType(String mimeType) {
		// verify valid MimeType and replace if not
		try {
			new MimeType(mimeType);
			this.mimeType = mimeType;
		} catch (MimeTypeParseException e) {
			int i = mimeType.lastIndexOf('/');
			if ((i != -1) && (i < mimeType.length()))
				this.mimeType = "application/" + mimeType.substring(i + 1);
			else
				this.mimeType = "application/octet-stream";
		}
	}

}
