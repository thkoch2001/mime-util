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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.medsea.mimeutil.detector.MimeDetector;
import eu.medsea.util.StringUtil;

/**
 * <p>
 * The <code>MimeUtil</code> utility is a utility class that allows applications to detect, work with and manipulate mime types.
 * </p>
 * <p>
 * A mime or "Multipurpose Internet Mail Extension" type is an Internet standard that is important outside of just e-mail use.
 * Mime is used extensively in other communications protocols such as HTTP for web communications.
 * IANA "Internet Assigned Numbers Authority" is responsible for the standardisation and publication of mime types. Basically any
 * resource on any computer that can be located via a URI can be assigned a mime type. So for instance, JPEG images have a mime type
 * of image/jpg. Some resources can have multiple mime types associated with them such as files with an XML extension have the mime types
 * text/xml and application/xml and even specialised versions of xml such as image/svg+xml for SVG image files.
 * </p>
 * <p>
 * To do this <code>MimeUtil</code> uses registered <code>MimeDetector</code>(s) that are delegated too in sequence to actually
 * perform the detection. There a three <code>MimeDetector</code>(s) registered by default that
 * perform detection based on file extensions, file globing and magic number detection using
 * the Unix file(1) magic.mime files.
 * Please refer to the java doc for each of these <code>MimeDetector</code>(s) for a description of how they
 * actually perform their particular detection process.
 * </p>
 * <p>
 * It is important to note that mime matching is not an exact science, meaning
 * that a positive match does not guarantee that the returned mime type is actually correct.
 * It is a best guess method of matching and the matched mime types should be used with this in
 * mind.
 * </p>
 * <p>
 * New <code>MimeDetector</code>(s) can easily be created and registered with <code>MimeUtil</code> to extend it's
 * functionality beyond these initial detection strategies by extending the <code>AbstractMimeDetector</code> class.
 * To see how to implement your own <code>MimeDetector</code> and register it with <code>MimeUtil</code> take a look
 * at the java doc and source code for the {@link ExtensionMimeDetector}, {@link MagicMimeMimeDetector} and
 * {@link GlobingMimeDetector} classes. Previously registered <code>MimeDetector</code>(s) can also be un-registered at
 * any time.
 * </p>
 * <p>
 * The order that the <code>MimeDetector</code>(s) are executed is defined by the priority of the individual <code>MimeDetector</code>(s)
 * and <code>MimeDetector</code>(s) with the same priority are executed in the order they are registered.
 * </p>
 * <p>
 * The resulting <code>Collection</code> of mime types returned in response to a getMimeTypes(...) call is a normalised list of the
 * accumulation of mime types returned by each of the registered <code>MimeDetector</code>(s) that implement the specified getMimeTypes(...)
 * methods. This Collection of mime types can be influenced using MimeHandler(s) that can be registered against one or more MimeDetector(s) that
 * are able to manipulate the Collection of mime types that will be returned to the client.
 * </p>
 * <p>
 * All methods in this class that return a Collection object actually return a {@link MimeTypeHashSet} that implements both the {@link Set} and {@link Collection}
 * interfaces.
 * </p>
 *
 * @author Steven McArdle.
 *
 */
public class MimeUtil {
	private static Log log = LogFactory.getLog(MimeUtil.class);

	/**
	 * Mime type used to identify a directory
	 */
	public static final MimeType DIRECTORY_MIME_TYPE = new MimeType("application/directory");
	/**
	 * Mime type used to identify a directory
	 */
	public static final MimeType UNKNOWN_MIME_TYPE = new MimeType("application/octet-stream");

	private static final Pattern mimeSplitter = Pattern.compile("[/;]++");

	// All mime types know to the utility
	private static Map mimeTypes = new HashMap();

	private static MimeUtilMimeDetectorRegistry mimeUtilMimeDetector = new MimeUtilMimeDetectorRegistry();

	// the native byte order of the underlying OS. "BIG" or "little" Endian
	private static ByteOrder nativeByteOrder = ByteOrder.nativeOrder();

	// Register all know internal mime detectors.
	static {
		InputStream is = MimeUtil.class.getResourceAsStream("/MimeDetectors");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		if(is != null) {
			try {
				String line = null;
				while((line = br.readLine()) != null) {
					if(line.trim().startsWith("#")) {
						continue;
					}
					Class.forName(line);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * While MimeType(s) are being loaded by the MimeDetector(s) they should be
	 * added to the list of known mime types. It is not mandatory for MimeDetector(s)
	 * to do so but they should where possible so that the list is as complete as possible.
	 * You can add other mime types to this list using this method. You can then use the
	 * isMimeTypeKnown(...) utility method to see if a mime type you have
	 * matches one that the utility has already seen.
	 * <p>
	 * This can be used to limit the mime types you work with i.e. if its not been loaded
	 * then don't bother using it as it won't match. This is no guarantee that a match will not
	 * be found as it is possible that a particular MimeDetector does not have an initialisation
	 * phase that loads all of the mime types it will match.
	 * </p>
	 * <p>
	 * For instance if you had a mime type of abc/xyz and passed this to
	 * isMimeTypeKnown(...) it would return false unless you specifically add
	 * this to the know mime types using this method.
	 * </p>
	 *
	 * @param mimeType
	 *            a mime type you want to add to the known mime types.
	 *            Duplicates are ignored.
	 * @see #isMimeTypeKnown(String mimetype)
	 */
	public static void addKnownMimeType(final MimeType mimeType) {
		addKnownMimeType(mimeType.toString());
	}


	/**
	 * While MimeType(s) are being loaded by the MimeDetector(s) they should be
	 * added to the list of known mime types. It is not mandatory for MimeDetector(s)
	 * to do so but they should where possible so that the list is as complete as possible.
	 * You can add other mime types to this list using this method. You can then use the
	 * isMimeTypeKnown(...) utility method to see if a mime type you have
	 * matches one that the utility has already seen.
	 * <p>
	 * This can be used to limit the mime types you work with i.e. if its not been loaded
	 * then don't bother using it as it won't match. This is no guarantee that a match will not
	 * be found as it is possible that a particular MimeDetector does not have an initialisation
	 * phase that loads all of the mime types it will match.
	 * </p>
	 * <p>
	 * For instance if you had a mime type of abc/xyz and passed this to
	 * isMimeTypeKnown(...) it would return false unless you specifically add
	 * this to the know mime types using this method.
	 * </p>
	 *
	 * @param mimeType
	 *            a mime type you want to add to the known mime types.
	 *            Duplicates are ignored.
	 * @see #isMimeTypeKnown(String mimetype)
	 */
	public static void addKnownMimeType(final String mimeType) {
		try {

			String key = getMediaType(mimeType);
			Set s = (Set) mimeTypes.get(key);
			if (s == null) {
				s = new TreeSet();
			}
			s.add(getSubType(mimeType));
			mimeTypes.put(key, s);
		} catch (MimeException ignore) {
			// A couple of entries in the magic mime file don't follow the rules
			// so ignore them
		}
	}

	/**
	 * Add a MimeDetector to the MimeDetector registry.
	 * MimeDetector(s) are effectively singletons as they are keyed against their
	 * fully qualified class name.
	 * @param mimeDetector. This must be an AbstractMimeDetector class and not
	 * a MimeUtil interface. This enforces that all custom MimeDetector(s) extend
	 * the AbstractMimeDetector rather than just implementing the MimeDetector interface.
	 * @see MimeDetector
	 * @see MimeDetector
	 */
	public static void addMimeDetector(final MimeDetector mimeDetector) {
		mimeUtilMimeDetector.addMimeDetector(mimeDetector);
	}

	/**
	 * Get the extension part of a file name defined by the file parameter.
	 *
	 * @param file
	 *            a file object
	 * @return the file extension or null if it does not have one.
	 */
	public static String getExtension(final File file) {
		return MimeUtil.getExtension(file.getName());
	}

	/**
	 * Get the extension part of a file name defined by the fileName parameter.
	 * There may be no extension or it could be a single part extension such as
	 * .bat or a multi-part extension such as tar.gz
	 *
	 * @param fileName
	 *            a relative or absolute path to a file
	 * @return the file extension or null if it does not have one.
	 */
	public static String getExtension(final String fileName) {
		// Remove any path element from this name
		File file = new File(fileName);
		String fname = file.getName();
		if (fname == null || fname.indexOf(".") < 0) {
			return "";
		}
		return fname.substring(fname.indexOf(".") + 1);
	}

	/**
	 * Get the first in a comma separated list of mime types. Useful when using
	 * extension mapping that can return multiple mime types separate by commas
	 * and you only want the first one.
	 *
	 * @param mimeTypes
	 *            comma separated list of mime types
	 * @return first in a comma separated list of mime types or null if the mimeTypes string is null or empty
	 */
	public static MimeType getFirstMimeType(final String mimeTypes) {
		if (mimeTypes != null && mimeTypes.trim().length() != 0) {
			return new MimeType(mimeTypes.split(",")[0].trim());
		}
		return null;
	}

	/**
	 * Utility method to get the major or media part of a mime type i.e. the bit before
	 * the '/' character
	 *
	 * @param mimeType
	 *            you want to get the media part from
	 * @return media type of the mime type
	 * @throws MimeException
	 *             if you pass in an invalid mime type structure
	 */
	public static String getMediaType(final String mimeType)
			throws MimeException {
		return new MimeType(mimeType).getMediaType();
	}

	/**
	 *
	 * Utility method to get the quality part of a mime type. If it does not
	 * exist then it is always set to q=1.0 unless it's a wild card. For the
	 * major component wild card the value is set to 0.01 For the minor
	 * component wild card the value is set to 0.02
	 * <p>
	 * Thanks to the Apache organisation or these settings.
	 *
	 * @param mimeType
	 *            a valid mime type string with or without a valid q parameter
	 * @return the quality value of the mime type either calculated from the
	 *         rules above or the actual value defined.
	 * @throws MimeException
	 *             this is thrown if the mime type pattern is invalid.
	 */
	public static double getMimeQuality(final String mimeType) throws MimeException {
		if (mimeType == null) {
			throw new MimeException("Invalid MimeType [" + mimeType + "].");
		}
		String[] parts = mimeSplitter.split(mimeType);
		if (parts.length < 2) {
			throw new MimeException("Invalid MimeType [" + mimeType + "].");
		}
		if (parts.length > 2) {
			for (int i = 2; i < parts.length; i++) {
				if (parts[i].trim().startsWith("q=")) {
					// Get the number part
					try {
						// Get the quality factor
						double d = Double.parseDouble(parts[i].split("=")[1]
								.trim());
						return d > 1.0 ? 1.0 : d;
					} catch (NumberFormatException e) {
						throw new MimeException(
								"Invalid Mime quality indicator ["
										+ parts[i].trim()
										+ "]. Must be a valid double between 0 and 1");
					} catch (Exception e) {
						throw new MimeException(
								"Error parsing Mime quality indicator.", e);
					}
				}
			}
		}
		// No quality indicator so always assume its 1 unless a wild card is used
		if (parts[0].contains("*")) {
			return 0.01;
		} else if (parts[1].contains("*")) {
			return 0.02;
		} else {
			// Assume q value of 1
			return 1.0;
		}
	}

	/**
	 * Get a registered MimeDetector by name.
	 * @param name the name of a registered MimeDetector. This is always the fully qualified
	 * name of the class implementing the MimeDetector.
	 * @return
	 */
	public static MimeDetector getMimeDetector(final String name) {
		return mimeUtilMimeDetector.getMimeDetector(name);
	}

	/**
	 * TODO: Add description
	 * @param data
	 * @return
	 * @throws MimeException
	 */
	public static Collection getMimeTypes(final byte [] data) throws MimeException
	{
		return getMimeTypes(data, UNKNOWN_MIME_TYPE);
	}

	/**
	 * TODO: Add desription
	 * @param data
	 * @param unknownMimeType
	 * @throws MimeException
	 */
	public static Collection getMimeTypes(final byte [] data, final MimeType unknownMimeType) throws MimeException
	{
		if(log.isDebugEnabled()) {
			try {
			log.debug("Getting mime types for byte array [" + StringUtil.getHexString(data)+ "].");
			}catch(UnsupportedEncodingException uee) {
				throw new MimeException(uee);
			}
		}
		return mimeUtilMimeDetector.getMimeTypes(data, unknownMimeType);
	}

	/**
	 * Get all of the matching mime types for this file object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the default UNKNOWN_MIME_TYPE
	 * @param file the File object to detect.
	 * @return collection of matching MimeType(s)
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final File file) throws MimeException
	{
		return getMimeTypes(file, UNKNOWN_MIME_TYPE);
	}

	/**
	 * Get all of the matching mime types for this file object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the unknownMimeType passed in.
	 * @param file the File object to detect.
	 * @param unknownMimeType.
	 * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
	 * contain the passed in parameter unknownMimeType
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final File file, final MimeType unknownMimeType) throws MimeException
	{
		if(log.isDebugEnabled()) {
			log.debug("Getting mime types for file [" + file.getAbsolutePath() + "].");
		}
		return mimeUtilMimeDetector.getMimeTypes(file, unknownMimeType);
	}

	/**
	 * Get all of the matching mime types for this InputStream object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the default UNKNOWN_MIME_TYPE
	 * @param in InputStream to detect.
	 * @return
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final InputStream in) throws MimeException
	{
		return getMimeTypes(in, UNKNOWN_MIME_TYPE);
	}

	/**
	 * Get all of the matching mime types for this InputStream object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the unknownMimeType passed in.
	 * @param in the InputStream object to detect.
	 * @param unknownMimeType.
	 * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
	 * contain the passed in parameter unknownMimeType
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final InputStream in, final MimeType unknownMimeType) throws MimeException
	{
		if(log.isDebugEnabled()) {
			log.debug("Getting mime types for InputSteam [" + in + "].");
		}
		if (!in.markSupported())
			throw new MimeException("InputStream does not support mark and reset!");
		return mimeUtilMimeDetector.getMimeTypes(in, unknownMimeType);
	}

	/**
	 * Get all of the matching mime types for this file name.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the default UNKNOWN_MIME_TYPE
	 * @param fileName the name of a file to detect.
	 * @return collection of matching MimeType(s)
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final String fileName) throws MimeException
	{
		return getMimeTypes(fileName, UNKNOWN_MIME_TYPE);
	}

	/**
	 * Get all of the matching mime types for this file name .
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the unknownMimeType passed in.
	 * @param fileName the name of a file to detect.
	 * @param unknownMimeType.
	 * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
	 * contain the passed in parameter unknownMimeType
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final String fileName, final MimeType unknownMimeType) throws MimeException
	{
		if(log.isDebugEnabled()) {
			log.debug("Getting mime types for file name [" + fileName + "].");
		}
		return mimeUtilMimeDetector.getMimeTypes(fileName, unknownMimeType);
	}

	/**
	 * Get all of the matching mime types for this URLConnection object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the default UNKNOWN_MIME_TYPE
	 * @param url a URL to detect.
	 * @return collection of matching MimeType(s)
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final URLConnection url) throws MimeException
	{
		return getMimeTypes(url, UNKNOWN_MIME_TYPE);
	}

	/**
	 * Get all of the matching mime types for this URLConnection object.
	 * The method delegates down to each of the registered MimeHandler(s) and returns a
	 * normalised list of all matching mime types. If no matching mime types are found the returned
	 * Collection will contain the unknownMimeType passed in.
	 * @param url the URL to detect.
	 * @param unknownMimeType.
	 * @return the Collection of matching mime types. If the collection would be empty i.e. no matches then this will
	 * contain the passed in parameter unknownMimeType
	 * @throws MimeException if there are problems such as reading files generated when the MimeHandler(s)
	 * executed.
	 */
	public static Collection getMimeTypes(final URLConnection url, final MimeType unknownMimeType) throws MimeException
	{
		if(log.isDebugEnabled()) {
			log.debug("Getting mime types for URL [" + url + "].");
		}
		try {
			return mimeUtilMimeDetector.getMimeTypes(new BufferedInputStream(url.getInputStream()), unknownMimeType);
		}catch(Exception e) {
			throw new MimeException(e);
		}
	}

	/**
	 * Get the native byte order of the OS on which you are running. It will be
	 * either big or little endian. This is used internally for the magic mime
	 * rules mapping.
	 *
	 * @return ByteOrder
	 */
	public static ByteOrder getNativeOrder() {
		return MimeUtil.nativeByteOrder;
	}

	/**
	 * Gives you the best match for your requirements.
	 * <p>
	 * You can pass the accept header from a browser request to this method
	 * along with a comma separated list of possible mime types returned from
	 * say getExtensionMimeTypes(...) and the best match according to the accept
	 * header will be returned.
	 * </p>
	 * <p>
	 * The following is typical of what may be specified in an HTTP Accept
	 * header:
	 * </p>
	 * <p>
	 * Accept: text/xml, application/xml, application/xhtml+xml,
	 * text/html;q=0.9, text/plain;q=0.8, video/x-mng, image/png, image/jpeg,
	 * image/gif;q=0.2, text/css, *&#47;*;q=0.1
	 * </p>
	 * <p>
	 * The quality parameter (q) indicates how well the user agent handles the
	 * MIME type. A value of 1 indicates the MIME type is understood perfectly,
	 * and a value of 0 indicates the MIME type isn't understood at all.
	 * </p>
	 * <p>
	 * The reason the image/gif MIME type contains a quality parameter of 0.2,
	 * is to indicate that PNG & JPEG are preferred over GIF if the server is
	 * using content negotiation to deliver either a PNG or a GIF to user
	 * agents. Similarly, the text/html quality parameter has been lowered a
	 * little, to ensure that the XML MIME types are given in preference if
	 * content negotiation is being used to serve an XHTML document.
	 * </p>
	 *
	 * @param accept
	 *            is a comma separated list of mime types you can accept
	 *            including QoS parameters. Can pass the Accept: header
	 *            directly.
	 * @param canProvide
	 *            is a comma separated list of mime types that can be provided
	 *            such as that returned from a call to
	 *            getExtensionMimeTypes(...)
	 * @return the best matching mime type possible.
	 */
	public static MimeType getPreferedMimeType(String accept, final String canProvide) {
		if (canProvide == null || canProvide.trim().length() == 0) {
			throw new MimeException(
					"Must specify at least one mime type that can be provided.");
		}
		if (accept == null || accept.trim().length() == 0) {
			accept = "*/*";
		}

		// If an accept header is passed in then lets remove the Accept part
		if (accept.indexOf(":") > 0) {
			accept = accept.substring(accept.indexOf(":") + 1);
		}

		// Remove any unwanted spaces from the wanted mime types for instance
		// text/html; q=0.4
		accept = accept.replaceAll(" ", "");

		return getBestMatch(accept, getList(canProvide));
	}

	/**
	 * Get the most specific match of the Collection of mime types passed in.
	 * The Collection
	 * @param mimeTypes this should be the Collection of mime types returned
	 * from a getMimeTypes(...) call.
	 * @return the most specific MimeType. If more than one of the mime types in the Collection
	 * have the same value then the first one found with this value in the Collection is returned.
	 */
	public static MimeType getMostSpecificMimeType(final Collection mimeTypes) {
		MimeType mimeType = null;
		int specificity = 0;
		for(Iterator it = mimeTypes.iterator(); it.hasNext();) {
			MimeType mt = (MimeType)it.next();
			if(mt.getSpecificity() > specificity) {
				mimeType = mt;
			}
		}
		return mimeType;
	}

	/**
	 * Utility method to get the minor part of a mime type i.e. the bit after
	 * the '/' character
	 *
	 * @param mimeType
	 *            you want to get the minor part from
	 * @return sub type of the mime type
	 * @throws MimeException
	 *             if you pass in an invalid mime type structure
	 */
	public static String getSubType(final String mimeType)
			throws MimeException {
		return new MimeType(mimeType).getSubType();
	}

	/**
	 * Check to see if this mime type is one of the types seen during
	 * initialisation or has been added at some later stage using
	 * addKnownMimeType(...)
	 *
	 * @param mimeType
	 * @return true if the mimeType is in the list else false is returned
	 * @see #addKnownMimeType(String mimetype)
	 */
	public static boolean isMimeTypeKnown(final MimeType mimeType) {
		try {
			Set s = (Set) mimeTypes.get(mimeType.getMediaType());
			if (s == null) {
				return false;
			}
			return s.contains(mimeType.getSubType());
		} catch (MimeException e) {
			return false;
		}
	}

	/**
	 * Check to see if this mime type is one of the types seen during
	 * initialisation or has been added at some later stage using
	 * addKnownMimeType(...)
	 *
	 * @param mimeType
	 * @return true if the mimeType is in the list else false is returned
	 * @see #addKnownMimeType(String mimetype)
	 */
	public static boolean isMimeTypeKnown(final String mimeType) {
		return isMimeTypeKnown(new MimeType(mimeType));
	}

	/**
	 * Utility convenience method to check if a particular MimeType instance is actually a TextMimeType.
	 * Used when iterating over a collection of MimeType's to help with casting to enable access
	 * the the TextMimeType methods not available to a standard MimeType.
	 * @param mimeType
	 * @return true if the passed in instance is a TextMimeType
	 * @see MimeType
	 * @see TextMimeType
	 */
	public static boolean isTextMimeType(final MimeType mimeType) {
		return mimeType instanceof TextMimeType;
	}

	/**
	 * Remove a previously registered MimeDetector
	 * @param mimeDetector
	 * @return the MimeDetector that was removed from the registry else null.
	 */
	public static MimeDetector removeMimeDetector(final MimeDetector mimeDetector) {
		return mimeUtilMimeDetector.removeMimeDetector(mimeDetector);
	}

	/**
	 * Get the quality parameter of this mime type i.e. the <code>q=</code> property.
	 * This method implements a value system similar to that used by the apache server i.e.
	 * if the media type is a * then it's <code>q</code> value is set to 0.01 and if the sub type is
	 * a * then the <code>q</code> value is set to 0.02 unless a specific <code>q</code>
	 * value is specified. If a <code>q</code> property is set it is limited to a max value of 1.0
	 *
	 * @param mimeType
	 * @return the quality value as a double between 0.0 and 1.0
	 * @throws MimeException
	 */
	public static double getQuality(final String mimeType) throws MimeException
	{
		if(mimeType == null || mimeType.trim().length() == 0) {
			return 0.0;
		}

		String [] parts = mimeSplitter.split(mimeType);

		// Now check to see if a quality indicator was part of the passed in type
		if (parts.length > 2) {
			for (int i = 2; i < parts.length; i++) {
				if (parts[i].trim().startsWith("q=")) {
					// Get the number part
					try {
						// Get the quality factor
						double d = Double.parseDouble(parts[i].split("=")[1].trim());
						return d > 1.0 ? 1.0 : d;
					} catch (NumberFormatException e) {
						throw new MimeException(
								"Invalid Mime quality indicator ["
										+ parts[i].trim()
										+ "]. Must be a valid double between 0 and 1");
					} catch (Exception e) {
						throw new MimeException(
								"Error parsing Mime quality indicator.", e);
					}
				}
			}
		}
		// No quality indicator so always assume its 1 unless a wild card is used
		if (parts[0].contains("*")) {
			return 0.01;
		} else if (parts[1].contains("*")) {
			return 0.02;
		} else {
			// Assume q value of 1
			return 1.0;
		}
	}

	// Check each entry in each of the wanted lists against the entries in the
	// can provide list.
	// We take into consideration the QoS indicator
	private static MimeType getBestMatch(final String accept, final List canProvideList) {

		if (canProvideList.size() == 1) {
			// If we only have one mime type that can be provided then thats
			// what we provide even if
			// the wanted list does not contain this entry or it's the worst
			// QoS.
			// This will cover the majority of cases
			return new MimeType((String) canProvideList.get(0));
		}

		Map wantedMap = normaliseWantedMap(accept, canProvideList);

		MimeType bestMatch = null;
		double qos = 0.0;
		Iterator it = wantedMap.keySet().iterator();
		while (it.hasNext()) {
			List wantedList = (List) wantedMap.get(it.next());
			Iterator it2 = wantedList.iterator();
			while (it2.hasNext()) {
				String mimeType = (String) it2.next();
				double q = getMimeQuality(mimeType);
				String majorComponent = getMediaType(mimeType);
				String minorComponent = getSubType(mimeType);
				if (q > qos) {
					qos = q;
					bestMatch = new MimeType(majorComponent + "/" + minorComponent);
				}
			}
		}
		// Gone through all the wanted list and found the best match possible
		return bestMatch;
	}

	// Turn a comma separated string into a list
	private static List getList(final String options) {
		List list = new ArrayList();
		String[] array = options.split(",");
		for (int i = 0; i < array.length; i++) {
			list.add(array[i].trim());
		}
		return list;
	}

	// Turn a comma separated string of accepted mime types into a Map
	// based on the list of mime types that can be provided
	private static Map normaliseWantedMap(final String accept, final List canProvide) {
		Map map = new LinkedHashMap();
		String[] array = accept.split(",");

		for (int i = 0; i < array.length; i++) {
			String mimeType = array[i].trim();
			String major = getMediaType(mimeType);
			String minor = getSubType(mimeType);
			double qos = getMimeQuality(mimeType);

			if (major.contains("*")) {
				// All canProvide types are acceptable with the qos defined OR
				// 0.01 if not defined
				Iterator it = canProvide.iterator();
				while (it.hasNext()) {
					String mt = (String) it.next();
					List list = (List) map.get(MimeUtil.getMediaType(mt));
					if (list == null) {
						list = new ArrayList();
					}
					list.add(mt + ";q=" + qos);
					map.put(MimeUtil.getMediaType(mt), list);
				}
			} else if (minor.contains("*")) {
				Iterator it = canProvide.iterator();
				while (it.hasNext()) {
					String mt = (String) it.next();
					if (getMediaType(mt).equals(major)) {
						List list = (List) map.get(major);
						if (list == null) {
							list = new ArrayList();
						}
						list.add(major + "/" + getSubType(mt) + ";q="
								+ qos);
						map.put(major, list);
					}
				}

			} else {
				if (canProvide.contains(major + "/" + minor)) {
					List list = (List) map.get(major);
					if (list == null) {
						list = new ArrayList();
					}
					list.add(major + "/" + minor + ";q=" + qos);
					map.put(major, list);
				}
			}
		}
		return map;
	}
}

/**
 * <p>
 * All methods in this class that return a Collection object actually return a {@link MimeTypeHashSet} that implements both the {@link Set} and {@link Collection}
 * interfaces.
 * </p>

 * @author Steven McArdle
 *
 */
class MimeUtilMimeDetectorRegistry {

	private static Log log = LogFactory.getLog(MimeUtilMimeDetectorRegistry.class);

	private Map mimeDetectors = new TreeMap();

	MimeDetector addMimeDetector(final MimeDetector mimeDetector) {
		if(mimeDetectors.containsKey(mimeDetector.getName())) {
			log.error("MimeDetector [" + mimeDetector.getName() + "] will not be registered as a MimeDetector with this name is already registered.");
			return (MimeDetector)mimeDetectors.get(mimeDetector.getName());
		}
		if(log.isDebugEnabled()) {
			log.debug("Registering MimeDetect with name [" + mimeDetector.getName() + "] and description [" + mimeDetector.getDescription() + "]");
		}
		return (MimeDetector)mimeDetectors.put(mimeDetector.getName(), mimeDetector);
	}

	MimeDetector getMimeDetector(final String name) {
		return (MimeDetector)mimeDetectors.get(name);
	}

	Collection getMimeTypes(final byte [] data, final MimeType unknownMimeType) throws MimeException
	{
		Collection mimeTypes = new MimeTypeHashSet();
		if(data != null) {
			for(Iterator it  = mimeDetectors.values().iterator();it.hasNext();) {
				try {
					MimeDetector md = (MimeDetector)it.next();
					mimeTypes.addAll(md.getMimeTypes(data));
				}catch(UnsupportedOperationException usoe) {
					// We ignore this as it indicates that this MimeDetector does not support
					// Getting mime types from files
				}catch(Exception e) {
					log.error(e, e);
				}
			}
		}
		if(mimeTypes.isEmpty()) {
			mimeTypes.add(unknownMimeType);
		}
		if(log.isDebugEnabled()) {
			log.debug("Retrieved mime types [" + mimeTypes.toString() + "]");
		}
		return mimeTypes;
	}

	Collection getMimeTypes(final File file, final MimeType unknownMimeType) throws MimeException
	{
		Collection mimeTypes = new MimeTypeHashSet();
		for(Iterator it  = mimeDetectors.values().iterator();it.hasNext();) {
			try {
				MimeDetector md = (MimeDetector)it.next();
				mimeTypes.addAll(md.getMimeTypes(file));
			}catch(UnsupportedOperationException usoe) {
				// We ignore this as it indicates that this MimeDetector does not support
				// Getting mime types from files
			}catch(Exception e) {
				log.error(e, e);
			}
		}
		if(mimeTypes.isEmpty()) {
			mimeTypes.add(unknownMimeType);
		}
		if(log.isDebugEnabled()) {
			log.debug("Retrieved mime types [" + mimeTypes.toString() + "]");
		}
		return mimeTypes;
	}

	Collection getMimeTypes(final InputStream in, final MimeType unknownMimeType) throws MimeException
	{
		Collection mimeTypes = new MimeTypeHashSet();
		for(Iterator it  = mimeDetectors.values().iterator();it.hasNext();) {
			try {
				MimeDetector md = (MimeDetector)it.next();
				mimeTypes.addAll(md.getMimeTypes(in));
			}catch(UnsupportedOperationException usoe) {
				// We ignore this as it indicates that this MimeDetector does not support
				// Getting mime types from streams
			}catch(Exception e) {
				log.error(e, e);
			}
		}
		if(mimeTypes.isEmpty()) {
			mimeTypes.add(unknownMimeType);
		}
		if(log.isDebugEnabled()) {
			log.debug("Retrieved mime types [" + mimeTypes.toString() + "]");
		}
		return mimeTypes;
	}

	Collection getMimeTypes(final String fileName, final MimeType unknownMimeType) throws MimeException
	{
		Collection mimeTypes = new MimeTypeHashSet();
		for(Iterator it  = mimeDetectors.values().iterator();it.hasNext();) {
			try {
				MimeDetector md = (MimeDetector)it.next();
				mimeTypes.addAll(md.getMimeTypes(fileName));
			}catch(UnsupportedOperationException usoe) {
				// We ignore this as it indicates that this MimeDetector does not support
				// Getting mime types from file names
			}catch(Exception e) {
				log.error(e, e);
			}
		}
		if(mimeTypes.isEmpty()) {
			mimeTypes.add(unknownMimeType);
		}
		if(log.isDebugEnabled()) {
			log.debug("Retrieved mime types [" + mimeTypes.toString() + "]");
		}
		return mimeTypes;
	}


	MimeDetector removeMimeDetector(final MimeDetector mimeDetector) {
		if(mimeDetector == null) {
			log.error("Cannot remove NULL from MimeDetector registry.");
			return null;
		}
		if(log.isDebugEnabled()) {
			log.debug("Removing MimeDetector [" + mimeDetector.getName() + "] from registry.");
		}
		return (MimeDetector)mimeDetectors.remove(mimeDetector.getName());
	}

}