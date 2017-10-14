package fr.gouv.vitam.tools.mailextract.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class RawDataSource implements DataSource{
		
		ByteArrayInputStream inputStream;
		
		String mimeType;
		
		String name;
		
		public RawDataSource(byte[] rawContent, String mimeType, String name) {
			if (rawContent==null)
				rawContent = new byte[0];
			inputStream= new ByteArrayInputStream(rawContent);
			this.mimeType=mimeType;
			this.name=name;
		}
		
		 /**
	     * This method returns an <code>InputStream</code> representing
	     * the data and throws the appropriate exception if it can
	     * not do so.  Note that a new <code>InputStream</code> object must be
	     * returned each time this method is called, and the stream must be
	     * positioned at the beginning of the data.
	     *
	     * @return an InputStream
	     */
	    public InputStream getInputStream() throws IOException
	    {
	    	return inputStream;
	    }

	    /**
	     * This method returns an <code>OutputStream</code> where the
	     * data can be written and throws the appropriate exception if it can
	     * not do so.  Note that a new <code>OutputStream</code> object must
	     * be returned each time this method is called, and the stream must
	     * be positioned at the location the data is to be written.
	     *
	     * @return an OutputStream
	     */
	    public OutputStream getOutputStream() throws IOException
	    {
	    	throw new IOException("No output on this Datasource");
	    }

	    /**
	     * This method returns the MIME type of the data in the form of a
	     * string. It should always return a valid type. It is suggested
	     * that getContentType return "application/octet-stream" if the
	     * DataSource implementation can not determine the data type.
	     *
	     * @return the MIME Type
	     */
	    public String getContentType(){
	    	return mimeType;
	    }

	    /**
	     * Return the <i>name</i> of this object where the name of the object
	     * is dependant on the nature of the underlying objects. DataSources
	     * encapsulating files may choose to return the filename of the object.
	     * (Typically this would be the last component of the filename, not an
	     * entire pathname.)
	     *
	     * @return the name of the object.
	     */
	    public String getName(){
	    	return name;
	    }

		
	}

