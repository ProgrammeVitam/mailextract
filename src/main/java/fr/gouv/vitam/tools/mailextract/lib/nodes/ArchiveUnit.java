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

package fr.gouv.vitam.tools.mailextract.lib.nodes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.gouv.vitam.tools.mailextract.lib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextract.lib.utils.ExtractionException;

/**
 * Class for SEDA Archive Unit managing metadata, objects, if any, and on disk
 * representation.
 * 
 * <p>
 * Other classes create Archive Units on the fly with extracted information and
 * content. This class write on disk representation with convenient
 * directory/file structure and ArchiveUnitContent.xml files for metadata.
 * <p>
 * All the files, if not pure binary, are UTF-8 encoded, as the file names.
 */
public class ArchiveUnit {
	private StoreExtractor storeExtractor;
	private String rootPath;
	private String name;
	private boolean forceMessageUnit;
	private MetadataXMLList contentmetadatalist = new MetadataXMLList();
	private List<ArchiveObject> objects = new ArrayList<ArchiveObject>();

	// Utility class containing one Object of the ObjectGroup
	private class ArchiveObject {
		String filename;
		String usage;
		int version;
		byte[] rawContent;

		ArchiveObject(byte[] rawContent, String filename, String usage, int version) {
			this.rawContent = rawContent;
			this.filename = filename;
			this.usage = usage;
			this.version = version;
		}
	}

	/**
	 * Instantiates a new archive unit.
	 * <p>
	 * This constructor is used for root Archive Unit (no father).
	 *
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param rootPath
	 *            Directory in which the directory/file structure is created
	 * @param name
	 *            Name of the root Archive Unit
	 */
	public ArchiveUnit(StoreExtractor storeExtractor, String rootPath, String name) {
		this.storeExtractor = storeExtractor;
		this.rootPath = rootPath;
		this.name = name;
		this.forceMessageUnit = false;
	}

	/**
	 * Instantiates a new archive unit.
	 * <p>
	 * Name is reduced as defined by options, and Archive Unit directory name is generated as
	 * "'unitType'#'UniqID':'name'
	 * 
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param father
	 *            Father Archive Unit
	 * @param unitType
	 *            Unit type (Folder| Message| Attachment...)
	 * @param name
	 *            Name of the Archive Unit
	 */
	public ArchiveUnit(StoreExtractor storeExtractor, ArchiveUnit father, String unitType, String name) {
		this.storeExtractor = storeExtractor;
		if (unitType == null)
			this.name = name;
		else {
			this.name = this.normalizeUniqUnitname(unitType, name);
			if (unitType.equals("Message"))
				forceMessageUnit = true;
		}
		this.rootPath = father.getFullName();
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
		return storeExtractor.getLogger();
	}

	/**
	 * Gets the full name.
	 *
	 * @return Full path Archive Unit directory name
	 */
	public String getFullName() {
		return rootPath + File.separator + getName();
	}

	/**
	 * Gets the root path.
	 *
	 * @return Root path Archive Unit directory name
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * Force the ArchiveUnit as an ArchiveUnit with objects.
	 *
	 * @param b
	 *            the new object unit
	 */
	public void setObjectUnit(boolean b) {
		forceMessageUnit = true;
	}

	/**
	 * Gets the name.
	 *
	 * @return Name of Archive Unit in directory name
	 */
	public String getName() {
		if (!forceMessageUnit && (objects.isEmpty()))
			return name;
		else
			return "__" + name + "__";
	}

	/**
	 * Adds a simple (key, value) metadata.
	 * <p>
	 * If value is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and value is empty, the metadata is set to "".
	 * If mandatory flag is true and value is null, the fact is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Metadata String value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addMetadata(String key, String value, boolean mandatory) {
		if (value != null && !value.isEmpty())
			contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, value));
		else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds a simple (key, value) metadata with an attribute.
	 * <p>
	 * If value is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and value is empty, the metadata is set to "".
	 * If mandatory flag is true and value is null, the fact is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param attributename
	 *            Metadata attribute name
	 * @param attributevalue
	 *            Metadata attribute value
	 * @param value
	 *            Metadata String value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addMetadata(String key, String attributename, String attributevalue, String value, boolean mandatory) {
		if (value != null && !value.isEmpty())
			contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, attributename, attributevalue, value));
		else if (mandatory) {
			getLogger().finest("mailextract: mandatory metadata '" + key + "' is not defined in unit '" + name
					+ "' in folder '" + rootPath + "'");
		}
	}

	/**
	 * Adds a simple (key, value) metadata with a default value if value is null
	 * or empty.
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Metadata value
	 * @param defaultValue
	 *            Default metadata value
	 */
	public void addDefaultValuedMetadata(String key, String value, String defaultValue) {
		if ((value != null) && !value.isEmpty())
			contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, value));
		else
			contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, defaultValue));
	}

	/**
	 * Adds a list of (key, value) metadata for the same key .
	 *
	 * <p>
	 * If list of values is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and list of values is null or empty, the
	 * metadata lack is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param valuesList
	 *            Metadata list of values
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addSameMetadataList(String key, List<String> valuesList, boolean mandatory) {
		if ((valuesList != null) && (!valuesList.isEmpty())) {
			for (String s : valuesList) {
				contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, s));
			}
		} else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds for the key metadata a person value
	 * <p>
	 * This is a utility method, used first for the writer. For example the due
	 * structure for "TOTO<toto@sample.fr>" Writer metadata in XML is:
	 * <p>
	 * <Writer> <FirstName></FirstName> <BirthName>TOTO</BirthName>
	 * <Identifier>toto@sample.fr</Identifier></Writer>
	 * <p>
	 * If mandatory flag is true and value is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Person value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addPersonMetadata(String key, String value, boolean mandatory) {
		MetadataPerson p;
		MetadataXMLNode mvMetaData;
		MetadataXMLList mlMetaData;

		if ((value != null) && !value.isEmpty()) {
			p = new MetadataPerson(value);
			mlMetaData = new MetadataXMLList();
			mvMetaData = new MetadataXMLNode("FirstName", p.firstName);
			mlMetaData.addMetadataXMLNode(mvMetaData);
			mvMetaData = new MetadataXMLNode("BirthName", p.birthName);
			mlMetaData.addMetadataXMLNode(mvMetaData);
			mvMetaData = new MetadataXMLNode("Identifier", p.identifier);
			mlMetaData.addMetadataXMLNode(mvMetaData);
			contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, mlMetaData));
		} else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds an event value.
	 *
	 * @param identifier
	 *            the event identifier
	 * @param type
	 *            the event type
	 * @param dateTime
	 *            the event date time
	 * @param detail
	 *            the event detail
	 */
	public void addEventMetadata(String identifier, String type, String dateTime, String detail) {
		MetadataXMLNode mvMetaData;
		MetadataXMLList mlMetaData;

		mlMetaData = new MetadataXMLList();
		mvMetaData = new MetadataXMLNode("EventIdentifier", identifier);
		mlMetaData.addMetadataXMLNode(mvMetaData);
		mvMetaData = new MetadataXMLNode("EventType", type);
		mlMetaData.addMetadataXMLNode(mvMetaData);
		mvMetaData = new MetadataXMLNode("EventDateTime", dateTime);
		mlMetaData.addMetadataXMLNode(mvMetaData);
		mvMetaData = new MetadataXMLNode("EventDetail", detail);
		mlMetaData.addMetadataXMLNode(mvMetaData);
		contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode("Event", mlMetaData));
	}

	/**
	 * Adds for the key metadata an array of person values, with values in
	 * valuesList
	 * <p>
	 * This is a utility method, used first for the addresses list. For example
	 * the due structure for "TOTO<toto@sample.fr>" Addressee metadata in XML
	 * is:
	 * <p>
	 * <Addressee> <FirstName></FirstName> <BirthName>TOTO</BirthName>
	 * <Identifier>toto@sample.fr</Identifier></Addressee>
	 * <p>
	 * If valuesList is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and value is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param valuesList
	 *            Values list
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addPersonMetadataList(String key, List<String> valuesList, boolean mandatory) {
		MetadataPerson p;
		MetadataXMLNode mvMetaData;
		MetadataXMLList mlMetaData;

		if ((valuesList != null) && (valuesList.size() != 0)) {
			for (String s : valuesList) {
				p = new MetadataPerson(s);
				mlMetaData = new MetadataXMLList();
				mvMetaData = new MetadataXMLNode("FirstName", p.firstName);
				mlMetaData.addMetadataXMLNode(mvMetaData);
				mvMetaData = new MetadataXMLNode("BirthName", p.birthName);
				mlMetaData.addMetadataXMLNode(mvMetaData);
				mvMetaData = new MetadataXMLNode("Identifier", p.identifier);
				mlMetaData.addMetadataXMLNode(mvMetaData);
				contentmetadatalist.addMetadataXMLNode(new MetadataXMLNode(key, mlMetaData));
			}
		} else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds an object with content from a String.
	 * <p>
	 * This object will be saved to disk UTF-8 encoded.
	 *
	 * @param stringContent
	 *            Object content
	 * @param filename
	 *            File name
	 * @param usage
	 *            Usage type (BinaryMaster| TextContent...)
	 * @param version
	 *            Object version (usually 1)
	 */
	public void addObject(String stringContent, String filename, String usage, int version) {
		objects.add(new ArchiveObject(stringContent.getBytes(), normalizeFilename(filename), usage, version));
	}

	/**
	 * Adds an object with content from a byte array.
	 * <p>
	 * This object will be saved to disk in raw binary format.
	 *
	 * @param byteContent
	 *            Object content
	 * @param filename
	 *            File name
	 * @param usage
	 *            Usage type (BinaryMaster| TextContent...)
	 * @param version
	 *            Object version (usually 1)
	 */
	public void addObject(byte[] byteContent, String filename, String usage, int version) {
		objects.add(new ArchiveObject(byteContent, normalizeFilename(filename), usage, version));
	}

	// create all the directories hierarchy
	private void createDirectory(String dirname) throws ExtractionException {
		File dir = new File(dirname);
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new ExtractionException("mailextract: Illegal destination directory, writing unit \"" + name + "\"");
		}
	}

	// create a file from byte array
	private void writeFile(String dirPath, String filename, byte[] byteContent) throws ExtractionException {
		OutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(dirPath + File.separator + filename));
			if (byteContent != null)
				output.write(byteContent);
		} catch (IOException ex) {
			if (dirPath.length() + filename.length() > 250)
				throw new ExtractionException(
						"mailextract: Illegal destination file (may be too long pathname), writing unit \"" + name
								+ "\"" + " dir=" + dirPath + " filename=" + filename);
			else
				throw new ExtractionException("mailextract: Illegal destination file, writing unit \"" + name + "\""
						+ " dir=" + dirPath + " filename=" + filename);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					throw new ExtractionException("mailextract: Can't close file, writing unit \"" + name + "\""
							+ " dir=" + dirPath + " filename=" + filename);
				}
		}
	}

	/**
	 * Write the Archive Unit representation on disk.
	 *
	 * @throws ExtractionException
	 *             Any unrecoverable extraction exception (access trouble, major
	 *             format problems...)
	 */
	public void write() throws ExtractionException {
		String dirPath;
		String filename;

		// different name if groupe unit or unit with objects
		dirPath = getFullName();

		// write unit directory
		createDirectory(dirPath);

		// add content surrounding metadata
		MetadataXMLNode contentmetadata = new MetadataXMLNode("Content", contentmetadatalist);

		// write unit metadata file
		writeFile(dirPath, "ArchiveUnitContent.xml", contentmetadata.writeXML().getBytes());

		// write objects files
		if (!objects.isEmpty()) {
			for (ArchiveObject o : objects) {
				if (o.filename == null || o.filename.isEmpty())
					filename = "undefined";
				else
					filename = o.filename;
				writeFile(dirPath, "__" + o.usage + "_" + Integer.toString(o.version) + "_" + filename, o.rawContent);
			}
		}
	}

	// reduce if needed a filename conserving the extension
	private String normalizeFilename(String filename) {
		String result = "";
		String extension = "";
		int len;

		// extract extension, short string after last point, if any
		int lastPoint = filename.lastIndexOf('.');
		if (lastPoint != -1) {
			extension = filename.substring(lastPoint);
			if (lastPoint >= 1)
				result = filename.substring(0, lastPoint);
			else
				result = "";
		} else
			result = filename;

		len = storeExtractor.getOptions().namesLength + 20;

		result = result.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}\\.]", "-");

		if (result.length() > len)
			result = result.substring(0, len);

		return result + extension;
	}

	// create a unique name for an typed archive unit reduced as defined by options
	private String normalizeUniqUnitname(String type, String filename) {
		String result = "";
		int len;

		len = storeExtractor.getOptions().namesLength;
		if (len < 32)
			type = type.substring(0, 1);

		if (filename != null)
			result = filename.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}]", "-");

		if (result.length() > len)
			result = result.substring(0, len);
		result = type + "#" + Integer.toString(storeExtractor.getUniqID()) + "-" + result;

		return result;
	}

}
