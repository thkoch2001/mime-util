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
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.detector.MimeDetector;
import eu.medsea.mimeutil.handler.MimeHandler;

/**
 * ALL MimeDetector(s) must extend this class.
 * @author Steven McArdle
 *
 */
public abstract class MimeDetector {

	private SortedSet mimeHandlers = new TreeSet();

	/**
	 * Gets the name of this MimeDetector
	 * @return name of MimeDetector as a fully qualified class name
	 */
	public final String getName() {
		return getClass().getName();
	}

	/**
	 * Add a mime handler to this MimeDetector. The MimeHandler(s) are given a chance
	 * to influence the collection of MimeTypes before they are returned for collation by
	 * MimeUtil and then passed back to the client.
	 * @param handler
	 */
	public final void addMimeHandler(MimeHandler handler) {
		mimeHandlers.add(handler);
	}

	/**
	 * remove a mime handler to this MimeDetector. The MimeHandler(s) are given a chance
	 * to influence the collection of MimeTypes before they are returned for collation by
	 * MimeUtil and then passed back to the client.
	 * @param handler
	 * @return true if the MimeHandler was removed else false.
	 */
	public final boolean removeMimeHandler(MimeHandler handler) {
		return mimeHandlers.remove(handler);
	}

	/**
	 * Return the Set of MimeHandler(s). A set is used as the MimeHandler(s)
	 * are order maintained.
	 * @return Set of currently registered MimeHandler(s)
	 */
	public final Set getMimeHandlers() {
		return mimeHandlers;
	}

	/*
	 * Iterates over the Set of MimeHandler(s)and executes them if the MimeType collection
	 * contains any of the MimeTypes the MimeHandler is interested in.
	 */
	private Collection fireMimeHandlers(Collection mimeTypes) {
		for(Iterator it = mimeHandlers.iterator(); it.hasNext();) {
			MimeHandler mh = (MimeHandler)it.next();
			for(Iterator itmh = mh.getMimeTypes().iterator(); itmh.hasNext();) {
				MimeType mt = (MimeType)itmh.next();
				// If the MimeTypeHandler is interested in any of the mimeTypes then fire it.
				if(mimeTypes.contains(mt)) {
					mimeTypes = mh.handle(mimeTypes);
					// Only call the MimeHandler once.
					// It can process all the types it is interested in if more than one MimeType was registered.
					break;
				}
			}
		}
		return mimeTypes;
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(URLConnection) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param url
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesInputStream(...) abstract method.
	 */
	public final Collection getMimeTypes(final URLConnection url) {
		try {
			return fireMimeHandlers(getMimeTypesInputStream(new BufferedInputStream(url.getInputStream())));
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
		return fireMimeHandlers(getMimeTypesFile(file));
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(fileName) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param file
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesFile(...) abstract method.
	 */
	public final Collection getMimeTypes(final String fileName) throws MimeException {
		return fireMimeHandlers(getMimeTypesFile(new File(fileName)));
	}

	/**
	 * This method is called by the MimeUtil getMimeTypes(InputStream) method via the MimeUtil.MimeUtilMimeDetectorRegistry class.
	 * @param file
	 * @return collection of matched MimeType(s) from the specific MimeDetector getMimeTypesInputStream(...) abstract method.
	 */
	public final Collection getMimeTypes(final InputStream in) throws MimeException {
		return fireMimeHandlers(getMimeTypesInputStream(in));
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
	 *
	 * @param in InputStream. If the InputStream does not support the mark() and reset() methods it will throw an exception.
	 * @return collection of matched MimeType(s)
	 * @throws UnsupportedOperationException
	 */
	public abstract Collection getMimeTypesInputStream(final InputStream in) throws UnsupportedOperationException;
}


