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

package fr.gouv.vitam.tools.mailextract.lib.javamail.rfc822;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.URLName;

/**
 * JavaMail Folder simulated for RFC822 uniq message file.
 * <p>
 * This is the main class for folder analysis and message slicing.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class RFC822Folder extends Folder {

	private volatile boolean opened = false;
	private RFC822Store rfc822store;

	/**
	 * Instantiates a new RFC822 simulated folder.
	 *
	 * @param store
	 *            Store
	 * @throws MessagingException
	 *             Messaging exception from inner JavaMail calls
	 */
	// constructors
	public RFC822Folder(RFC822Store store) throws MessagingException {
		super(store);
		this.rfc822store = store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getName()
	 */
	@Override
	public String getName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getFullName()
	 */
	@Override
	public String getFullName() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getSeparator()
	 */
	// implement inherited abstract method Folder.getSeparator()
	@Override
	public char getSeparator() {
		return File.separatorChar;
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public Folder[] list(String pattern) throws MessagingException {
		throw new MethodNotSupportedException("RFC822: list with pattern not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#list()
	 */
	@Override
	public Folder[] list() throws MessagingException {
		Folder[] result = new Folder[0];

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getParent()
	 */
	@Override
	public Folder getParent() throws MessagingException {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getType()
	 */
	@Override
	public int getType() {
		return HOLDS_MESSAGES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getPermanentFlags()
	 */
	@Override
	public Flags getPermanentFlags() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#hasNewMessages()
	 */
	@Override
	public boolean hasNewMessages() {
		// a simulated folder in memory with one rfc822 mail never changed
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getFolder(java.lang.String)
	 */
	@Override
	public Folder getFolder(String name) throws MessagingException {
		if ((name==null)||(name.isEmpty()))
			return new RFC822Folder(rfc822store);
		else 
			throw new MethodNotSupportedException("RFC822: no folder supported");
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public boolean create(int type) throws MessagingException {
		throw new MethodNotSupportedException("RFC822: no writing supported");
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public boolean delete(boolean recurse) throws MessagingException {
		throw new MethodNotSupportedException("RFC822: no writing supported");
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public boolean renameTo(Folder f) throws MessagingException {
		throw new MethodNotSupportedException("RFC822: no writing supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return opened;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#open(int)
	 */
	@Override
	public void open(int mode) throws MessagingException {
		if (opened)
			throw new IllegalStateException("RFC822: simulated folder is already open");

		this.mode = mode;
		switch (mode) {
		case READ_WRITE:
			throw new MethodNotSupportedException("RFC822: no writing supported");
		case READ_ONLY:
		default:
			if (!rfc822store.hasRawContent())
				throw new MessagingException("RFC822: open failure no raw content");
			break;
		}
		
		opened=true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#close(boolean)
	 */
	@Override
	public void close(boolean expunge) throws MessagingException {
		if (!opened)
			throw new IllegalStateException("RFC822: simulated folder is not open");
		opened = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getMessageCount()
	 */
	@Override
	public int getMessageCount() throws MessagingException {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getMessage(int)
	 */
	@Override
	public Message getMessage(int msgno) throws MessagingException {
		if (msgno != 1)
			throw new IndexOutOfBoundsException("RFC822: only message 1, no message number " + msgno);
		Message m;
		
		// each get regenerate a message with no strong link so that it can be
		// GC. Optimal for the extraction usage with only one get by message
		if (!rfc822store.hasRawContent())
			throw new MessagingException("RFC822: open failure no raw content");
		m = new RFC822Message(this,
					new ByteArrayInputStream(rfc822store.getRawContent()), msgno);

		return m;
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public void appendMessages(Message[] msgs) throws MessagingException {
		throw new MethodNotSupportedException("RFC822: no writing supported");
	}

	/*
	 * Not implemented, cause of no use in mail extract
	 */
	@Override
	public Message[] expunge() throws MessagingException {
		throw new MethodNotSupportedException("RFC822: no writing supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getURLName()
	 */
	@Override
	public URLName getURLName() {
		URLName storeURL = getStore().getURLName();

		return new URLName(storeURL.getProtocol(), storeURL.getHost(), storeURL.getPort(),
				"" /* no folder name */, storeURL.getUsername(),
				null /* no password */);
	}
}
