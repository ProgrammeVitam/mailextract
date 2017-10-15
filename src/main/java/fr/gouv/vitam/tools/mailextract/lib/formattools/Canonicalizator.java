package fr.gouv.vitam.tools.mailextract.lib.formattools;

import java.io.PrintStream;
import java.io.OutputStream;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

public class Canonicalizator {
	/** Singleton instance **/
	private static Canonicalizator INSTANCE = new Canonicalizator();

	/** Private constructor */
	private Canonicalizator() {
		Logger logger;

		// get rid of all the search properties and so on initialization
		// messages of ESAPI
		try {
			PrintStream originalOut = System.out;
			PrintStream originalErr = System.err;
			PrintStream out = new PrintStream(new OutputStream() {
				public void write(int b) {
					// Do nothing
				}
			});
			System.setOut(out);
			System.setErr(out);
			ESAPI.encoder();
			logger = ESAPI.getLogger("Encoder");
			logger.setLevel(Logger.OFF);
			System.setOut(originalOut);
			System.setErr(originalErr);
		} catch (Exception e) {
			// Do nothing
		}
	}

	/**
	 * Get the XMLCanonicalizator singleton.
	 *
	 * @return single instance of XMLCanonicalizator
	 */
	public static Canonicalizator getInstance() {
		return INSTANCE;
	}

	/**
	 * Canonicalize a String, removing escape entities (even mixed and
	 * multiples) and xml encode
	 * 
	 * @param in
	 *            the input string
	 * @return formatted text, or empty string if none
	 */
	public String toXML(String in) {
		String result;

		if (in == null)
			result = "";
		else {
			// unescape all HTML entities with no exception if complex coding
			// found
			result = ESAPI.encoder().canonicalize(in, false);
			// then xml encoding at minimal level with UTF8 coding
			result = result.replace("&", "&amp;");
			result = result.replace("\"", "&quot;");
			result = result.replace("'", "&apos;");
			result = result.replace("<", "&lt;");
			result = result.replace(">", "&gt;");
			// other solution with ESAPI but encode even the UTF8
//			 result = ESAPI.encoder().encodeForXML(result); 
		}

		return result;
	}

	/**
	 * Canonicalize a String, removing escape entities (even mixed and
	 * multiples)
	 * 
	 * @param in
	 *            the input string
	 * @return formatted text, or empty string if none
	 */
	public String toSimpleText(String in) {
		String result;

		if (in == null)
			result = "";
		else
			// unescape all HTML entities with no exception if complex coding
			// found
			result = ESAPI.encoder().canonicalize(in, false);

		return result;
	}
}
