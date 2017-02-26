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

package fr.gouv.vitam.tools.mailextract.lib.javamail.thundermbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import javax.mail.util.SharedFileInputStream;

/**
 * Optimized buffered mbox file reader for Thunderbird mbox file.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class ThunderMboxFileReader {

	private Logger logger;

	private RandomAccessFile raf;
	private SharedFileInputStream sifs;
	private String filePath;

	// for buffered access to RandomAccessFile
	private static int BUFFER_SIZE = 4096;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private int len = 0;
	private long bufferPos = 0;
	private int curPos = 0;
	private int lineNum = 0;
	private long fromLineEnd = 0;

	/**
	 * Instantiates a new thunder mbox file reader.
	 *
	 * @param logger
	 *            Operation store extractor logger
	 * @param file
	 *            File
	 * @throws IOException
	 *             Unable to open the file.
	 */
	ThunderMboxFileReader(Logger logger, File file) throws IOException {
		this.logger = logger;
		this.filePath = file.getPath();
		raf = new RandomAccessFile(file, "r");
		sifs = new SharedFileInputStream(file);
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
		return logger;
	}

	/**
	 * Close.
	 *
	 * @throws IOException
	 *             Unable to close the file.
	 */
	public void close() throws IOException {
		raf.close();
		sifs.close();
	}

	/**
	 * New stream, created from this file containing bytes from start position
	 * to end-1 position
	 * <p>
	 * If end=-1 from bytes are from start position to the end of file
	 * 
	 * @param start
	 *            Start
	 * @param end
	 *            End
	 * @return the input stream
	 */
	public InputStream newStream(long start, long end) {
		return sifs.newStream(start, end);
	}

	// buffered read
	private final int read() throws IOException {
		if (curPos >= len) {
			bufferPos = raf.getFilePointer();
			if ((len = raf.read(buffer)) == -1)
				return -1;
			curPos = 0;
		}
		return buffer[curPos++];
	}

	// buffered get file pointer
	private final long getFilePointer() {
		return bufferPos + curPos;
	}

	// read a complete line but return only first 64 bytes
	private final int readFirstBytesLine(RandomAccessFile raf, byte[] buffer) throws IOException {
		int i = 0;
		int b;

		lineNum++;
		while (true) {
			b = read();
			if (b == -1)
				return -1;
			if (b == '\n')
				return i;
			if (i < 64) {
				buffer[i++] = (byte) b;
			}
		}
	}

	// construct a String from buffer
	private String constructLine(byte[] buffer, int len) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < len; i++) {
			final char c=(char)(buffer[i]);
			if (c>=32)
			 stringBuilder.append(c);
		}
		return stringBuilder.toString();
	}

	// verify line compliance to the delimiter pattern
	// TODO get information on the Thunderbird mbox file delimiter
	// pattern and verify the date if present
	private boolean isCompliantFromLine(byte[] buffer, int len) {
		// too long to be a delimiter line
		if (len > 34) {
			return false;
		} else {
			String line = constructLine(buffer, len);
			if (line.length() == 5)
				return true;
			else if (line.startsWith("From - "))
				return true;
			getLogger().finest("mailextract.thundermbox: Misleading '"+line+"' line in file "+filePath+" at line "+Integer.toString(lineNum)+" is not considered as a message delimiter");
			return false;
		}
	}

	/**
	 * Gets the next position of a "From - date" line start.
	 *
	 * @return File position
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public long getNextFromLineBeg() throws IOException {
		long beg;
		int len;
		byte[] buffer = new byte[64];

		while (true) {
			beg = getFilePointer();
			len = readFirstBytesLine(raf, buffer);
			if (len == -1) {
				fromLineEnd = -1;
				return -1;
			}
			if ((buffer[0] == 'F') && (buffer[1] == 'r') && (buffer[2] == 'o') && (buffer[3] == 'm')
					&& (buffer[4] == ' ')) {
				// then verify whole line compliance
				if (isCompliantFromLine(buffer, len)) {
					fromLineEnd = getFilePointer();
					return beg;
				}
			}
		}
	}

	/**
	 * Gets the end position of the last "From " line identified.
	 *
	 * @return File position
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public long getLastFromLineEnd() throws IOException {
		return fromLineEnd;
	}
}
