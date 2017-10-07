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
package fr.gouv.vitam.tools.mailextract.app;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;

/**
 * MailExtractGraphicApp class for the graphic application.
 */
public class MailExtractGraphicApp implements ActionListener, Runnable {

	/** The main window. */
	private MailExtractMainWindow mainWindow;

	/** The parameters */
	private String destRootPath = "";
	private String destName = "";
	private String protocol = "";
	private String server = "localhost";
	private String user = "";
	private String password = "";
	private String container = "";
	private String folder = "";
	private int storeExtractorOptions = 0;
	private boolean local = true;;
	private String logLevel = "";

	/**
	 * Instantiates a new mail extract graphic app with beginning parameters.
	 *
	 * @param protocol
	 *            the protocol
	 * @param server
	 *            the server
	 * @param user
	 *            the user
	 * @param password
	 *            the password
	 * @param container
	 *            the container
	 * @param folder
	 *            the folder
	 * @param destRootPath
	 *            the dest root path
	 * @param destName
	 *            the dest name
	 * @param storeExtractorOptions
	 *            the store extractor options
	 * @param local
	 *            the local
	 */
	MailExtractGraphicApp(String protocol, String server, String user, String password, String container, String folder,
			String destRootPath, String destName, int storeExtractorOptions, String logLevel, boolean local) {
		this.protocol = protocol;
		this.server = server;
		this.user = user;
		this.password = password;
		this.container = container;
		this.folder = folder;
		this.destRootPath = destRootPath;
		this.destName = destName;
		this.storeExtractorOptions = storeExtractorOptions;
		this.logLevel = logLevel;
		this.local = local;

		EventQueue.invokeLater(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			mainWindow = new MailExtractMainWindow(this);
			insertOptions();
			redirectSystemStreams();
			mainWindow.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// insert all parameters from the command line in the graphic fields
	private void insertOptions() {
		if (local) {
			mainWindow.localRadioButton.doClick();
			if (protocol.equals("pst"))
				mainWindow.pstRadioButton.doClick();
			else if (protocol.equals("mbox"))
				mainWindow.mboxRadioButton.doClick();
			else if (protocol.equals("eml"))
				mainWindow.emlRadioButton.doClick();
			else if (protocol.equals("thunderbird"))
				mainWindow.thunderbirdRadioButton.doClick();
			mainWindow.containerField.setText(container);
		} else {
			mainWindow.protocoleRadioButton.doClick();
			if (protocol.equalsIgnoreCase("imap"))
				mainWindow.imapRadioButton.doClick();
			else
				mainWindow.imapsRadioButton.doClick();
			mainWindow.userField.setText(user);
			mainWindow.passwordField.setText(password);
			mainWindow.serverField.setText(server);
		}
		mainWindow.folderField.setText(folder);
		mainWindow.savedirField.setText(destRootPath);
		mainWindow.nameField.setText(destName);

		if ((storeExtractorOptions & StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS) != 0) {
			mainWindow.keeponlydeepCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions & StoreExtractor.CONST_DROP_EMPTY_FOLDERS) != 0) {
			mainWindow.dropemptyfoldersCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions & StoreExtractor.CONST_WARNING_MSG_PROBLEM) != 0) {
			mainWindow.warningCheckBox.setSelected(true);
		}
		if ((storeExtractorOptions & StoreExtractor.CONST_NAMES_SHORTENED) != 0) {
			mainWindow.nameshortenedCheckBox.setSelected(true);
		}

		// convert from normalized log level name to the choice list log level
		for (int i = 0; i < 7; i++) {
			if (logLevel.equals(loglevelStrings[i])) {
				logLevel = mainWindow.loglevelGraphicStrings[i];
				break;
			}
		}
		mainWindow.loglevelComboBox.setSelectedItem(logLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		// global panel local or protocole enable/disable
		if (command.equals("local")) {
			mainWindow.thunderbirdRadioButton.setEnabled(true);
			mainWindow.pstRadioButton.setEnabled(true);
			mainWindow.mboxRadioButton.setEnabled(true);
			mainWindow.emlRadioButton.setEnabled(true);
			mainWindow.containerLabel.setEnabled(true);
			mainWindow.containerField.setEnabled(true);
			mainWindow.containerButton.setEnabled(true);
			mainWindow.imapRadioButton.setEnabled(false);
			mainWindow.imapsRadioButton.setEnabled(false);
			mainWindow.serverLabel.setEnabled(false);
			mainWindow.serverField.setEnabled(false);
			mainWindow.userLabel.setEnabled(false);
			mainWindow.userField.setEnabled(false);
			mainWindow.passwordLabel.setEnabled(false);
			mainWindow.passwordField.setEnabled(false);
		} else if (command.equals("protocole")) {
			mainWindow.thunderbirdRadioButton.setEnabled(false);
			mainWindow.pstRadioButton.setEnabled(false);
			mainWindow.mboxRadioButton.setEnabled(false);
			mainWindow.emlRadioButton.setEnabled(false);
			mainWindow.containerLabel.setEnabled(false);
			mainWindow.containerField.setEnabled(false);
			mainWindow.containerButton.setEnabled(false);
			mainWindow.imapRadioButton.setEnabled(true);
			mainWindow.imapsRadioButton.setEnabled(true);
			mainWindow.serverLabel.setEnabled(true);
			mainWindow.serverField.setEnabled(true);
			mainWindow.userLabel.setEnabled(true);
			mainWindow.userField.setEnabled(true);
			mainWindow.passwordLabel.setEnabled(true);
			mainWindow.passwordField.setEnabled(true);
		} else if (command.equals("container")) {
			String filename = selectPath(mainWindow.containerField.getText(), false);
			if (filename != null)
				mainWindow.containerField.setText(filename);
		} else if (command.equals("savedir")) {
			String dirname = selectPath(mainWindow.savedirField.getText(), true);
			if (dirname != null)
				mainWindow.savedirField.setText(dirname);
		} else if (command.equals("list"))
			doAction(LIST_ACTION);
		else if (command.equals("stat"))
			doAction(STAT_ACTION);
		else if (command.equals("extract"))
			doAction(EXTRACT_ACTION);
		else if (command.equals("empty"))
			doAction(EMPTY_LOG);
	}

	// get a file and/or directory from a standard selection dialog
	// if dirBool is true only directory can be selected
	private String selectPath(String folder, boolean dirBool) {
		File file;

		JFileChooser fileChooser = new JFileChooser();
		fileChooser
				.setFileSelectionMode((dirBool ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES));
		fileChooser.setFileHidingEnabled(false);
		file = new File(folder);
		if (file.exists())
			fileChooser.setSelectedFile(file);
		int returnVal = fileChooser.showOpenDialog(this.mainWindow);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			return (file.getAbsolutePath());
		} else
			return null;
	}

	/** The Constant LIST_ACTION. */
	static final int LIST_ACTION = 1;

	/** The Constant STAT_ACTION. */
	static final int STAT_ACTION = 2;

	/** The Constant EXTRACT_ACTION. */
	static final int EXTRACT_ACTION = 3;

	/** The Constant EMPTY_LOG. */
	static final int EMPTY_LOG = 4;

	// do one of the 3 actions
	private void doAction(int actionNumber) {
		parseParams();

		if (actionNumber == EMPTY_LOG) {
			mainWindow.consoleTextArea.setText("");
		} else
			new MailExtractThread(actionNumber, protocol, server, user, password, container, folder, destRootPath,
					destName, storeExtractorOptions, logLevel).start();
	}

	String[] loglevelStrings = { "OFF", "SEVERE", "WARNING", "INFO", "FINE", "FINER", "FINEST" };

	// get the global parameters from the graphic fields
	private void parseParams() {
		destRootPath = "";
		destName = "";
		protocol = "";
		server = "localhost";
		user = "";
		password = "";
		container = "";
		folder = "";
		storeExtractorOptions = 0;
		local = true;

		// local
		if (mainWindow.localRadioButton.isSelected()) {
			if (mainWindow.thunderbirdRadioButton.isSelected())
				protocol = "thunderbird";
			else if (mainWindow.emlRadioButton.isSelected())
				protocol = "eml";
			else if (mainWindow.mboxRadioButton.isSelected())
				protocol = "mbox";
			else if (mainWindow.pstRadioButton.isSelected())
				protocol = "pst";
			container = mainWindow.containerField.getText();
		}
		// server
		else {
			if (mainWindow.imapsRadioButton.isSelected())
				protocol = "imaps";
			else
				protocol = "imap";
			server = mainWindow.serverField.getText();
			user = mainWindow.userField.getText();
			password = mainWindow.passwordField.getText();
		}
		folder = mainWindow.folderField.getText();
		destRootPath = mainWindow.savedirField.getText();
		destName = mainWindow.nameField.getText();

		if (mainWindow.keeponlydeepCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_KEEP_ONLY_DEEP_EMPTY_FOLDERS;
		}
		if (mainWindow.dropemptyfoldersCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_DROP_EMPTY_FOLDERS;
		}
		if (mainWindow.warningCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_WARNING_MSG_PROBLEM;
		}
		if (mainWindow.nameshortenedCheckBox.isSelected()) {
			storeExtractorOptions |= StoreExtractor.CONST_NAMES_SHORTENED;
		}

		// convert from log level name in the choice list to normalized log
		// level
		logLevel = (String) mainWindow.loglevelComboBox.getSelectedItem();
		for (int i = 0; i < 7; i++) {
			if (logLevel.equals(mainWindow.loglevelGraphicStrings[i])) {
				logLevel = loglevelStrings[i];
				break;
			}
		}
	}

	// used to update console text area
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainWindow.consoleTextArea.append(text);
			}
		});
	}

	// used to redirect out console stream to text area (no err redirection to
	// avoid tika and other tools errors...)
	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		// System.setErr(new PrintStream(out, true));
	}
}
