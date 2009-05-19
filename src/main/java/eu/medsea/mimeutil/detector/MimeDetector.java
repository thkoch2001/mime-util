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
package eu.medsea.mimeutil.detector;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MimeDetector;

/**
 * ALL MimeDetector(s) must extend this class.
 * @author Steven McArdle
 *
 */
public abstract class MimeDetector {

	/**
	 * Gets the name of this MimeDetector
	 * @return name of MimeDetector as a fully qualified class name
	 */
	public final String getName() {
		return getClass().getName();
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(byte []) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param data
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesByteArray(...) abstract method.
	 */
	public final Collection getMimeTypes(final byte [] data) throws MimeException {
		Collection mimeTypes = getMimeTypesByteArray(data);
		// We remove any entry that may be the same as the UNKNOWN_MIME_TYPE
		// because we get this by default if there are NO other mime types.
		mimeTypes.remove(MimeUtil.UNKNOWN_MIME_TYPE);
		return mimeTypes;
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(URLConnection) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param url
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesInputStream(...) abstract method.
	 */
	public final Collection getMimeTypes(final URLConnection url) {
		try {
			Collection mimeTypes = getMimeTypes(new BufferedInputStream(url.getInputStream()));
			if(mimeTypes.isEmpty()) {
				// If no mime types are returned try to get the mime type from the URLConnection
				String mimeType = url.getContentType();
				if(mimeType != null && mimeType.length() != 0 && !"content/unknown".equals(mimeType)) {
					// We don't want to record empty mime types or the content/unknown mime type
					mimeTypes.add(url.getContentType());
				}
			}
			return mimeTypes;
		} catch (IOException e) {
			throw new MimeException(e);
		}
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(File) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param file
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesFile(...) abstract method.
	 */
	public final Collection getMimeTypes(final File file) throws MimeException {
		Collection mimeTypes = getMimeTypesFile(file);
		// We remove any entry that may be the same as the UNKNOWN_MIME_TYPE
		// because we get this by default if there are NO other mime types.
		mimeTypes.remove(MimeUtil.UNKNOWN_MIME_TYPE);
		return mimeTypes;
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(fileName) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param file
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesFile(...) abstract method.
	 */
	public final Collection getMimeTypes(final String fileName) throws MimeException {
		return getMimeTypes(new File(fileName));
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(InputStream) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * The Stream is checked to see that it supports the mark() and reset() methods but using them is down to the implementation.
	 * @param file
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesInputStream(...) abstract method.
	 */
	public final Collection getMimeTypes(final InputStream in) throws MimeException {
		if(!in.markSupported()) {
			throw new MimeException("mark() and reset() must be supported by the Stream.");
		}
		Collection mimeTypes = getMimeTypesInputStream(in);
		// We remove any entry that may be the same as the UNKNOWN_MIME_TYPE
		// because we get this by default if there are NO other mime types.
		mimeTypes.remove(MimeUtil.UNKNOWN_MIME_TYPE);
		return mimeTypes;
	}

	/**
	 * Abstract method to be implement by concrete MimeDetector(s).
	 * @return description of this MimeDetector
	 */
	public abstract String getDescription();

	/**
	 * Abstract method that must be implemented by concrete MimeDetector(s). This takes a File object and is
	 * called by the MimeUtil getMimeTypes(fileName) and getMimeTypes(file) methods.
	 * If your MimeDetector does not handle File objects then either throw an UnsupportedOperationException or return an
	 * empty collection.
	 *
	 * @param file
	 * @return collection of matched MimeType(s)
	 * @throws UnsupportedOperationException
	 */
	public abstract Collection getMimeTypesFile(final File file) throws UnsupportedOperationException;

	/**
	 * Abstract method that must be implemented by concrete MimeDetector(s). This takes an InputStream object and is
	 * called by the MimeUtil getMimeTypes(URLConnection) and getMimeTypes(InputStream) methods.
	 * If your MimeDetector does not handle InputStream objects then either throw an UnsupportedOperationException or return an
	 * empty collection.
	 * <p>
	 * If the InputStream passed in does not support the mark() and reset() methods a MimeException will be thrown
	 * before reaching this point. The implementation is responsible for the actual use of the mark() and reset() methods
	 * as the amount of data to retrieve from the stream is implementation and even call to call dependent.
	 * If you do not use the mark() and reset() methods on the Stream then the position in the Stream will have moved on when this method returns.
	 * </p>
	 * <p>
	 * To allow the reuse of the Stream in other parts of your code in a way that it is unaware of
	 * any data read via this method i.e. the Stream position will be returned to where it was when this method was called,
	 * it is IMPORTANT to utilise the mark() and reset() methods within your implementing method. If you do not care about the
	 * position for further reading you can ignore their use but the utility will still throw a MimeException if the Stream
	 * does not support the mark() and reset() methods.
	 * </p>
	 * @param in InputStream.
	 *
	 * @return collection of matched MimeType(s)
	 * @throws UnsupportedOperationException
	 */
	public abstract Collection getMimeTypesInputStream(final InputStream in) throws UnsupportedOperationException;

	/**
	 * Abstract method that must be implemented by concrete MimeDetector(s). This takes a byte [] object and is
	 * called by the MimeUtil getMimeTypes(byte []) method.
	 * If your MimeDetector does not handle byte [] objects then either throw an UnsupportedOperationException or return an
	 * empty collection.
	 *
	 * @param data byte []. Is a byte array that you want to parse for matching mime types.
	 * @return collection of matched MimeType(s)
	 * @throws UnsupportedOperationException
	 */
	public abstract Collection getMimeTypesByteArray(final byte [] data) throws UnsupportedOperationException;
}


