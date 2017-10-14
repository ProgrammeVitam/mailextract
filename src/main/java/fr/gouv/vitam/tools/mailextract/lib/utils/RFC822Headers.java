package fr.gouv.vitam.tools.mailextract.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeUtility;

public class RFC822Headers extends InternetHeaders {

	
	static ByteArrayInputStream getBAIS(String headersString){
		if (headersString == null)
			headersString="";
		headersString += "\n\n";
		ByteArrayInputStream bais = new ByteArrayInputStream(headersString.getBytes());
		return bais;
	}
	
	public RFC822Headers(String headersString) throws MessagingException {
		super(getBAIS(headersString),true);
		// no use to close on ByteArrayInputStream
	}
	
	
	// utility function to get the value part of an header string
	public static String getHeaderValue(String line) {
	    int i = line.indexOf(':');
	    if (i < 0)
		return line;
	    // skip whitespace after ':'
	    int j;
	    for (j = i + 1; j < line.length(); j++) {
		char c = line.charAt(j);
		if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
		    break;
	    }
	    return line.substring(j);
	}


	public List<String> getReferences() {
		List<String> result = null;
		String refHeader = getHeader("References"," ");
		if (refHeader!=null) {
			result = new ArrayList<String>();
			String[] refList = getHeaderValue(refHeader).split(",");
			for (String tmp: refList )	
				try {
				result.add(MimeUtility.decodeText(tmp));
				} catch (UnsupportedEncodingException uee) {
					// too bad
				}
			}
		return result;
	}

	static private String getElementalStringAddress(InternetAddress address) {
		String result;
		String s;

		if (address != null) {
			s = address.getPersonal();
			if (s != null)
				result = s + " ";
			else
				result = "";
			s = address.getAddress();
			if (s != null)
				result += "<" + s + ">";
		} else
			result = "";
		return result;
	}

	static private String getStringAddress(InternetAddress address) {
		String result;

		if (address != null) {
			try {
				result = getElementalStringAddress(address);
				// special case of group address (RFC 2822)
				if (address.isGroup()) {
					result += ":";
					InternetAddress[] group = address.getGroup(false);
					for (int k = 0; k < group.length; k++) {
						if (k > 0)
							result += ",";
						result += getElementalStringAddress(group[k]);
					}
				}
			} catch (AddressException e) {
				// not supposed to be
				result = "";
			}
		} else
			result = "";
		return result;
	}

	public List<String> getAddressHeader(String name) {
		List<String> result = new ArrayList<String>();
		String addressHeaderString = getHeader(name,", ");
		if (addressHeaderString != null) {
			InternetAddress[] iAddressArray = null;
			try {
				iAddressArray = InternetAddress.parseHeader(addressHeaderString, false);
			} catch (AddressException e) {
				try {
					// try at least to Mime decode
					addressHeaderString = MimeUtility.decodeText(addressHeaderString);
				} catch (UnsupportedEncodingException uee) {
					// too bad
				}
				// wrongly formatted address, keep raw address list in metadata");
				result.add(addressHeaderString);
				return result;
			}
			if (iAddressArray != null) {
				for (InternetAddress ia : iAddressArray) {
					result.add(getStringAddress(ia));
				}
			}
		}
		return result;
	}

}
