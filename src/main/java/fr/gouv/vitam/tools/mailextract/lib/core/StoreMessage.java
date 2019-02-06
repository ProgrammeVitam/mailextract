/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitam.tools.mailextract.lib.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import fr.gouv.vitam.tools.mailextract.lib.formattools.TikaExtractor;
import fr.gouv.vitam.tools.mailextract.lib.formattools.rtf.HTMLFromRTFExtractor;
import fr.gouv.vitam.tools.mailextract.lib.formattools.HTMLTextExtractor;
import fr.gouv.vitam.tools.mailextract.lib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextract.lib.nodes.MetadataPerson;
import fr.gouv.vitam.tools.mailextract.lib.utils.DateRange;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;
import fr.gouv.vitam.tools.mailextract.lib.utils.RawDataSource;

/**
 * Abstract class for store element which is a mail box message.
 * <p>
 * It defines all information (descriptive metadata and objects) to collect from
 * a message and the method to generate directory/files structure from this
 * information. Each subclass has to be able to extract these informations from
 * a message.
 * <p>
 * It is able to generate a mime fake of the message, if not natively Mime.
 * <ul>
 * <p>
 * Metadata information to collect in Vitam guidelines for mail extraction
 * <li>Subject (Title metadata),</li>
 * <li>List of "From" addresses (Writer metadata),</li>
 * <li>List of "To" recipients addresses (Addressee metadata),</li>
 * <li>List of "Cc" and "Bcc" recipients addresses (Recipient metadata),</li>
 * <li>Message unique ID given by the sending server (OriginatingSystmId
 * metadata),</li>
 * <li>Sent date (SentDate metadata),</li>
 * <li>Received date (ReceivedDate metadata),</li>
 * <li>Message unique ID of the message replied to (and in some implementation
 * forwarded) by the current message (OriginatingSystemIdReplyTo metadata),</li>
 * <li>Message body textual content</li> In the descriptive metadata is also
 * added the DescriptionLevel, which is Item for message and for Attachements.
 * <p>
 * Metadata information extracted for study
 * <li>List of "Reply-To" addresses (ReplyTo metadata),</li>
 * <li>List of "Return-Path" addresses, more reliable information given by the
 * first mail relay server (ReturnPath metadata),</li>
 * <li>Complete mail header (MailHeader metadata), contains more metadata which
 * is kept in mime fake generation.</li>
 * <p>
 * Content information extracted
 * <li>All Body Content, text, html and rtf extraction of the message body, when
 * it exists,</li>
 * <li>Attachments, content with filename,</li>
 * </ul>
 * <p>
 * All values can be null, it then express that the metadata is not defined for
 * this message.
 */
public abstract class StoreMessage extends StoreElement {

    /** Store folder. containing this message. **/
    protected StoreFolder storeFolder;

    // /** Message nature (MESSAGE, CALENDAR). */
    // protected int nature;
    //
    // /** The Constant MESSAGE. */
    // static public final int MESSAGE = 0;
    //
    // /** The Constant CALENDAR. */
    // static public final int CALENDAR = 1;
    //
    /**
     * Raw binary content of the message for mime sources, or of the mime fake
     * for others.
     */
    protected byte[] mimeContent;

    /** Mime fake if any, or null for mime source. */
    protected MimeMessage mimeFake;

    /** Different versions of the message body. */
    protected String[] bodyContent = new String[3];

    /** The Constant TEXT_BODY. */
    static public final int TEXT_BODY = 0;

    /** The Constant HTML_BODY. */
    static public final int HTML_BODY = 1;

    /** The Constant RTF_BODY. */
    static public final int RTF_BODY = 2;

    /** The Constant OUT_OF_BODY. */
    static public final int OUT_OF_BODY = 3;

    /** Complete mail header from original smtp format, if any. */
    protected List<String> mailHeader;

    /** Attachments list. */
    protected List<StoreMessageAttachment> attachments;

    /** Appointment information. */
    protected StoreMessageAppointment appointment;

    /** Subject. */
    protected String subject;

    /** "From" address. */
    protected String from;

    /** List of "To" recipients addresses. */
    protected List<String> recipientTo;

    /** List of "Cc"recipients addresses. */
    protected List<String> recipientCc;

    /** List of "Bcc" recipients addresses. */
    protected List<String> recipientBcc;

    /** List of "Reply-To" addresses. */
    protected List<String> replyTo;

    /** "Return-Path" address. */
    protected String returnPath;

    /** Sent date. **/
    protected Date sentDate;

    /** Received date. **/
    protected Date receivedDate;

    /** Message unique ID given by the sending server. */
    protected String messageID;

    /**
     * Message unique ID of the message replied to (and in some implementation
     * forwarded) by the current message.
     */
    protected String inReplyToUID;

    /**
     * List of message unique ID of the message in the same thread of forward
     * and reply.
     */
    protected List<String> references;

    /** List of "Sender" addresses. */
    protected List<String> sender;

    /** Message ArchiveUnit. */
    public ArchiveUnit messageNode;

    /**
     * Instantiates a new mail box message.
     *
     * @param storeFolder
     *            Mail box folder containing this message
     */
    protected StoreMessage(StoreFolder storeFolder) {
        this.storeFolder = storeFolder;
    }

    /**
     * Gets the sent date.
     *
     * <p>
     * Specific metadata getter used for folder date range computation.
     *
     * @return the sent date
     */
    public Date getSentDate() {
        return sentDate;
    }

    /**
     * Gets the message size.
     *
     * <p>
     * Specific information getter used for listing size statistic of folders.
     * Depend on sub class implementation.
     *
     * @return the message size
     */
    public abstract long getMessageSize();

    /**
     * Gets the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the message rfc822 form
     *
     * <p>
     * Either the original content if mime message or a fake mime message
     * generated form, generated during extraction operation.
     * <p>
     * <b>Important:</b> this is computed during message extraction
     *
     * @return the mime content
     */
    public byte[] getMimeContent() {
        return mimeContent;
    }

    /**
     * Gets the logger created during the store extractor construction, and used
     * in all mailextract classes.
     *
     * <p>
     * For convenience each class which may have some log actions has it's own
     * getLogger method always returning the store extractor logger.
     *
     * @return logger
     */
    public Logger getLogger() {
        return storeFolder.getLogger();
    }

    /**
     * Gets the current operation store extractor.
     *
     * @return storeExtractor
     */
    public StoreExtractor getStoreExtractor() {
        return storeFolder.getStoreExtractor();
    }

    /**
     * Log at warning or at finest level depending on store extractor options
     * <p>
     * To log a problem on a specific message.
     *
     * @param msg
     *            Message to log
     */
    public void logMessageWarning(String msg) {
        if (subject != null)
            msg += " for message [" + subject + "]";
        else
            msg += " for [no subject] message";

        if (storeFolder.getStoreExtractor().options.warningMsgProblem)
            getLogger().warning(msg);
        else
            getLogger().finest(msg);
    }

    /*
     * Header analysis methods to be implemented for each StoreMessage
     * implementation
     */

    /**
     * Get and generate the complete message mime header from original format,
     * if any, and all other information useful for analyzing.
     */
    protected abstract void prepareAnalyze();

    /**
     * Analyze message to get Subject metadata.
     */
    protected abstract void analyzeSubject();

    /**
     * Analyze message to get Message-ID metadata.
     */
    protected abstract void analyzeMessageID();

    /**
     * Analyze message to get From metadata.
     */
    protected abstract void analyzeFrom();

    /**
     * Analyze message to get recipients (To, cc and bcc) metadata.
     */
    protected abstract void analyzeRecipients();

    /**
     * Analyze message to get Reply-To metadata.
     */
    protected abstract void analyzeReplyTo();

    /**
     * Analyze message to get Return-Path metadata.
     */
    protected abstract void analyzeReturnPath();

    /**
     * Analyze message to get sent and received dates metadata.
     */
    protected abstract void analyzeDates();

    /**
     * Analyze message to get In-Reply-To metadata.
     */
    protected abstract void analyzeInReplyToId();

    /**
     * Analyze message to get References metadata.
     */
    protected abstract void analyzeReferences();

    /*
     * Content analysis methods to be implemented for each StoreMessage
     * implementation
     */

    /**
     * Analyze message to get the different bodies (text, html, rtf) if any.
     */
    protected abstract void analyzeBodies();

    /**
     * Analyze message to get the attachments, which can be other messages.
     */
    protected abstract void analyzeAttachments();

    // change attachement type to store with the good scheme
    private void setStoreAttachment(StoreMessageAttachment a, String scheme) {
        a.attachmentStoreScheme = scheme;
        a.attachmentType = StoreMessageAttachment.STORE_ATTACHMENT;
    }

    /**
     * Detect embedded store attachments not identified during parsing.
     * <p>
     * It use for this, the list of mimetypes that can be treated by known store
     * extractors. This list is constructed using
     * {@link fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor#addExtractionRelation
     * StoreExtractor.addExtractionRelation}, and a default one is set calling
     * {@link fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor#initDefaultExtractors
     * StoreExtractor.initDefaultExtractors}
     */
    protected void detectStoreAttachments() {
        String mimeType;

        if (attachments != null && !attachments.isEmpty()) {
            for (StoreMessageAttachment a : attachments) {
                if ((a.attachmentType != StoreMessageAttachment.STORE_ATTACHMENT) && (a.attachmentContent != null)
                        && (a.attachmentContent instanceof byte[])) {
                    try {
                        mimeType = TikaExtractor.getInstance().getMimeType(a.getRawAttachmentContent());
                        if (mimeType == null)
                            continue;
                        // if (mimeType.equals("application/vnd.ms-tnef"))
                        // System.out.println("---------------------TNEF
                        // detected");
                        for (String mt : StoreExtractor.mimeTypeSchemeMap.keySet()) {
                            if (mimeType.equals(mt)) {
                                setStoreAttachment(a, StoreExtractor.mimeTypeSchemeMap.get(mt));
                                break;
                            }
                        }
                    } catch (ExtractionException e) {
                        // forget it
                    }
                }
            }
        }
    }

    /*
     * Global message
     */

    /**
     * Gets the native mime content, if any, or null.
     *
     * @return the native mime content
     */
    protected abstract byte[] getNativeMimeContent();

    /**
     * Analyze the appointment information if any in the message, or null.
     */
    protected abstract void analyzeAppointmentInformation();

    /**
     * Analyze message to collect metadata and content information (protocol
     * specific).
     *
     * <p>
     * This is the main method for sub classes, where all metadata and
     * information has to be extracted in standard representation out of the
     * inner representation of the message.
     *
     * If needed a fake raw SMTP content (.eml) is generated with all the body
     * formats available but without the attachments, which are extracted too.
     *
     * @throws ExtractionException
     *             Any unrecoverable extraction exception (access trouble, major
     *             format problems...)
     */
    public void analyzeMessage() throws ExtractionException {
        // header metadata extraction
        // * special global
        analyzeSubject();
        if ((subject == null) || subject.isEmpty())
            subject = "[SubjectVide]";

        // header content extraction
        prepareAnalyze();

        // * messageID
        analyzeMessageID();
        if ((messageID == null) || messageID.isEmpty())
            messageID = "[MessageIDVide]";

        // * recipients and co
        analyzeFrom();
        analyzeRecipients();
        analyzeReplyTo();
        analyzeReturnPath();

        // * sent and received dates
        analyzeDates();

        // * immediate in-reply-to and references
        analyzeInReplyToId();
        analyzeReferences();

        // content extraction
        analyzeBodies();
        optimizeBodies();
        analyzeAttachments();

        // try to get appointment information if any
        analyzeAppointmentInformation();

        // detect embedded store attachments not determine during parsing
        detectStoreAttachments();

        // no raw content, will be constructed at StoreMessage level
        mimeContent = getNativeMimeContent();
    }

    // get rid of useless beginning and ending spaces, carriage returns and
    // desencapsulate html and text from rtf
    private void optimizeBodies() {
        // get rid of useless beginning and ending spaces, carriage returns...
        if (bodyContent[TEXT_BODY] != null)
            bodyContent[TEXT_BODY] = bodyContent[TEXT_BODY].trim();
        if (bodyContent[HTML_BODY] != null)
            bodyContent[HTML_BODY] = bodyContent[HTML_BODY].trim();
        if (bodyContent[RTF_BODY] != null)
            bodyContent[RTF_BODY] = bodyContent[RTF_BODY].trim();

        try {
            // de-encapsulate TEXT and HTML from RTF if defined as encapsulated
            if (((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())) {
                HTMLFromRTFExtractor htmlExtractor = new HTMLFromRTFExtractor(bodyContent[RTF_BODY]);
                if (htmlExtractor.isEncapsulatedTEXTinRTF()) {
                    String result;
                    result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if ((result != null) && !result.isEmpty()) {
                        result = result.trim();
                        if ((bodyContent[TEXT_BODY] == null) || bodyContent[TEXT_BODY].isEmpty()) {
                            bodyContent[TEXT_BODY] = result;
                            bodyContent[RTF_BODY] = null;
                        } else {
                            if (bodyContent[TEXT_BODY].equals(result))
                                bodyContent[RTF_BODY] = null;
                        }
                    }
                } else if (htmlExtractor.isEncapsulatedHTMLinRTF()
                        && ((bodyContent[HTML_BODY] == null) || bodyContent[HTML_BODY].isEmpty())) {
                    String result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if ((result != null) && !result.isEmpty()) {
                        result = result.trim();
                        bodyContent[HTML_BODY] = result;
                        bodyContent[RTF_BODY] = null;
                    }
                }

            }
        } catch (ExtractionException e) {
            // forget bodies optimisation
        }
    }

    // get the String rid of all characters that may cause problems in xml
    // metadata
    private static String purifyMetadataText(String in) {
        String result;

        result = in.replaceAll("[\\p{C}&&[^\\r\\n\\t]]", "");
        // break HTML tags in metadata if any
        result = result.replace("<", "< ");
        result = result.replace("&lt;", "&lt; ");
        return result;
    }

    /**
     * Create the Archive Unit structures with all content and metadata needed,
     * and then write them on disk if writeFlag is true.
     * <p>
     * This is "the" method where the extraction structure and content is mainly
     * defined (see also {@link StoreFolder#extractFolder StoreFolder.extractFolder} and
     * {@link StoreExtractor#extractAllFolders StoreFolder.extractAllFolders}).
     *
     * @param writeFlag
     *            write or not flag (no write used for stats)
     *
     * @throws ExtractionException
     *             Any unrecoverable extraction exception (access trouble, major
     *             format problems...)
     */
    public final void extractMessage(boolean writeFlag) throws ExtractionException {
        // String description = "[Vide]";
        String textContent = null;

        if (messageID.equals("<20CBF48095D47140B58E9F5202ADD9800BD0837C71@KIARA.cab.travail.gouv.fr>"))
            System.out.println("Got it!");

        // create message unit
        if ((subject == null) || subject.trim().isEmpty())
            subject = "[Vide]";

        messageNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.folderArchiveUnit, "Message", subject);

        // metadata in SEDA 2.0-ontology order
        messageNode.addMetadata("DescriptionLevel", "Item", true);
        messageNode.addMetadata("Title", subject, true);
        messageNode.addMetadata("OriginatingSystemId", messageID, false);

        // description = "Message extrait du compte " +
        // mailBoxFolder.storeExtractor.user;
        if (appointment != null) {
            messageNode.addMetadata("Description", "Rendez-vous", true);
        }
        messageNode.addPersonMetadata("Writer", from, false);
        messageNode.addPersonMetadataList("Addressee", recipientTo, false);
        messageNode.addPersonMetadataList("Recipient", recipientCc, false);
        messageNode.addPersonMetadataList("Recipient", recipientBcc, false);
        messageNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), false);
        messageNode.addMetadata("ReceivedDate", DateRange.getISODateString(receivedDate), false);

        // put appointment information in metadata if any
        if (appointment != null) {
            if (appointment.identifier == null)
                appointment.identifier = "[IDVide]";
            if (appointment.location == null)
                appointment.location = "[LocalisationVide]";
            String bdString, edString;
            if (appointment.beginDate != null)
                bdString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.beginDate);
            else
                bdString = "[Date/HeureInconnues]";
            if (appointment.endDate != null)
                edString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.endDate);
            else
                edString = "[Date/HeureInconnues]";

            messageNode.addEventMetadata(appointment.identifier, "RDV Début", bdString,
                    "Localisation : " + appointment.location);
            messageNode.addEventMetadata(appointment.identifier, "RDV Fin", edString,
                    "Localisation : " + appointment.location);
        }

        // reply-to messageID
        if ((inReplyToUID != null) && !inReplyToUID.isEmpty())
            messageNode.addMetadata("OriginatingSystemIdReplyTo", inReplyToUID, false);

        // get textContent if TEXT_CONTENT not empty
        if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty())
            textContent = bodyContent[TEXT_BODY];

        // get text content from html if no textContent
        if ((textContent == null) && (bodyContent[HTML_BODY] != null))
            textContent = HTMLTextExtractor.getInstance().act(bodyContent[HTML_BODY]);

        // purify textContent and put in metadata
        if ((textContent != null) && (!textContent.trim().isEmpty())) {
            if (getStoreExtractor().options.extractMessageTextFile)
                messageNode.addObject(HTMLTextExtractor.getInstance().htmlStringtoString(textContent), messageID + ".txt",
                        "TextContent", 1);
            if (getStoreExtractor().options.extractMessageTextMetadata) {
                // // break HTML tags in metadata if any
                // textContent = textContent.replace("<", "< ");
                // textContent = textContent.replace("&lt;", "&lt; ");
                messageNode.addMetadata("TextContent", purifyMetadataText(textContent), true);
            }
        }

        // extract all attachment and generate mimecontent of theese attachments
        // if needed
        if (attachments != null && !attachments.isEmpty()) {
            // create all attachments subunits/object groups
            extractMessageAttachments(messageNode, writeFlag);
        }

        // generate mime fake if needed and associated mimeContent
        if (mimeContent == null) {
            mimeFake = getMimeFake();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mimeFake.writeTo(baos);
                mimeContent = baos.toByteArray();
            } catch (Exception e) {
                logMessageWarning("mailextract: Can't extract raw content");
            }
        }
        if (mimeContent == null)
            mimeContent = "".getBytes();

        // add object binary master except if empty one
        if (!isEmptyBodies() || (attachments != null))
            messageNode.addObject(mimeContent, messageID + ".eml", "BinaryMaster", 1);

        if (writeFlag)
            messageNode.write();

        getLogger().finer("mailextract: Extracted message " + (subject == null ? "no subject" : subject));
        getLogger().finest("with SentDate=" + (sentDate == null ? "Unknown sent date" : sentDate.toString()));

        // write in csv list
        if (storeFolder.getStoreExtractor().options.extractList)
            writeExtractList();
    }

    private void writeExtractList() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|",
                (sentDate == null ? "" : sdf.format(sentDate)));
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|",
                (receivedDate == null ? "" : sdf.format(receivedDate)));
        if ((from != null) && !from.isEmpty()) {
            MetadataPerson p = new MetadataPerson(from);
            storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|\"%s\"|", filterHyphen(p.birthName),
                    filterHyphen(p.identifier));
        }
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|",
                filterHyphen(personStringListToIndentifierString(recipientTo)));
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|", filterHyphen(subject));
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|", filterHyphen(messageID));
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|", filterHyphen(attachmentsNamesList()));
        if (replyTo == null)
            storeFolder.getStoreExtractor().getPSExtractList().format("\"\"|");
        else {
            MetadataPerson p = new MetadataPerson(replyTo.get(0));
            storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|", filterHyphen(p.identifier));
        }
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%s\"|", filterHyphen(storeFolder.getFullName()));
        storeFolder.getStoreExtractor().getPSExtractList().format("\"%d\"|", this.getMessageSize());
        if (!storeFolder.getStoreExtractor().isRoot())
            storeFolder.getStoreExtractor().getPSExtractList().format("\"Attached\"");
        if (appointment != null) {
            String bdString, edString;
            if (appointment.beginDate != null)
                bdString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.beginDate);
            else
                bdString = "[Date/HeureInconnues]";
            if (appointment.endDate != null)
                edString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.endDate);
            else
                edString = "[Date/HeureInconnues]";
            storeFolder.getStoreExtractor().getPSExtractList().format("|\"%s\"|\"%s\"|\"%s\"",
                    filterHyphen(appointment.location), bdString, edString);
        } else {
            storeFolder.getStoreExtractor().getPSExtractList().format("|||", this.getMessageSize());
        }
        storeFolder.getStoreExtractor().getPSExtractList().println("");
        storeFolder.getStoreExtractor().getPSExtractList().flush();
    }

    private String personStringListToIndentifierString(List<String> sList) {
        String result = "";
        MetadataPerson p;
        boolean first = true;

        if (sList != null) {
            for (String s : sList) {
                if (first)
                    first = false;
                else
                    result += ", ";
                p = new MetadataPerson(s);
                result += p.identifier;
            }
        }
        return result;
    }

    private String attachmentsNamesList() {
        String result = "";
        boolean first = true;

        if (attachments != null) {
            for (StoreMessageAttachment a : attachments) {
                if (first)
                    first = false;
                else
                    result += ", ";
                result += a.getName();
            }
        }
        return result;
    }

    private String filterHyphen(String s) {
        return s.replace("\"", " ");
    }

    /** Extract a file or inline message attachment. */
    private final void extractFileOrInlineAttachment(ArchiveUnit messageNode, StoreMessageAttachment attachment,
                                                     boolean writeFlag) throws ExtractionException {
        ArchiveUnit attachmentNode;

        if ((attachment.name == null) || attachment.name.isEmpty())
            attachment.name = "[Vide]";
        attachmentNode = new ArchiveUnit(storeFolder.storeExtractor, messageNode, "Attachment", attachment.name);
        attachmentNode.addMetadata("DescriptionLevel", "Item", true);
        attachmentNode.addMetadata("Title", attachment.name, true);
        attachmentNode.addMetadata("Description", "Document \"" + attachment.name + "\" joint au message " + messageID,
                true);

        // get the max of creation and modification date which define the
        // creation date of the present file
        // (max for correcting a current confusion between theese two dates)
        Date date = null;
        if (attachment.creationDate != null) {
            if (attachment.modificationDate != null)
                date = (attachment.creationDate.compareTo(attachment.modificationDate) > 0 ? attachment.creationDate
                        : attachment.modificationDate);
            else
                date = attachment.creationDate;
        } else if (attachment.modificationDate != null)
            date = attachment.modificationDate;
        if (date != null)
            attachmentNode.addMetadata("CreatedDate", DateRange.getISODateString(attachment.creationDate), true);

        // Raw object extraction
        attachmentNode.addObject(attachment.getRawAttachmentContent(), attachment.name, "BinaryMaster", 1);

        // Text object extraction
        String textExtract = null;
        if (getStoreExtractor().options.extractFileTextFile || getStoreExtractor().options.extractFileTextMetadata)
            try {
                textExtract = TikaExtractor.getInstance().extractTextFromBinary(attachment.getRawAttachmentContent());
            } catch (ExtractionException ee) {
                this.getLogger().severe("mailextract: Can't extract text content from attachment " + attachment.name);
            }
        // put in file
        if (getStoreExtractor().options.extractFileTextFile && (!((textExtract == null) || textExtract.trim().isEmpty()))) {
            attachmentNode.addObject(textExtract.getBytes(), attachment.name + ".txt", "TextContent", 1);
        }
        // put in metadata
        if (getStoreExtractor().options.extractFileTextMetadata
                && (!((textExtract == null) || textExtract.isEmpty()))) {
            attachmentNode.addMetadata("TextContent", purifyMetadataText(textExtract), true);
        }

        if (writeFlag)
            attachmentNode.write();
    }

    /** Extract a store attachment */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private final void extractStoreAttachment(ArchiveUnit rootNode, DateRange attachedMessagedateRange,
                                              StoreMessageAttachment a, boolean writeFlag) throws ExtractionException {
        StoreExtractor extractor;
        Boolean isContainerScheme = false;

        Class storeExtractorClass = StoreExtractor.schemeStoreExtractorClassMap.get(a.attachmentStoreScheme);
        if (storeExtractorClass == null) {
            logMessageWarning("mailextract: Unknown embedded store type=" + a.attachmentStoreScheme
                    + " , extracting unit in path " + rootNode.getFullName());
            extractor = null;
        } else {
            isContainerScheme = StoreExtractor.schemeContainerMap.get(a.attachmentStoreScheme);
            if (isContainerScheme) {
                rootNode = new ArchiveUnit(getStoreExtractor(), rootNode, "Container",
                        (a.name == null ? "Infile" : a.name));
                rootNode.addMetadata("DescriptionLevel", "Item", true);
                rootNode.addMetadata("Title",
                        "Conteneur " + a.attachmentStoreScheme + (a.name == null ? "" : " " + a.name), true);
                rootNode.addMetadata("Description",
                        "Extraction d'un conteneur " + a.attachmentStoreScheme + (a.name == null ? "" : " " + a.name),
                        true);
            }
            try {
                extractor = (StoreExtractor) storeExtractorClass
                        .getConstructor(StoreMessageAttachment.class, ArchiveUnit.class, StoreExtractorOptions.class,
                                StoreExtractor.class, Logger.class, PrintStream.class)
                        .newInstance(a, rootNode, getStoreExtractor().options, getStoreExtractor(), getLogger(),
                                getStoreExtractor().getPSExtractList());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
                    | SecurityException e) {
                logMessageWarning("mailextract: Dysfonctional embedded store type=" + a.attachmentStoreScheme
                        + " , extracting unit in path " + rootNode.getFullName());
                extractor = null;
            } catch (InvocationTargetException e) {
                Throwable te = e.getCause();
                if (te instanceof ExtractionException)
                    throw (ExtractionException) te;
                logMessageWarning("mailextract: Dysfonctional embedded store type=" + a.attachmentStoreScheme
                        + " , extracting unit in path " + rootNode.getFullName());
                extractor = null;
            }
        }
        if (extractor != null) {
            extractor.writeTargetLog();
            extractor.getRootFolder().extractFolderAsRoot(writeFlag);
            getStoreExtractor().addTotalAttachedMessagesCount(
                    extractor.getTotalElementsCount() + extractor.getTotalAttachedMessagesCount());
            attachedMessagedateRange.extendRange(extractor.getRootFolder().getDateRange());
            extractor.endStoreExtractor();
            if (extractor.getRootFolder().dateRange.isDefined() && isContainerScheme) {
                rootNode.addMetadata("StartDate",
                        DateRange.getISODateString(extractor.getRootFolder().dateRange.getStart()), true);
                rootNode.addMetadata("EndDate",
                        DateRange.getISODateString(extractor.getRootFolder().dateRange.getEnd()), true);
            }
            if (writeFlag)
                rootNode.write();
        }
    }

    /** Extract all message attachments. */
    private final void extractMessageAttachments(ArchiveUnit messageNode, boolean writeFlag)
            throws ExtractionException {
        DateRange attachedMessagedateRange;
        boolean attachedFlag = false;

        attachedMessagedateRange = new DateRange();

        for (StoreMessageAttachment a : attachments) {
            // message identification
            if (a.attachmentType == StoreMessageAttachment.STORE_ATTACHMENT) {
                // recursive extraction of a message in attachment...
                logMessageWarning("mailextract: Attached message extraction");
                extractStoreAttachment(messageNode, attachedMessagedateRange, a, writeFlag);
                attachedFlag = true;
            } else if (writeFlag) {
                // standard attachment file
                extractFileOrInlineAttachment(messageNode, a, writeFlag);
                if (a.creationDate != null)
                    attachedMessagedateRange.extendRange(a.creationDate);
                if (a.modificationDate != null)
                    attachedMessagedateRange.extendRange(a.modificationDate);
                attachedFlag = true;
            }
        }
        if (attachedFlag && writeFlag) {
            if (attachedMessagedateRange.isDefined()) {
//				messageNode.addMetadata("StartDate", DateRange.getISODateString(attachedMessagedateRange.getStart()),
//						true);
//				messageNode.addMetadata("EndDate", DateRange.getISODateString(attachedMessagedateRange.getEnd()), true);
            }
        }
    }

    /**
     * Add this message in the folder accumulators for number of messages and
     * total raw size of messages.
     *
     * @throws ExtractionException
     *             Any unrecoverable extraction exception (access trouble, major
     *             format problems...)
     */
    public void countMessage() throws ExtractionException {
        // accumulate in folder statistics
        storeFolder.incFolderElementsCount();
        storeFolder.addFolderElementsRawSize(getMessageSize());
    }

    /**
     * Gets the mime fake.
     *
     * @return the mime fake
     */
    public MimeMessage getMimeFake() {
        MimeMessage mime = new FixIDMimeMessage(Session.getDefaultInstance(new Properties()));
        try {
            buildMimeHeader(mime);
            buildMimePart(mime);
            mime.saveChanges();
        } catch (MessagingException e) {
            logMessageWarning("mailextract: Unable to generate mime fake ");
            mime = null;
        } catch (ExtractionException e) {
            logMessageWarning("mailextract: " + e.getMessage());
            mime = null;
        }
        return mime;
    }

    private static void setAddressList(MimeMessage mime, String tag, List<String> addressList)
            throws MessagingException, UnsupportedEncodingException {
        if ((addressList != null) && (!addressList.isEmpty())) {
            String value = "";
            int countline = 0;
            for (String tmp : addressList) {
                // 80 characters lines
                tmp = MimeUtility.encodeText(tmp);
                if (countline + tmp.length() > 80) {
                    value += "\n\t";
                    countline = 1;
                } else
                    countline += tmp.length();
                value += MimeUtility.encodeText(tmp) + ",";
            }
            value = value.substring(0, value.length() - 1);
            mime.setHeader(tag, value);
        }
    }

    private void buildMimeHeader(MimeMessage mime) throws ExtractionException {
        try {
            // put all know headers, they will be change by the specific ones
            if ((mailHeader != null) && (mailHeader.size() > 0)) {
                String tag, value;
                for (String tmp : mailHeader) {
                    if (tmp.indexOf(':') < 0)
                        continue;
                    tag = tmp.substring(0, tmp.indexOf(':'));
                    value = tmp.substring(tmp.indexOf(':') + 1);
                    mime.setHeader(tag, value);
                }
            }

            // Return-Path
            if (returnPath != null)
                mime.setHeader("Return-Path", MimeUtility.encodeText(returnPath));
            // From
            if (from != null)
                mime.setHeader("From", MimeUtility.encodeText(from));
            // To
            if (recipientTo != null)
                setAddressList(mime, "To", recipientTo);
            // cc
            if (recipientCc != null)
                setAddressList(mime, "cc", recipientCc);
            // bcc
            if (recipientBcc != null)
                setAddressList(mime, "bcc", recipientBcc);
            // Reply-To
            if (replyTo != null)
                setAddressList(mime, "Reply-To", replyTo);
            // Date
            if (sentDate != null)
                mime.setSentDate(sentDate);
            // Subject
            if (subject != null)
                mime.setSubject(MimeUtility.encodeText(subject));
            // Message-ID
            if (messageID != null)
                mime.setHeader("Message-ID", MimeUtility.encodeText(messageID));
            // In-Reply-To
            if ((inReplyToUID != null) && (!inReplyToUID.isEmpty()))
                mime.setHeader("In-Reply-To", MimeUtility.encodeText(inReplyToUID));

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ExtractionException("Unable to generate mime header of message " + subject);
        }
    }

    private void addAttachmentPart(MimeMultipart root, boolean isInline) throws ExtractionException {
        try {
            // build attach part
            for (StoreMessageAttachment a : attachments) {
                boolean thisIsInline = (a.attachmentType == StoreMessageAttachment.INLINE_ATTACHMENT);

                if ((thisIsInline && isInline) || ((!thisIsInline) && (!isInline))) {
                    MimeBodyPart attachPart = new MimeBodyPart();

                    // set Content-ID
                    String cidName = null;
                    if ((a.contentID != null) && !a.contentID.trim().isEmpty()) {
                        attachPart.setContentID("<" + a.contentID.trim() + ">");
                        if (a.contentID.indexOf('@') < 0)
                            cidName = a.contentID;
                        else
                            cidName = a.contentID.substring(0, a.contentID.indexOf('@'));
                    } else
                        cidName = "unknown";

                    // set object and Content-Type
                    String attachmentName = encodedFilename(a.name, cidName);
                    if ((a.mimeType == null) || (a.mimeType.isEmpty()))
                        attachPart.setContent(a.getRawAttachmentContent(),
                                "application/octet-stream; name=\"" + attachmentName + "\"");
                    else {
                        if (a.mimeType.startsWith("text")) {
                            String s;
                            s = new String(a.getRawAttachmentContent(), "UTF-8");
                            attachPart.setContent(s, a.mimeType + "; name=\"" + attachmentName + "\"");
                        } else if (a.mimeType.startsWith("message")) {
                            // bypass datahandler as the rfc822 form is provided
                            RawDataSource rds = new RawDataSource(a.getRawAttachmentContent(), a.mimeType,
                                    attachmentName);
                            DataHandler dh = new DataHandler(rds);
                            attachPart.setDataHandler(dh);
                        } else {
                            attachPart.setContent(a.getRawAttachmentContent(),
                                    a.mimeType + "; name=\"" + attachmentName + "\"");
                        }
                    }
                    // set Content-Disposition
                    if (a.attachmentType == StoreMessageAttachment.INLINE_ATTACHMENT)
                        attachPart.setDisposition("inline; filename=\"" + attachmentName + "\"");
                    else
                        attachPart.setDisposition("attachment; filename=\"" + attachmentName + "\"");
                    root.addBodyPart(attachPart);
                }
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new ExtractionException(
                    "Unable to generate " + (isInline ? "inlines" : "attachments") + " of message " + subject);
        }

    }

    private MimeMultipart newChild(MimeMultipart parent, String type) throws MessagingException {
        MimeMultipart child = new MimeMultipart(type);
        final MimeBodyPart mbp = new MimeBodyPart();
        parent.addBodyPart(mbp);
        mbp.setContent(child);
        return child;
    }

    // some extraction has no body only headers
    private boolean isEmptyBodies() {
        if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty())
            return false;
        if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty())
            return false;
        if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())
            return false;
        // if (attachments.size() > 0)
        // return false;
        return true;
    }

    private void buildMimePart(MimeMessage mime) throws ExtractionException {
        boolean hasInline = false;
        int relatedPart = OUT_OF_BODY;

        MimeMultipart rootMp = new MimeMultipart("mixed");
        {
            try {
                // search if there are inlines
                for (StoreMessageAttachment a : attachments) {
                    if (a.attachmentType == StoreMessageAttachment.INLINE_ATTACHMENT) {
                        hasInline = true;
                        break;
                    }
                }

                // // de-encapulate HTML from RTF if needed
                // if (((bodyContent[RTF_BODY] != null) &&
                // !bodyContent[RTF_BODY].isEmpty())) {
                // HTMLFromRTFExtractor htmlExtractor = new
                // HTMLFromRTFExtractor(bodyContent[RTF_BODY]);
                // if (htmlExtractor.isEncapsulatedTEXTinRTF()) {
                // String result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                // if ((result != null) && !result.isEmpty()) {
                // if ((bodyContent[TEXT_BODY] == null) ||
                // bodyContent[TEXT_BODY].isEmpty()) {
                // bodyContent[TEXT_BODY] = result;
                // bodyContent[RTF_BODY] = null;
                // } else {
                // result = result.trim();
                // if (bodyContent[TEXT_BODY].equals(result))
                // bodyContent[RTF_BODY] = null;
                // }
                // }
                // } else if (htmlExtractor.isEncapsulatedHTMLinRTF()
                // && ((bodyContent[HTML_BODY] == null) ||
                // bodyContent[HTML_BODY].isEmpty())) {
                // String result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                // if ((result != null) && !result.isEmpty()) {
                // bodyContent[HTML_BODY] = result;
                // bodyContent[RTF_BODY] = null;
                // }
                // }
                //
                // }

                // determine in which part to add related
                if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())
                    relatedPart = RTF_BODY;
                else if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty())
                    relatedPart = HTML_BODY;

                // build message part
                MimeMultipart msgMp = newChild(rootMp, "alternative");
                {
                    if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[TEXT_BODY], "text/plain; charset=utf-8");
                        msgMp.addBodyPart(part);
                    }
                    // if empty message, construct a fake empty text part
                    if (isEmptyBodies()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent("", "text/plain; charset=utf-8");
                        msgMp.addBodyPart(part);
                    }

                    if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty()) {
                        MimeMultipart upperpart;
                        if (hasInline && (relatedPart == HTML_BODY)) {
                            upperpart = newChild(msgMp, "related");
                        } else
                            upperpart = msgMp;

                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[HTML_BODY], "text/html; charset=utf-8");
                        upperpart.addBodyPart(part);

                        if (hasInline && (relatedPart == HTML_BODY))
                            addAttachmentPart(upperpart, true);
                    }
                    if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty()) {
                        MimeMultipart upperpart;
                        if (hasInline && (relatedPart == RTF_BODY)) {
                            upperpart = newChild(msgMp, "related");
                        } else
                            upperpart = msgMp;

                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[RTF_BODY], "text/rtf; charset=US-ASCII");// ;
                        // charset=utf-8");
                        upperpart.addBodyPart(part);

                        if (hasInline && (relatedPart == RTF_BODY))
                            addAttachmentPart(upperpart, true);

                    }
                }
            } catch (MessagingException e) {
                throw new ExtractionException("Unable to generate mime body part of message " + subject);
            }

            // add inline part of attachments if not added to HTML body
            if (relatedPart == OUT_OF_BODY)
                addAttachmentPart(rootMp, true);
            addAttachmentPart(rootMp, false);

            try {
                mime.setContent(rootMp);
            } catch (MessagingException e) {
                throw new ExtractionException("Unable to generate mime fake of message " + subject);
            }
        }
    }

    private String encodedFilename(String filename, String ifnone) {
        String tmp;
        if ((filename != null) && !filename.trim().isEmpty())
            tmp = filename;
        else
            tmp = ifnone;
        try {
            return MimeUtility.encodeWord(tmp, "utf-8", "B");
        } catch (UnsupportedEncodingException e) {
            // forget it
        }
        return "Unknown";
    }

    /**
     * Prevent update Message-ID
     *
     * @author inter6
     *
     */
    private class FixIDMimeMessage extends MimeMessage {

        public FixIDMimeMessage(Session session) {
            super(session);
        }

        @Override
        protected void updateMessageID() throws MessagingException {
            String[] ids = getHeader("Message-ID");
            if (ids == null || ids.length == 0 || ids[0] == null || ids[0].isEmpty()) {
                super.updateMessageID();
            }
        }

        ;

    }

}
