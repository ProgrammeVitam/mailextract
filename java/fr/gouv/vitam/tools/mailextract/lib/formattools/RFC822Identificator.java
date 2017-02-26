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
package fr.gouv.vitam.tools.mailextract.lib.formattools;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import fr.gouv.vitam.tools.mailextract.lib.core.ExtractionException;

public class RFC822Identificator {
	private static final int MAX_TRIES = 3;
	private static final long MILLI_SECONDS_BETWEEN_TRIES = 1000;

	/** Singleton instance **/
	private static final RFC822Identificator INSTANCE = new RFC822Identificator();

	/** Tika object **/
	private Tika tika;

	/** Connection for Siegfried if present **/
	private CloseableHttpClient httpClient;

	/** * Default JsonFactory */
	private JsonFactory jsonFactory;

	/** * Default ObjectMapper */
	private ObjectMapper objectMapper;

	/**
	 * Private constructor.
	 */
	private RFC822Identificator() {
		httpClient = HttpClients.createDefault();

		// try to connect to see if SiegFried is active
		try {
			String test = "Hello world";
			String result = callSiegfried("http://localhost:5138", test.getBytes());
			if (!result.startsWith("{\"siegfried\""))
				throw new ExtractionException();
			tika = new Tika();
			jsonFactory = new JsonFactory();
			objectMapper = buildObjectMapper(jsonFactory);
		} catch (ExtractionException e) {
			tika = new Tika();
			objectMapper = null;
		}
	}

	private static final ObjectMapper buildObjectMapper(JsonFactory jsonFactory) {
		final ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
		// objectMapper.registerModule(new JavaTimeModule());
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
		objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		objectMapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
		return objectMapper;
	}

	/**
	 * Get the FileTextExtractor singleton.
	 *
	 * @return single instance of FileTextExtractor
	 */
	public static RFC822Identificator getInstance() {
		return INSTANCE;
	}

	/**
	 * Gets the mime type.
	 *
	 * @param rawContent
	 *            the raw content
	 * @return the mime type
	 */
	public boolean isRFC822(byte[] rawContent) throws ExtractionException {
		String mimeType;

		try {
			if (rawContent.length > 0) {
				mimeType = tika.detect(rawContent);
				if (mimeType.equals("message/rfc822"))
					return true;
				else if (objectMapper != null) {
					mimeType = getSiegFriedMimeType(rawContent);
					if (mimeType.equals("message/rfc822"))
						return true;
				}
			}
		} catch (Exception e) {
			// if any problem in identification tools, considered not a message
			return false;
		}
		return false;
	}

	private String getSiegFriedMimeType(byte[] rawContent) throws ExtractionException {
		int tries = 0;
		String mimeType = "UNKNOWN";

		while (true) {
			try {
				tries += 1;
				String responseSiegfried = callSiegfried("http://localhost:5138", rawContent);
				JsonNode jsonNode;
				try {
					jsonNode = objectMapper.readTree(responseSiegfried);
				} catch (final IOException | IllegalArgumentException e) {
					throw new ExtractionException("Inner");
				}

				JsonNode j = jsonNode.get("files").get(0).get("matches").get(0);
				String formatId = j.get("id").asText();
				if (!"UNKNOWN".equals(formatId))
					mimeType = j.get("mime").asText();
				break;
			} catch (ExtractionException e) {
				if (tries > MAX_TRIES) {
					throw new ExtractionException("mailextract.formattools: error on the Json got from Siegfried", e);
				}
				sleep(MILLI_SECONDS_BETWEEN_TRIES);
			}
		}
		return mimeType;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {// NOSONAR : This should never happen

		}

	}

	private String callSiegfried(String siegfriedURL, byte[] rawContent) throws ExtractionException {
		String returnSiegfriedValue = null;

		try {
			HttpPost post = new HttpPost(siegfriedURL + "/identify?format=json");
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody("file", rawContent, ContentType.DEFAULT_BINARY, "tmp");
			HttpEntity entity = builder.build();
			post.setEntity(entity);
			HttpResponse response = httpClient.execute(post);
			returnSiegfriedValue = EntityUtils.toString(response.getEntity(), CharsetUtils.get("UTF8"));
			post.releaseConnection();
		} catch (IOException ie) {
			throw new ExtractionException("mailextract.formattools: Error on the HTTP client sent to siegfried");
		}
		return returnSiegfriedValue;
	}
}
