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

import java.io.*;
import java.net.URLDecoder;

import javax.mail.*;
import javax.mail.Folder;

// TODO: Auto-generated Javadoc
/**
 * JavaMail Store for RFC822 uniq message file.
 * <p><b>Warning:</b>Only for reading and without file locking or new messages management.
 */
public class RFC822Store extends Store {

	/** Raw content of the uniq message file **/
	private byte[] rawContent;

	/**
	 * Constructor, used by the JavaMail library.
	 *
	 * @param session
	 *            the session
	 * @param url
	 *            the url supposed to be formed as rfc822://localhost
	 */
	public RFC822Store(Session session, URLName url) {
		super(session, url);
		rawContent=null;
	}

	/**
	 * Override this service method to implement specific check, including url
	 * and defined directory availability (not in params)
	 * 
	 * <p>
	 * Here control the params coherence RFC822 single mail rfc822://localhost.
	 *
	 * @param host
	 *            only localhost
	 * @param port
	 *            not used
	 * @param user
	 *            not used
	 * @param passwd
	 *            not used
	 * @return true, if successful
	 * @throws MessagingException
	 *             the messaging exception
	 */
	@Override
	protected boolean protocolConnect(String host, int port, String user, String passwd) throws MessagingException {
		// verify params significance in ThunderMBox context
		if (!host.equals("localhost"))
			throw new MessagingException("RFC822: only support localhost");
		if (!((passwd == null) || (passwd.isEmpty())))
			throw new MessagingException("RFC822: does not allow passwords");
		if (port != -1)
			throw new MessagingException("RFC822: does not allow port selection");
		return true;
	}

	/**
	 * Sets the raw binary content.
	 *
	 * @param rawContent
	 *            the binary in memory mail content
	 *            
	 */
	public void setRawContent(byte[] rawContent) {
		this.rawContent = rawContent;
	}

	/**
	 * Gets the raw binary content.
	 *
	 * @return the raw content
	 *            the binary in memory mail content
	 */
	public byte[] getRawContent() {
		return rawContent;
	}

	/**
	 * Verify if a raw binary content exists.
	 *
	 * @return true, if successful
	 */
	public boolean hasRawContent() {
		return !((rawContent==null) || (rawContent.length==0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Store#getDefaultFolder()
	 */
	@Override
	public Folder getDefaultFolder() throws MessagingException {
		return new RFC822Folder(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Store#getFolder(java.lang.String)
	 */
	@Override
	public Folder getFolder(String name) throws MessagingException {
		if ((name==null) || (name.isEmpty()))
			return new RFC822Folder(this);
		else 
			throw new MessagingException("RFC822: only one root simulated folder, no "+name+" folder");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Store#getFolder(javax.mail.URLName)
	 */
	@Override
	public Folder getFolder(URLName url) throws MessagingException {
		// verify that the root directory in store is respected
		String name = "";
		try {
			name = URLDecoder.decode(url.getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// not possible
		}
		if ((name==null) || (name.isEmpty()))
			return new RFC822Folder(this);
		else 
			throw new MessagingException("RFC822: only one root simulated folder, no "+name+" folder");
	}

}
