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

import javax.swing.JFrame;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import javax.swing.SwingConstants;

/**
 * MailExtractMainWindow class for the main window with all parameters field and
 * console text area
 */
public class MailExtractMainWindow extends JFrame {

	private static final long serialVersionUID = 4607177374736676766L;

	private MailExtractGraphicApp app;

	/** The folder field. */
	JTextField folderField;

	/** The savedir field. */
	JTextField savedirField;

	/** The local radio button. */
	JRadioButton localRadioButton;

	/** The thunderbird radio button. */
	JRadioButton thunderbirdRadioButton;

	/** The outlook radio button. */
	JRadioButton pstRadioButton;

	/** The mbox radio button. */
	JRadioButton mboxRadioButton;

	/** The eml radio button. */
	JRadioButton emlRadioButton;

	/** The eml radio button. */
	JRadioButton msgRadioButton;

	/** The name label. */
	JLabel nameLabel;

	/** The name field. */
	JTextField nameField;

	/** The container label. */
	JLabel containerLabel;

	/** The container field. */
	JTextField containerField;

	/** The container button. */
	JButton containerButton;

	/** The protocole radio button. */
	JRadioButton protocoleRadioButton;

	/** The imap radio button. */
	JRadioButton imapRadioButton;

	/** The imaps radio button. */
	JRadioButton imapsRadioButton;

	/** The server label. */
	JLabel serverLabel;

	/** The server field. */
	JTextField serverField;

	/** The user label. */
	JLabel userLabel;

	/** The user field. */
	JTextField userField;

	/** The password label. */
	JLabel passwordLabel;

	/** The password field. */
	JTextField passwordField;

	/** The loglevel combo box. */
	@SuppressWarnings("rawtypes")
	JComboBox loglevelComboBox;

	/** The warning check box. */
	JCheckBox warningCheckBox;

	/** The keeponlydeep check box. */
	JCheckBox keeponlydeepCheckBox;

	/** The dropemptyfolders check box. */
	JCheckBox dropemptyfoldersCheckBox;

	/** The names length field. */
	JTextField namesLengthField;

	/** The console text area. */
	JTextArea consoleTextArea;
	private JScrollPane scrollPane;

	/** The proposed log level */
	String[] loglevelGraphicStrings = { "OFF", "ERREUR FATALE", "AVERTISSEMENT", "INFO PROCESS", "INFO DOSSIERS",
			"INFO MESSAGES", "DETAIL MESSAGES" };

	/**
	 * Gets the global graphic app.
	 *
	 * @return the app
	 */
	public MailExtractGraphicApp getApp() {
		return app;
	}

	/**
	 * Create the main window and initialize all the frames.
	 *
	 * @param app
	 *            the app
	 */
	public MailExtractMainWindow(MailExtractGraphicApp app) {
		super();
		this.app = app;
		initialize();
	}

	// Initialize the contents of the frame.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize() {
		java.net.URL imageURL = getClass().getClassLoader().getResource("Logo96.png");
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			setIconImage(icon.getImage());
		}
		this.setTitle("MailExtract");

		getContentPane().setPreferredSize(new Dimension(800, 600));
		setBounds(0, 0, 800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		gridBagLayout.columnWeights = new double[] { 1, 1, 1, 1};
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 20, 20, 0, 0 };
		getContentPane().setLayout(gridBagLayout);

		consoleTextArea = new JTextArea();
		consoleTextArea.setFont(new Font("Courier 10 Pitch", Font.BOLD, 12));
		consoleTextArea.setLineWrap(true);

		scrollPane = new JScrollPane(consoleTextArea);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.insets = new Insets(5, 5, 0, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 11;
		getContentPane().add(scrollPane, gbc_scrollPane);

		JButton extractButton = new JButton("Extraire messages");
		GridBagConstraints gbc_extractButton = new GridBagConstraints();
		gbc_extractButton.gridwidth = 1;
		gbc_extractButton.insets = new Insets(0, 0, 10, 10);
		gbc_extractButton.gridx = 0;
		gbc_extractButton.gridy = 10;
		getContentPane().add(extractButton, gbc_extractButton);
		extractButton.setActionCommand("extract");
		extractButton.addActionListener(app);

		JButton listButton = new JButton("Lister dossiers");
		GridBagConstraints gbc_listButton = new GridBagConstraints();
		gbc_listButton.anchor = GridBagConstraints.EAST;
		gbc_listButton.insets = new Insets(0, 0, 10, 10);
		gbc_listButton.gridx = 1;
		gbc_listButton.gridy = 10;
		getContentPane().add(listButton, gbc_listButton);
		listButton.setActionCommand("list");
		listButton.addActionListener(app);

		JButton statButton = new JButton("Lister stats dossiers");
		GridBagConstraints gbc_statButton = new GridBagConstraints();
		gbc_statButton.insets = new Insets(0, 0, 10, 10);
		gbc_statButton.gridx = 2;
		gbc_statButton.gridy = 10;
		getContentPane().add(statButton, gbc_statButton);
		statButton.setActionCommand("stat");
		statButton.addActionListener(app);

		JButton emptyButton = new JButton("Vider log");
		GridBagConstraints gbc_emptyButton = new GridBagConstraints();
		gbc_emptyButton.insets = new Insets(0, 0, 10, 10);
		gbc_emptyButton.gridx = 3;
		gbc_emptyButton.gridy = 10;
		getContentPane().add(emptyButton, gbc_emptyButton);
		emptyButton.setActionCommand("empty");
		emptyButton.addActionListener(app);

		warningCheckBox = new JCheckBox("Remonte les pbs sur les messages");
		GridBagConstraints gbc_warningCheckBox = new GridBagConstraints();
		gbc_warningCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_warningCheckBox.gridx = 2;
		gbc_warningCheckBox.gridy = 8;
		getContentPane().add(warningCheckBox, gbc_warningCheckBox);

		loglevelComboBox = new JComboBox(loglevelGraphicStrings);
		GridBagConstraints gbc_loglevelComboBox = new GridBagConstraints();
		gbc_loglevelComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_loglevelComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_loglevelComboBox.gridx = 1;
		gbc_loglevelComboBox.gridy = 8;
		getContentPane().add(loglevelComboBox, gbc_loglevelComboBox);

		JLabel loglevelLabel = new JLabel("Niveau de log");
		GridBagConstraints gbc_loglevelLabel = new GridBagConstraints();
		gbc_loglevelLabel.anchor = GridBagConstraints.EAST;
		gbc_loglevelLabel.insets = new Insets(0, 0, 5, 5);
		gbc_loglevelLabel.gridx = 0;
		gbc_loglevelLabel.gridy = 8;
		getContentPane().add(loglevelLabel, gbc_loglevelLabel);

		keeponlydeepCheckBox = new JCheckBox("Garder dossier 1er niv");
		GridBagConstraints gbc_keeponlydeepRadioButton = new GridBagConstraints();
		gbc_keeponlydeepRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_keeponlydeepRadioButton.gridx = 1;
		gbc_keeponlydeepRadioButton.gridy = 5;
		getContentPane().add(keeponlydeepCheckBox, gbc_keeponlydeepRadioButton);

		JLabel namesLengthLabel = new JLabel("Longueur gardée des noms");
		GridBagConstraints gbc_namesLengthLabel = new GridBagConstraints();
		gbc_namesLengthLabel.anchor = GridBagConstraints.EAST;
		gbc_namesLengthLabel.insets = new Insets(0, 0, 5, 5);
		gbc_namesLengthLabel.gridx = 2;
		gbc_namesLengthLabel.gridy = 5;
		getContentPane().add(namesLengthLabel, gbc_namesLengthLabel);

		namesLengthField = new JTextField();
		GridBagConstraints gbc_namesLengthField = new GridBagConstraints();
		gbc_namesLengthField.weightx = 0.5;
		gbc_namesLengthField.insets = new Insets(0, 0, 5, 10);
		gbc_namesLengthField.fill = GridBagConstraints.HORIZONTAL;
		gbc_namesLengthField.gridx = 3;
		gbc_namesLengthField.gridy = 5;
		getContentPane().add(namesLengthField, gbc_namesLengthField);
		namesLengthField.setColumns(128);

		JButton savedirButton = new JButton("Répertoire...");
		GridBagConstraints gbc_savedirButton = new GridBagConstraints();
		gbc_savedirButton.insets = new Insets(0, 0, 5, 0);
		gbc_savedirButton.gridx = 3;
		gbc_savedirButton.gridy = 6;
		getContentPane().add(savedirButton, gbc_savedirButton);
		savedirButton.setActionCommand("savedir");
		savedirButton.addActionListener(app);

		savedirField = new JTextField();
		GridBagConstraints gbc_savedirField = new GridBagConstraints();
		gbc_savedirField.gridwidth = 2;
		gbc_savedirField.insets = new Insets(0, 0, 5, 5);
		gbc_savedirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_savedirField.gridx = 1;
		gbc_savedirField.gridy = 6;
		gbc_savedirField.weightx = 0.5;
		getContentPane().add(savedirField, gbc_savedirField);

		dropemptyfoldersCheckBox = new JCheckBox("Eliminer dossiers vides");
		GridBagConstraints gbc_dropemptyfoldersCheckBox = new GridBagConstraints();
		gbc_dropemptyfoldersCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_dropemptyfoldersCheckBox.gridx = 0;
		gbc_dropemptyfoldersCheckBox.gridy = 5;
		getContentPane().add(dropemptyfoldersCheckBox, gbc_dropemptyfoldersCheckBox);

		JLabel savedirLabel = new JLabel("Répertoire d'extraction");
		GridBagConstraints gbc_savedirLabel = new GridBagConstraints();
		gbc_savedirLabel.anchor = GridBagConstraints.EAST;
		gbc_savedirLabel.insets = new Insets(0, 0, 5, 5);
		gbc_savedirLabel.gridx = 0;
		gbc_savedirLabel.gridy = 6;
		getContentPane().add(savedirLabel, gbc_savedirLabel);

		folderField = new JTextField();
		GridBagConstraints gbc_folderField = new GridBagConstraints();
		gbc_folderField.gridwidth = 3;
		gbc_folderField.weightx = 0.5;
		gbc_folderField.insets = new Insets(0, 0, 5, 10);
		gbc_folderField.fill = GridBagConstraints.HORIZONTAL;
		gbc_folderField.gridx = 1;
		gbc_folderField.gridy = 4;
		getContentPane().add(folderField, gbc_folderField);
		folderField.setColumns(128);

		JLabel folderLabel = new JLabel("Dossier Racine");
		GridBagConstraints gbc_folderLabel = new GridBagConstraints();
		gbc_folderLabel.anchor = GridBagConstraints.EAST;
		gbc_folderLabel.insets = new Insets(0, 0, 5, 5);
		gbc_folderLabel.gridx = 0;
		gbc_folderLabel.gridy = 4;
		getContentPane().add(folderLabel, gbc_folderLabel);

		localRadioButton = new JRadioButton("Extraction locale");
		localRadioButton.setSelected(true);
		GridBagConstraints gbc_localRadioButton = new GridBagConstraints();
		gbc_localRadioButton.gridwidth = 2;
		gbc_localRadioButton.weightx = 0.5;
		gbc_localRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_localRadioButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_localRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_localRadioButton.gridx = 0;
		gbc_localRadioButton.gridy = 0;
		getContentPane().add(localRadioButton, gbc_localRadioButton);
		localRadioButton.setActionCommand("local");
		localRadioButton.addActionListener(app);

		protocoleRadioButton = new JRadioButton("Extraction serveur");
		GridBagConstraints gbc_protocoleRadioButton = new GridBagConstraints();
		gbc_protocoleRadioButton.gridwidth = 2;
		gbc_protocoleRadioButton.weightx = 0.5;
		gbc_protocoleRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_protocoleRadioButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_protocoleRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_protocoleRadioButton.gridx = 0;
		gbc_protocoleRadioButton.gridy = 2;
		getContentPane().add(protocoleRadioButton, gbc_protocoleRadioButton);
		protocoleRadioButton.setActionCommand("protocole");
		protocoleRadioButton.addActionListener(app);

		JPanel serverPanel = new JPanel();
		serverPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_serverPanel = new GridBagConstraints();
		gbc_serverPanel.gridwidth = 4;
		gbc_serverPanel.weightx = 1.0;
		gbc_serverPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_serverPanel.insets = new Insets(0, 10, 10, 10);
		gbc_serverPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverPanel.gridx = 0;
		gbc_serverPanel.gridy = 3;
		getContentPane().add(serverPanel, gbc_serverPanel);
		GridBagLayout gbl_serverPanel = new GridBagLayout();
		serverPanel.setLayout(gbl_serverPanel);
		serverPanel.setEnabled(false);

		imapRadioButton = new JRadioButton("IMAP");
		imapRadioButton.setEnabled(false);
		GridBagConstraints gbc_imapRadioButton = new GridBagConstraints();
		gbc_imapRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_imapRadioButton.gridx = 1;
		gbc_imapRadioButton.gridy = 0;
		serverPanel.add(imapRadioButton, gbc_imapRadioButton);

		imapsRadioButton = new JRadioButton("IMAPS");
		imapsRadioButton.setEnabled(false);
		imapsRadioButton.setSelected(true);
		GridBagConstraints gbc_imapsRadioButton = new GridBagConstraints();
		gbc_imapsRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_imapsRadioButton.gridx = 0;
		gbc_imapsRadioButton.gridy = 0;
		serverPanel.add(imapsRadioButton, gbc_imapsRadioButton);

		serverLabel = new JLabel("Serveur");
		serverLabel.setEnabled(false);
		GridBagConstraints gbc_serverLabel = new GridBagConstraints();
		gbc_serverLabel.anchor = GridBagConstraints.EAST;
		gbc_serverLabel.insets = new Insets(0, 0, 5, 5);
		gbc_serverLabel.gridx = 0;
		gbc_serverLabel.gridy = 3;
		serverPanel.add(serverLabel, gbc_serverLabel);

		userLabel = new JLabel("ID");
		userLabel.setEnabled(false);
		GridBagConstraints gbc_userLabel = new GridBagConstraints();
		gbc_userLabel.anchor = GridBagConstraints.EAST;
		gbc_userLabel.insets = new Insets(0, 0, 5, 5);
		gbc_userLabel.gridx = 0;
		gbc_userLabel.gridy = 1;
		serverPanel.add(userLabel, gbc_userLabel);

		userField = new JTextField();
		userField.setEnabled(false);
		GridBagConstraints gbc_userField = new GridBagConstraints();
		gbc_userField.weightx = 0.5;
		gbc_userField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userField.insets = new Insets(0, 0, 5, 5);
		gbc_userField.anchor = GridBagConstraints.NORTHWEST;
		gbc_userField.gridwidth = 2;
		gbc_userField.gridx = 1;
		gbc_userField.gridy = 1;
		serverPanel.add(userField, gbc_userField);
		userField.setColumns(128);

		passwordLabel = new JLabel("Mot de passe");
		passwordLabel.setEnabled(false);
		GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
		gbc_passwordLabel.anchor = GridBagConstraints.EAST;
		gbc_passwordLabel.insets = new Insets(0, 0, 5, 5);
		gbc_passwordLabel.gridx = 0;
		gbc_passwordLabel.gridy = 2;
		serverPanel.add(passwordLabel, gbc_passwordLabel);

		passwordField = new JTextField();
		passwordField.setEnabled(false);
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.weightx = 0.5;
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField.anchor = GridBagConstraints.NORTHWEST;
		gbc_passwordField.gridwidth = 2;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 2;
		serverPanel.add(passwordField, gbc_passwordField);
		passwordField.setColumns(128);

		serverField = new JTextField();
		serverField.setEnabled(false);
		GridBagConstraints gbc_serverField = new GridBagConstraints();
		gbc_serverField.weightx = 0.5;
		gbc_serverField.gridwidth = 2;
		gbc_serverField.anchor = GridBagConstraints.NORTHWEST;
		gbc_serverField.insets = new Insets(0, 0, 5, 5);
		gbc_serverField.fill = GridBagConstraints.HORIZONTAL;
		gbc_serverField.gridx = 1;
		gbc_serverField.gridy = 3;
		serverPanel.add(serverField, gbc_serverField);
		serverField.setColumns(128);

		JPanel localPanel = new JPanel();
		localPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_localPanel = new GridBagConstraints();
		gbc_localPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_localPanel.gridwidth = 4;
		gbc_localPanel.insets = new Insets(0, 10, 10, 10);
		gbc_localPanel.weightx = 0.5;
		gbc_localPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_localPanel.gridx = 0;
		gbc_localPanel.gridy = 1;
		getContentPane().add(localPanel, gbc_localPanel);
		GridBagLayout gbl_containerPanel = new GridBagLayout();
		localPanel.setLayout(gbl_containerPanel);

		pstRadioButton = new JRadioButton("Outlook Pst");
		pstRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
		pstRadioButton.setSelected(true);
		GridBagConstraints gbc_pstRadioButton = new GridBagConstraints();
		gbc_pstRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_pstRadioButton.gridx = 0;
		gbc_pstRadioButton.gridy = 0;
		localPanel.add(pstRadioButton, gbc_pstRadioButton);

		thunderbirdRadioButton = new JRadioButton("ThunderBird");
		thunderbirdRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_thunderbirdRadioButton = new GridBagConstraints();
		gbc_thunderbirdRadioButton.weightx = 0.5;
		gbc_thunderbirdRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_thunderbirdRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_thunderbirdRadioButton.gridx = 1;
		gbc_thunderbirdRadioButton.gridy = 0;
		localPanel.add(thunderbirdRadioButton, gbc_thunderbirdRadioButton);

		nameLabel = new JLabel("Nom de l'extraction");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.anchor = GridBagConstraints.EAST;
		gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 7;
		getContentPane().add(nameLabel, gbc_nameLabel);

		nameField = new JTextField();
		nameField.setText("");
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.gridwidth = 2;
		gbc_nameField.insets = new Insets(0, 0, 5, 5);
		gbc_nameField.anchor = GridBagConstraints.NORTHWEST;
		gbc_nameField.weightx = 0.5;
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 7;
		getContentPane().add(nameField, gbc_nameField);
		nameField.setColumns(128);
		
		mboxRadioButton = new JRadioButton("Mbox");
		mboxRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_mboxRadioButton = new GridBagConstraints();
		gbc_mboxRadioButton.weightx = 0.5;
		gbc_mboxRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_mboxRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_mboxRadioButton.gridx = 2;
		gbc_mboxRadioButton.gridy = 0;
		localPanel.add(mboxRadioButton, gbc_mboxRadioButton);
		
		emlRadioButton = new JRadioButton("Eml");
		emlRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_emlRadioButton = new GridBagConstraints();
		gbc_emlRadioButton.weightx = 0.5;
		gbc_emlRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_emlRadioButton.insets = new Insets(0, 0, 5, 0);
		gbc_emlRadioButton.gridx = 3;
		gbc_emlRadioButton.gridy = 0;
		localPanel.add(emlRadioButton, gbc_emlRadioButton);
		
		msgRadioButton = new JRadioButton("Outlook msg");
		msgRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_msgRadioButton = new GridBagConstraints();
		gbc_msgRadioButton.weightx = 0;
		gbc_msgRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_msgRadioButton.insets = new Insets(0, 0, 5, 0);
		gbc_msgRadioButton.gridx = 4;
		gbc_msgRadioButton.gridy = 0;
		localPanel.add(msgRadioButton, gbc_msgRadioButton);

		containerLabel = new JLabel("Chemin");
		GridBagConstraints gbc_containerLabel = new GridBagConstraints();
		gbc_containerLabel.anchor = GridBagConstraints.EAST;
		gbc_containerLabel.insets = new Insets(0, 0, 0, 5);
		gbc_containerLabel.gridx = 0;
		gbc_containerLabel.gridy = 1;
		localPanel.add(containerLabel, gbc_containerLabel);

		containerField = new JTextField();
		containerField.setText("");
		GridBagConstraints gbc_containerField = new GridBagConstraints();
		gbc_containerField.gridwidth = 3;
		gbc_containerField.insets = new Insets(0, 0, 0, 5);
		gbc_containerField.anchor = GridBagConstraints.NORTHWEST;
		gbc_containerField.weightx = 1.0;
		gbc_containerField.fill = GridBagConstraints.HORIZONTAL;
		gbc_containerField.gridx = 1;
		gbc_containerField.gridy = 1;
		localPanel.add(containerField, gbc_containerField);
		containerField.setColumns(128);

		containerButton = new JButton("Chemin...");
		GridBagConstraints gbc_containerButton = new GridBagConstraints();
		gbc_containerButton.gridx = 4;
		gbc_containerButton.gridy = 1;
		localPanel.add(containerButton, gbc_containerButton);
		containerButton.setActionCommand("container");
		containerButton.addActionListener(app);

		// RadioButtons
		ButtonGroup groupLocalProtocol = new ButtonGroup();
		groupLocalProtocol.add(localRadioButton);
		groupLocalProtocol.add(protocoleRadioButton);

		ButtonGroup groupLocal = new ButtonGroup();
		groupLocal.add(thunderbirdRadioButton);
		groupLocal.add(pstRadioButton);
		groupLocal.add(emlRadioButton);
		groupLocal.add(msgRadioButton);
		groupLocal.add(mboxRadioButton);

		ButtonGroup groupProtocol = new ButtonGroup();
		groupProtocol.add(imapRadioButton);
		groupProtocol.add(imapsRadioButton);

		pack();
	}
}
