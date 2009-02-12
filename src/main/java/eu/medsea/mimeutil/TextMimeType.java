/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.mimeutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

import eu.medsea.mimeutil.MimeException;

/**
 * This class can be used to represent a mime type for a text file.
 * This should only be returned by MimeDetector(s) that use magic number
 * type matching. It allows for an encoding to be associated to a text type
 * mime type such as text/plain.
 *
 * @author Steven McArdle
 *
 */
public class TextMimeType extends MimeType {

	private static final long serialVersionUID = -4798584119063522367L;

	private static TextEncodings textEncodings = new TextEncodings();

	// The default encoding is always set as UTF-8
	private String encoding = "UTF-8";

	/**
	 * Construct a new TestMimeType from an existing MimeType
	 * @param mimeType
	 */
	public TextMimeType(final MimeType mimeType) {
		super(mimeType);
	}

	/**
	 * Construct a new TextMimeType from a string representation of a MimeType
	 * @param mimeType
	 */
	public TextMimeType(final String mimeType) {
		super(mimeType);
	}

	/**
	 * Construct a TextMimeType from a string representation of a MimeType and
	 * an encoding that should be one of the known encodings.
	 *
	 * @param mimeType
	 * @param encoding
	 *
	 * @see #getKnownEncodings()
	 * @see #addKnownEncoding(String)
	 */
	public TextMimeType(final String mimeType, final String encoding) {
		super(mimeType);
		this.encoding = getValidEncoding(encoding);
	}

	/**
	 * Get the list of currently known encodings. This list is initialised from
	 * the file located in <code>eu.medsea.mimeutil.TextEncodings</code>. If a problem occurs
	 * while reading this file a default hard coded set of encodings are used.
	 *
	 * New encodings can be added to the known encodings using{@link #setEncoding(String)}
	 *
	 * @return collection of strings representing te encodings known to this class.
	 * @see #setEncoding(String)
	 */
	public Collection getKnownEncodings() {
		return textEncodings;
	}

	/**
	 * Add a new encoding to the internal encodings this class knows about.
	 *
	 * @param encoding
	 * @see #getKnownEncodings()
	 */
	public static void addKnownEncoding(final String encoding) {
		if(encoding == null || encoding.trim().length() == 0) {
			return;
		}
		textEncodings.add(encoding);
	}

	/**
	 * Get the encoding currently set for this TextMimeType. It will be one of the know encodings either
	 * initialised at class load time or one that has been added manually.
	 * @return the encoding as a string
	 * @see #getKnownEncodings()
	 * @see #setEncoding(String)
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Set the encoding used for the file or stream this MimeType is associated with
	 * @param encoding
	 */
	public void setEncoding(final String encoding) {
		if(!textEncodings.contains(encoding)) {
			throw new MimeException(new UnsupportedEncodingException("The encoding [" + encoding + "] is not a supported encoding."));
		}
		this.encoding = getValidEncoding(encoding);
	}

	/*
	 * Check to see if the encoding we want to set is a valid encoding.
	 * If its false we always return UTF-8
	 */
	private String getValidEncoding(final String encoding) {
		if(encoding == null || encoding.trim().length() == 0 || !isKnownEncoding(encoding)) {
			// Set UTF-8 as the default
			return "UTF-8";
		}
		return encoding;
	}

	/**
	 * Utility method to see if the passed in encoding is known to this class.
	 *
	 * @param encoding
	 * @return true if encoding passed in is one of the known encodings else false
	 * @see #getKnownEncodings()
	 */
	public boolean isKnownEncoding(String encoding) {
		return textEncodings.contains(encoding);
	}
}

/*
 * This class represents a list of known encodings used by TextMimeType.
 * It extends ArrayList so encodings can be added or removed. As
 * TextMimeType has no method to remove encodings it only realy supports
 * the adding of new encodings.
 */
class TextEncodings extends ArrayList {
	private static final long serialVersionUID = -247389882161262839L;

	TextEncodings() {
		try {
			parseEncodingFile(TextEncodings.class.getClassLoader().getResourceAsStream(
									"eu/medsea/util/TextEncodings"));
		} catch(Exception e) {
			// Failed to read the file so lets just add the basic types
			add("US-ASCII");
			add("ASCII");
			add("windows-1250");
			add("Cp1250");
			add("windows-1251");
			add("Cp1251");
			add("windows-1252");
			add("Cp1252");
			add("windows-1253");
			add("Cp1253");
			add("windows-1254");
			add("Cp1254");
			add("windows-1257");
			add("Cp1257");
			add("ISO-8859-1");
			add("ISO8859_1");
			add("ISO-8859-2");
			add("ISO8859_2");
			add("ISO-8859-4");
			add("ISO8859_4");
			add("ISO-8859-5");
			add("ISO8859_5");
			add("ISO-8859-7");
			add("ISO8859_7");
			add("ISO-8859-9");
			add("ISO8859_9");
			add("ISO-8859-13");
			add("ISO8859_13");
			add("ISO-8859-15");
			add("ISO8859_15");
			add("KOI8-R");
			add("KOI8_R");
			add("UTF-8");
			add("UTF8");
			add("UTF-16");
			add("UTF-16");
			add("UTF-16BE");
			add("UTF-16LE");
			add("UnicodeBig");
			add("UnicodeLittle");
		}
	}
	private void parseEncodingFile(InputStream is) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = br.readLine())!= null ) {
				String [] parts = line.split("\\s");
				if(parts.length > 1) {
					if("Name:".equals(parts[0]) || "Alias:".equals(parts[0])) {
						if(!"None".equals(parts[1])) {
							add(parts[1]);
						}
					}
				}
			}
		}finally {
			try {
				br.close();
			} catch (Exception ignore) {}
		}
	}
}

