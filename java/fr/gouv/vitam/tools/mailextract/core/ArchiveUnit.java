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

package fr.gouv.vitam.tools.mailextract.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class for SEDA Archive Unit managing metadata, objects, if any, and on disk
 * representation.
 * 
 * <p>
 * Other classes create Archive Units on the fly with extracted information and
 * content. This class write on disk representation with convenient
 * directory/file structure and manifest.json files for metadata.
 * <p>
 * All the files, if not pure binary, are UTF-8 encoded, as the file names.
 */
public class ArchiveUnit {
	private StoreExtractor storeExtractor;
	private String rootPath;
	private String name;
	private MetaDataList mdObject = new MetaDataList();;
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
	}

	/**
	 * Instantiates a new archive unit.
	 * <p>
	 * Name is reduced to 32 characters (8 if NAMES_SHORTENED option is
	 * enabled), and Archive Unit directory name is generated as
	 * "'unitType'#'UniqID':'name' (with unitType first character only if
	 * NAMES_SHORTENED option is enabled)
	 * 
	 * @param storeExtractor
	 *            Operation store extractor
	 * @param father
	 *            Father Archive Unit
	 * @param unitType
	 *            Unit type (Folder| Message| Attachment)
	 * @param name
	 *            Name of the Archive Unit
	 */
	public ArchiveUnit(StoreExtractor storeExtractor, ArchiveUnit father, String unitType, String name) {
		this.storeExtractor = storeExtractor;
		if (unitType == null)
			this.name = name;
		else
			this.name = this.normalizeUniqFilename(unitType, name);
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
		if (objects.isEmpty())
			return rootPath + File.separator + name;
		else
			return rootPath + File.separator + "__" + name + "__";
	}

	/**
	 * Adds a single key, object metadata, object being a String.
	 * <p>
	 * If object is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and object is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Metadata String value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addMetadata(String key, String value, boolean mandatory) {
		if ((value != null) && !value.isEmpty())
			mdObject.addMetadata(key, value);
		else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds a single key, object metadata, object being a String and with a
	 * default value if object is null or empty.
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
			mdObject.addMetadata(key, value);
		else
			mdObject.addMetadata(key, defaultValue);
	}

	/**
	 * Adds a single key, object metadata, object being an array of Strings.
	 * <p>
	 * If object is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and object is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Metadata array of String value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addArrayMetadata(String key, List<String> value, boolean mandatory) {
		if ((value != null) && (!value.isEmpty())) {
			mdObject.addMetadata(key, value);
		} else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds for the key metadata an array of subkey, values, with values in
	 * valuesList
	 * <p>
	 * This is a utility method, used first for the addresses list. For example
	 * the due structure for Addressee metadata is:
	 * <p>
	 * {"Addressee" :
	 * <ul>
	 * [
	 * <ul>
	 * {"Identifier" : "<toto@sample.fr>"}<br>
	 * {"Identifier" : "<titi@sample.fr>"}
	 * </ul>
	 * ]
	 * </ul>
	 * }
	 * <p>
	 * If object is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and value is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param subkey
	 *            Subkey
	 * @param valuesList
	 *            Values list
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addSubKeyArrayListMetadata(String key, String subkey, List<String> valuesList, boolean mandatory) {
		if ((valuesList != null) && (valuesList.size() != 0)) {
			MetaDataArray mvMetaData = new MetaDataArray();
			for (String s : valuesList) {
				MetaDataList mdList = new MetaDataList();
				mdList.addMetadata(subkey, s);
				mvMetaData.addMetadata(mdList);
			}
			mdObject.addMetadata(key, mvMetaData);
		} else if (mandatory)
			getLogger().finest("mailextract: mandatory metadata '" + key + "' empty in unit '" + name + "' in folder '"
					+ rootPath + "'");
	}

	/**
	 * Adds a single key, object metadata, object being an array of Strings with
	 * only one String.
	 * <p>
	 * This is a utility method, used first for Title. The due structure for
	 * Title metadata is:
	 * <p>
	 * {"Title" : ["This is a line of title"]}
	 * <p>
	 * If object is null or empty, no metadata is added.
	 * <p>
	 * If mandatory flag is true and object is null or empty, the metadata lack
	 * is logged
	 *
	 * @param key
	 *            Metadata key
	 * @param value
	 *            Metadata array of String value
	 * @param mandatory
	 *            Mandatory flag
	 */
	public void addArrayOneMetadata(String key, String value, boolean mandatory) {
		if ((value != null) && (!value.isEmpty())) {
			MetaDataArray mvMetaData = new MetaDataArray();
			MetaDataString mdStringValue = new MetaDataString(value);
			mvMetaData.addMetadata(mdStringValue);
			mdObject.addMetadata(key, mvMetaData);
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
			throw new ExtractionException("mailextract: Illegal destination file, writing unit \"" + name + "\""
					+ "dir=" + dirPath + " filename=" + filename);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					throw new ExtractionException("mailextract: Can't close file, writing unit \"" + name + "\""
							+ "dir=" + dirPath + " filename=" + filename);
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

		// different name if groupe unit or unit with objects
		if (objects.isEmpty())
			dirPath = rootPath + File.separator + name;
		else
			dirPath = rootPath + File.separator + "__" + name + "__";

		// write unit directory
		createDirectory(dirPath);

		// write unit metadata file
		writeFile(dirPath, "manifest.json", mdObject.writeJSON().getBytes());

		// write objects files
		if (!objects.isEmpty()) {
			for (ArchiveObject o : objects) {
				writeFile(dirPath, o.usage + "_" + Integer.toString(o.version) + "_" + o.filename, o.rawContent);
			}
		}
	}

	// reduce if needed a filename conserving the extension
	private String normalizeFilename(String filename) {
		String result = "";
		int len;

		if (storeExtractor.hasOptions(StoreExtractor.CONST_NAMES_SHORTENED))
			len = 8;
		else
			len = 32;
		if (filename != null)
			result = filename.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}\\.]", "_");
	
		if (result.length() > len)
			result = result.substring(result.length() - len);

		return result;
	}

	// create a unique name for an typed archive unit reduced if needed
	private String normalizeUniqFilename(String type, String filename) {
		String result = "";
		int len;

		if (storeExtractor.hasOptions(StoreExtractor.CONST_NAMES_SHORTENED)) {
			len = 8;
			type = type.substring(0, 1);
		} else {
			len = 32;
		}
		if (filename != null) 
			result = filename.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}]", "_");
		
		if (result.length() > len)
			result = result.substring(0, len);
		result = type + "#" + Integer.toString(storeExtractor.getUniqID()) + "_" + result;

		return result;
	}

}
