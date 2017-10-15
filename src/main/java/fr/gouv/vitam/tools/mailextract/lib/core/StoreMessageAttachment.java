package fr.gouv.vitam.tools.mailextract.lib.core;

import java.util.Date;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * Utility class to encapsulate an attachment file with content and metadata
 * (for now only filename) size and filename.
 */
public class StoreMessageAttachment {

	/** Binary raw content. */
	byte[] rawContent;

	/** other object content. */
	Object objectContent;

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

	/** Macro types of attachment. */
	public static final int MACRO_ATTACHMENT_TYPE_FILTER = 0xF;

	/** The Constant FILE_ATTACHMENT. */
	public static final int FILE_ATTACHMENT = 0x00;

	/** The Constant INLINE_ATTACHMENT. */
	public static final int INLINE_ATTACHMENT = 0x01;

	/** The Constant STORE_ATTACHMENT. */
	public static final int STORE_ATTACHMENT = 0x02;

	/** Specific store types of attachment. */
	public static final int SPECIFIC_ATTACHMENT_TYPE_FILTER = 0XF0;

	/** The Constant EML_STORE_ATTACHMENT. */
	public static final int EML_STORE_ATTACHMENT = 0x10;

	/** The Constant EMBEDDEDPST_STORE_ATTACHMENT. */
	public static final int EMBEDDEDPST_STORE_ATTACHMENT = 0x20;

	/** The Constant MBOX_STORE_ATTACHMENT. */
	public static final int MBOX_STORE_ATTACHMENT = 0x30;

	/**
	 * Instantiates a new attachment with binary content.
	 * 
	 * <p> The MimeType is normalized to application/* if unknown</p>
	 *
	 * @param name
	 *            name
	 * @param rawContent
	 *            Binary raw content
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

	public StoreMessageAttachment(String name, byte[] rawContent, Date creationDate, Date modificationDate, String mimeType,
			String contentID, int attachmentType) {
		this.name = name;
		this.rawContent = rawContent;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.contentID = contentID;
		this.attachmentType = attachmentType;
		this.objectContent = null;
		setMimeType(mimeType);
	}

	/**
	 * Instantiates a new attachment with an object content.
	 * 
	 * <p> The MimeType is normalized to application/* if unknown</p>
	 *
	 * @param name
	 *            Name
	 * @param objectContent
	 *            Object content
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

	public StoreMessageAttachment(String name, Object objectContent, Date creationDate, Date modificationDate,
			String mimeType, String contentID, int attachmentType) {
		this.name = name;
		this.rawContent = null;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.contentID = contentID;
		this.attachmentType = attachmentType;
		this.objectContent = objectContent;
		setMimeType(mimeType);
	}

	public String getFilename() {
		return name;
	}

	public byte[] getRawContent() {
		return rawContent;
	}

	public Object getObjectContent() {
		return objectContent;
	}

	public void setName(String name) {
		this.name=name;		
	}

	public String getName() {
		return name;
	}

	public void setRawContent(byte[] mimeContent) {
		rawContent=mimeContent;
	}

	public void setMimeType(String mimeType) {
		// verify valid MimeType and replace if not
		try {
			new MimeType(mimeType);
			this.mimeType=mimeType;
		} catch (MimeTypeParseException e) {
			int i = mimeType.lastIndexOf('/');
			if ((i != -1) && (i < mimeType.length()))
				this.mimeType = "application/" + mimeType.substring(i + 1);
			else
				this.mimeType = "application/octet-stream";
		}
	}

}
