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

package fr.gouv.vitam.tools.mailextract.javamail;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * (Strongly) Inspired by:
 * 
 * HTML to plain-text. This example program demonstrates the use of jsoup to
 * convert HTML input to lightly-formatted plain-text. That is divergent from
 * the general goal of jsoup's .text() methods, which is to get clean data from
 * a scrape.
 * <p>
 * Note that this is a fairly simplistic formatter -- for real world use you'll
 * want to embrace and extend.
 * </p>
 * <p>
 * To invoke from the command line, assuming you've downloaded the jsoup jar to
 * your current directory:
 * </p>
 * <p>
 * <code>java -cp jsoup.jar org.jsoup.examples.HtmlToPlainText url [selector]</code>
 * </p>
 * where <i>url</i> is the URL to fetch, and <i>selector</i> is an optional CSS
 * selector.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class JMHTMLToText {
	/**
	 * Format an Element to plain-text
	 * 
	 * @param element
	 *            the root element to format
	 * @return formatted text
	 */
	static public String getPlainText(Element element) {
		FormattingVisitor formatter = new FormattingVisitor();
		NodeTraversor traversor = new NodeTraversor(formatter);
		traversor.traverse(element); // walk the DOM, and call .head() and
										// .tail() for each node

		return formatter.toString();
	}
}

// the formatting rules, implemented in a breadth-first DOM traverse
final class FormattingVisitor implements NodeVisitor {
	private static final int maxWidth = 80;
	private int width = 0;
	private StringBuilder accum = new StringBuilder(); // holds the
														// accumulated text

	// hit when the node is first seen
	public void head(Node node, int depth) {
		String name = node.nodeName();
		if (node instanceof TextNode)
			append(((TextNode) node).text()); // TextNodes carry all
												// user-readable text in the
												// DOM.
		else if (name.equals("li"))
			append("\n * ");
		else if (name.equals("dt"))
			append("  ");
		else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr"))
			append("\n");
	}

	// hit when all of the node's children (if any) have been visited
	public void tail(Node node, int depth) {
		String name = node.nodeName();
		if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
			append("\n");
		else if (name.equals("a"))
			append(String.format(" <%s>", node.absUrl("href")));
	}

	// appends text to the string builder with a simple word wrap method
	private void append(String text) {
		if (text.startsWith("\n"))
			width = 0; // reset counter if starts with a newline. only from
						// formats above, not in natural text
		if (text.equals(" ") && (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n")))
			return; // don't accumulate long runs of empty spaces

		if (text.length() + width > maxWidth) { // won't fit, needs to wrap
			String words[] = text.split("\\s+");
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				boolean last = i == words.length - 1;
				if (!last) // insert a space if not the last word
					word = word + " ";
				if (word.length() + width > maxWidth) { // wrap and reset
														// counter
					accum.append("\n").append(word);
					width = word.length();
				} else {
					accum.append(word);
					width += word.length();
				}
			}
		} else { // fits as is, without need to wrap text
			accum.append(text);
			width += text.length();
		}
	}

	@Override
	public String toString() {
		return accum.toString();
	}
}
