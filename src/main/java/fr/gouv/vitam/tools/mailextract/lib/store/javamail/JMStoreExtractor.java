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

package fr.gouv.vitam.tools.mailextract.lib.store.javamail;

import javax.mail.*;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.store.javamail.JMStoreFolder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * StoreExtractor sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox structure through MailExtract application,
 * could also be used for POP3 and Gmail, via StoreExtractor (not tested).
 */
public class JMStoreExtractor extends StoreExtractor {
	private Store store;

	/**
	 * Instantiates a new JavaMail StoreExtractor for protcole or complex
	 * containers.
	 *
	 * @param protocol
	 *            Protocol used for extraction (imap| thunderbird [not tested
	 *            gimap| pop3])
	 * @param server
	 *            Server of target account ((hostname|ip)[:port
	 * @param user
	 *            User account name
	 * @param password
	 *            Password, can be null if not used
	 * @param container
	 *            Path to the local extraction target (Thunderbird or Outlook
	 * @param folder
	 *            Path of the extracted folder in the account mail box, can be
	 *            null if default root folder
	 * @param destRootPath
	 *            Root path of the extraction directory
	 * @param destName
	 *            Name of the extraction directory
	 * @param options
	 *            Options (flag composition of CONST_)
	 * @param rootStoreExtractor
	 *            the creating store extractor in nested extraction, or null if
	 *            root one
	 * @param logger
	 *            Logger used (from {@link java.util.logging.Logger})
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public JMStoreExtractor(String protocol, String server, String user, String password, String container,
			String folder, String destRootPath, String destName, int options, StoreExtractor rootStoreExtractor,
			Logger logger) throws ExtractionException {
		super(protocol, server, user, password, container, folder, destRootPath, destName, options, rootStoreExtractor,
				logger);

		String url = "";

		try {
			url = protocol + "://";
			if (user != null && !user.isEmpty()) {
				url += URLEncoder.encode(user, "UTF-8");
				if (password != null && !password.isEmpty())
					url += ":" + URLEncoder.encode(password, "UTF-8");
				url += "@";
			}
			if (server != null && !server.isEmpty())
				url += server;
			else
				url += "localhost";
			if (container != null && !container.isEmpty())
				url += "/" + URLEncoder.encode(container, "UTF-8");
			else if (folder != null && !folder.isEmpty())
				url += "/" + URLEncoder.encode(folder, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// impossible case with UTF-8
		}

		try {
			// Connect to the store
			Properties props = System.getProperties();
			props.setProperty("mail.imaps.ssl.trust", "*");
			props.setProperty("mail.imap.ssl.trust", "*");
			setSessionProperties(props);
			Session session = Session.getDefaultInstance(props, null);

			// add thunderbird provider
			session.addProvider(new Provider(Provider.Type.STORE, "thunderbird",
					fr.gouv.vitam.tools.mailextract.lib.store.javamail.thunderbird.ThunderbirdStore.class.getName(),
					"fr.gouv.vitam", getClass().getPackage().getImplementationVersion()));
			// add eml provider
			session.addProvider(new Provider(Provider.Type.STORE, "eml",
					fr.gouv.vitam.tools.mailextract.lib.store.javamail.eml.EmlStore.class.getName(), "fr.gouv.vitam",
					getClass().getPackage().getImplementationVersion()));
			// add mbox provider
			session.addProvider(new Provider(Provider.Type.STORE, "mbox",
					fr.gouv.vitam.tools.mailextract.lib.store.javamail.mbox.MboxStore.class.getName(), "fr.gouv.vitam",
					getClass().getPackage().getImplementationVersion()));

			URLName urlName = new URLName(url);
			store = session.getStore(urlName);
			store.connect();
		} catch (MessagingException e) {
			throw new ExtractionException("mailextract.javamail: can't get store for " + getDecodedURL(url)
					+ System.lineSeparator() + e.getMessage());
		}

		ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
		JMStoreFolder jMRootMailBoxFolder;

		try {
			if ((folder == null) || folder.isEmpty())

				jMRootMailBoxFolder = JMStoreFolder.createRootFolder(this, store.getDefaultFolder(), rootNode);
			else
				jMRootMailBoxFolder = JMStoreFolder.createRootFolder(this, store.getFolder(folder), rootNode);

			if (!jMRootMailBoxFolder.folder.exists()) {
				throw new ExtractionException("mailextract.javamail: Can't find extraction root folder " + folder);
			}
			rootAnalysisMBFolder = jMRootMailBoxFolder;
		} catch (MessagingException e) {
			throw new ExtractionException("mailextract.javamail: Can't find extraction root folder " + folder);
		}
	}

	// decode URL encoding to UTF-8
	static private String getDecodedURL(String url) {
		String decodedUrl = "";
		try {
			decodedUrl = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// non sense for UTF-8
		}
		return decodedUrl;
	}

	// set properties to have the most tolerant reader...
	private void setSessionProperties(Properties props) {
		props.setProperty("mail.mime.decodetext.strict", "false"); // not
																	// default
		props.setProperty("mail.mime.encodeeol.strict", "false");
		props.setProperty("mail.mime.decodefilename", "true"); // not default
		props.setProperty("mail.mime.decodeparameters", "true");
		props.setProperty("mail.mime.multipart.ignoremissingendboundary", "true");
		props.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true");
		props.setProperty("mail.mime.parameters.strict", "false"); // not
																	// default
		props.setProperty("mail.mime.windowsfilenames", "true"); // not default
		// props.setProperty("mail.mime.ignoremultipartencoding", "false");
		// //not default
	}
}
