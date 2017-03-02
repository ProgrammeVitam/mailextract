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

public class MailExtractMainWindow extends JFrame {

	private static final long serialVersionUID = 4607177374736676766L;

	private MailExtractApp app;

	JTextField folderField;
	JTextField savedirField;

	JRadioButton localRadioButton;
	JRadioButton thunderbirdRadioButton;
	JRadioButton outlookRadioButton;
	JLabel nameLabel;
	JTextField nameField;
	JLabel containerLabel;
	JTextField containerField;
	JButton containerButton;
	
	JRadioButton protocoleRadioButton;
	JRadioButton imapRadioButton;
	JRadioButton imapsRadioButton;
	JLabel serverLabel;
	JTextField serverField;
	JLabel userLabel;
	JTextField userField;
	JLabel passwordLabel;
	JTextField passwordField;	
	
	JComboBox loglevelComboBox;
	
	JCheckBox warningCheckBox;
	JCheckBox keeponlydeepCheckBox;
	JCheckBox dropemptyfoldersCheckBox;
	JCheckBox nameshortenedCheckBox;
	JTextArea consoleTextArea;
	private JScrollPane scrollPane;
		
	public MailExtractApp getApp() {
		return app;
	}

	/**
	 * Create the application.
	 */
	public MailExtractMainWindow(MailExtractApp app) {
		super();
		this.app = app;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		java.net.URL imageURL = getClass().getClassLoader().getResource("images/Logo48.png");
		if (imageURL != null) {
			ImageIcon icon = new ImageIcon(imageURL);
			setIconImage(icon.getImage());
		}
		this.setTitle("MailExtract");

		getContentPane().setPreferredSize(new Dimension(800, 600));
		setBounds(0, 0, 800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 20, 20, 0, 0};
		getContentPane().setLayout(gridBagLayout);
		
		String[] loglevelStrings={"OFF","SEVERE","WARNING","INFO","FINE","FINER","FINEST"};
		
		consoleTextArea = new JTextArea();
		consoleTextArea.setLineWrap(true);
		
		scrollPane = new JScrollPane(consoleTextArea);
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 11;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		nameshortenedCheckBox = new JCheckBox("Noms courts");
		GridBagConstraints gbc_nameshortenedCheckBox = new GridBagConstraints();
		gbc_nameshortenedCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_nameshortenedCheckBox.gridx = 3;
		gbc_nameshortenedCheckBox.gridy = 5;
		getContentPane().add(nameshortenedCheckBox, gbc_nameshortenedCheckBox);
		
		JButton extractButton = new JButton("Extraire");
		GridBagConstraints gbc_extractButton = new GridBagConstraints();
		gbc_extractButton.gridwidth = 2;
		gbc_extractButton.insets = new Insets(0, 0, 10, 10);
		gbc_extractButton.gridx = 0;
		gbc_extractButton.gridy = 10;
		getContentPane().add(extractButton, gbc_extractButton);
		extractButton.setActionCommand("extract");
		extractButton.addActionListener(app);
		
		JButton listButton = new JButton("Liste dossiers");
		GridBagConstraints gbc_listButton = new GridBagConstraints();
		gbc_listButton.anchor = GridBagConstraints.EAST;
		gbc_listButton.insets = new Insets(0, 0, 10, 10);
		gbc_listButton.gridx = 2;
		gbc_listButton.gridy = 10;
		getContentPane().add(listButton, gbc_listButton);
		listButton.setActionCommand("list");
		listButton.addActionListener(app);
		
		JButton statButton = new JButton("Stats dossiers");
		GridBagConstraints gbc_statButton = new GridBagConstraints();
		gbc_statButton.insets = new Insets(0, 0, 10, 10);
		gbc_statButton.gridx = 3;
		gbc_statButton.gridy = 10;
		getContentPane().add(statButton, gbc_statButton);
		statButton.setActionCommand("stat");
		statButton.addActionListener(app);
		
		warningCheckBox = new JCheckBox("Remonte les pbs sur les messages");
		GridBagConstraints gbc_warningCheckBox = new GridBagConstraints();
		gbc_warningCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_warningCheckBox.gridx = 2;
		gbc_warningCheckBox.gridy = 8;
		getContentPane().add(warningCheckBox, gbc_warningCheckBox);
		
		loglevelComboBox = new JComboBox(loglevelStrings);
		loglevelComboBox.setEditable(true);
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
		
		keeponlydeepCheckBox = new JCheckBox("Garde les vides infra-racine");
		GridBagConstraints gbc_keeponlydeepRadioButton = new GridBagConstraints();
		gbc_keeponlydeepRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_keeponlydeepRadioButton.gridx = 2;
		gbc_keeponlydeepRadioButton.gridy = 5;
		getContentPane().add(keeponlydeepCheckBox, gbc_keeponlydeepRadioButton);
		
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
		
		dropemptyfoldersCheckBox = new JCheckBox("Eliminer les dossiers vides");
		GridBagConstraints gbc_dropemptyfoldersCheckBox = new GridBagConstraints();
		gbc_dropemptyfoldersCheckBox.gridwidth = 2;
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
		gbc_localPanel.gridwidth = 4;
		gbc_localPanel.insets = new Insets(0, 10, 10, 10);
		gbc_localPanel.weightx = 0.5;
		gbc_localPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_localPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_localPanel.gridx = 0;
		gbc_localPanel.gridy = 1;
		getContentPane().add(localPanel, gbc_localPanel);
		GridBagLayout gbl_containerPanel = new GridBagLayout();
		localPanel.setLayout(gbl_containerPanel);
				
		thunderbirdRadioButton = new JRadioButton("ThunderBird");
		thunderbirdRadioButton.setSelected(true);
		GridBagConstraints gbc_thunderbirdRadioButton = new GridBagConstraints();
		gbc_thunderbirdRadioButton.gridwidth = 2;
		gbc_thunderbirdRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_thunderbirdRadioButton.gridx = 0;
		gbc_thunderbirdRadioButton.gridy = 0;
		localPanel.add(thunderbirdRadioButton, gbc_thunderbirdRadioButton);
		
		outlookRadioButton = new JRadioButton("Outlook");
		GridBagConstraints gbc_outlookRadioButton = new GridBagConstraints();
		gbc_outlookRadioButton.weightx = 0.5;
		gbc_outlookRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_outlookRadioButton.gridx = 2;
		gbc_outlookRadioButton.gridy = 0;
		localPanel.add(outlookRadioButton, gbc_outlookRadioButton);
		
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
		gbc_containerField.gridwidth = 2;
		gbc_containerField.insets = new Insets(0, 0, 0, 5);
		gbc_containerField.anchor = GridBagConstraints.NORTHWEST;
		gbc_containerField.weightx = 0.5;
		gbc_containerField.fill = GridBagConstraints.HORIZONTAL;
		gbc_containerField.gridx = 1;
		gbc_containerField.gridy = 1;
		localPanel.add(containerField, gbc_containerField);
		containerField.setColumns(128);
		
		containerButton = new JButton("Chemin...");
		GridBagConstraints gbc_containerButton = new GridBagConstraints();
		gbc_containerButton.insets = new Insets(0, 0, 0, 5);
		gbc_containerButton.gridx = 3;
		gbc_containerButton.gridy = 1;
		localPanel.add(containerButton, gbc_containerButton);
		containerButton.setActionCommand("container");
		containerButton.addActionListener(app);
		
		//RadioButtons
		ButtonGroup groupLocalProtocol = new ButtonGroup();
		groupLocalProtocol.add(localRadioButton);
		groupLocalProtocol.add(protocoleRadioButton);

		ButtonGroup groupLocal = new ButtonGroup();
		groupLocal.add(thunderbirdRadioButton);
		groupLocal.add(outlookRadioButton);

		ButtonGroup groupProtocol = new ButtonGroup();
		groupProtocol.add(imapRadioButton);
		groupProtocol.add(imapsRadioButton);
		
		pack();
	}

	void setRecordGrpOnly(boolean recordGrpOnly) {
	}

	void setFileName(boolean fileName) {
	}

	
	void reload() {
		resetPanes();
	}

	void resetPanes() {
	}
}
