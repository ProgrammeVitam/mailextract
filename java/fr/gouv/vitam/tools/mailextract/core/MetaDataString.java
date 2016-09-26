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

/**
 * Class for simple MetaData object which can contain only a String.
 */
public class MetaDataString extends MetaData {

	/** String value. */
	String value;

	/**
	 * Instantiates a new MetaDataString.
	 *
	 * @param value
	 *            String value
	 */
	MetaDataString(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
	 */
	public boolean isEmpty() {
		return (value == null) || value.isEmpty();
	}

	/**
	 * Write the metadata in JSON format.
	 * <p>
	 * The String is UTF-8 encoded with \r,\n,\t,\ and " escaped
	 * 
	 * @param depth
	 *            Depth used for tabulation (no use for this tree Metadata
	 *            Structure which is always a terminal tree node)
	 * @return the string
	 */
	public String writeJSON(int depth) {
		return "\"" + encodeJSONString(value) + "\"";
	}

	/**
	 * Write the metadata in XML format.
	 * <p>
	 * The String is UTF-8 encoded with \r,\n,\t escaped
	 * and <,&,>,' and " XML encoded
	 * 
	 * @param depth
	 *            Depth used for tabulation (no use for this tree Metadata
	 *            Structure which is always a terminal tree node)
	 * @return the string
	 */
	public String writeXML(int depth) {
		return encodeXMLString(value);
	}

	// TODO préciser l'encodage des métadonnées dans le JSON
	// escape some problematic characters in JSON
	private static String encodeJSONString(String in) {
		in = in.replace("\\", "\\\\");
		in = in.replace("\n", "\\n");
		in = in.replace("\r", "\\r");
		in = in.replace("\t", "\\t");
		in = in.replace("\"", "\\\"");
		return in;
	}

	// TODO préciser l'encodage des métadonnées dans le JSON
	// escape some problematic characters in JSON
	private static String encodeXMLString(String in) {
		in = in.replace("\\", "\\\\");
		in = in.replace("\n", "\\n");
		in = in.replace("\r", "\\r");
		in = in.replace("\t", "\\t");
		in = in.replace("\"", "&quot;");
		in = in.replace("'", "&apos;");
		in = in.replace("<", "&lt;");
		in = in.replace(">", "&gt;");
		in = in.replace("&", "&amp;");

		return in;
	}
}
