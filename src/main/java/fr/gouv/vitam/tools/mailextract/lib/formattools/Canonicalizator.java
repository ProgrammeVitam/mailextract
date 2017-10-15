package fr.gouv.vitam.tools.mailextract.lib.formattools;


import org.jsoup.parser.Parser;

public class Canonicalizator {
	/** Singleton instance **/
	private static Canonicalizator INSTANCE = new Canonicalizator();

	/** Private constructor */
	private Canonicalizator() {
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
			// unescape all HTML entities, multiple times if needed
			result = in;
			do {
				in = result;
				result = Parser.unescapeEntities(in, true);
			} while (!result.equals(in));

			// then xml encoding at minimal level with UTF8 coding
			result = result.replace("&", "&amp;");
			result = result.replace("\"", "&quot;");
			result = result.replace("'", "&apos;");
			result = result.replace("<", "&lt;");
			result = result.replace(">", "&gt;");
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
		else {
			// unescape all HTML entities, multiple times if needed
			result = in;
			do {
				in = result;
				result = Parser.unescapeEntities(in, true);
			} while (!result.equals(in));

		}
		return result;
	}
}
