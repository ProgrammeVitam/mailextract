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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Class for complex list MetaData object which can contain any list of couples (key, Metadata object).
 * <p>
 * It keeps the list ordered in the order in which couples (key, MetaData objects) were added.
 */

public class MetaDataList extends MetaData {
	private LinkedHashMap<String, MetaData> mdLinkedMap;

	/**
	 * Instantiates a new MetaDataList Object.
	 * <p>
	 * Always created empty.
	 */
	public MetaDataList() {
		mdLinkedMap = new LinkedHashMap<String, MetaData>();
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
	 */
	public boolean isEmpty() {
		return mdLinkedMap.isEmpty();
	}

	/**
	 * Adds a couple (key, MetaData object) to the current MetadataList
	 *
	 * @param key
	 *            Key
	 * @param value
	 *            MetaData object value
	 */
	public void addMetadata(String key, MetaData value) {
		if ((value != null) && !value.isEmpty())
			mdLinkedMap.put(key, value);
	}

	/**
	 * Adds a couple (key, MetaData object) to the current MetadataList, MetaData object containing the String value.
	 * <p>
	 * Utility method for common case (key, string value).
	 *
	 * @param key
	 *            Key
	 * @param value
	 *            String value
	 */
	public void addMetadata(String key, String value) {
		addMetadata(key, new MetaDataString(value));
	}

	/**
	 * Adds a couple (key, MetaData object) to the current MetadataList, MetaData object containing an array of all the String values in the list.
	 * <p>
	 * Utility method for common case (key, array of string value).
	 *
	 * @param key
	 *            Key
	 * @param valuesList
	 *            String value list
	 */
	public void addMetadata(String key, List<String> valuesList) {
		MetaDataArray mvMetaData = new MetaDataArray();

		if ((valuesList != null) && (valuesList.size() != 0)) {
			for (String s : valuesList) {
				MetaDataString mdStringValue = new MetaDataString(s);
				mvMetaData.addMetadata(mdStringValue);
			}
			addMetadata(key, mvMetaData);
		}
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.mailextract.core.MetaData#writeJSON(int)
	 */
	protected String writeJSON(int depth) {
		String result;
		Set<String> keySet = mdLinkedMap.keySet();
		boolean first = true;
		String tabs=depthTabs(depth);

		result = "{";
		if (keySet.size() > 1)
			result += "\n" + tabs;
		for (String s : keySet) {
			if (first)
				first = false;
			else
				result += ",\n" + tabs;
			result += "\"" + s + "\" : " + mdLinkedMap.get(s).writeJSON(depth + 1);
		}
		if (keySet.size() > 1)
			result += "\n" + tabs;
		result += "}";
		return result;
	}

	/**
	 * Write the metadata in JSON format.
	 * <p>
	 * The root metadata structure is always of list nature and this method is the only public one to write metadata, beginning at tabs depth 0.
	 * @return the string
	 */
	public String writeJSON() {
		return writeJSON(0);
	}

}