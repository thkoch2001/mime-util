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
package eu.medsea.util;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteOrder;

import eu.medsea.mimeutil.MimeType;

/**
 * <p>
 * The <code>MimeUtil</code> utility class is used to detect mime types from
 * either a files extension or by looking into the file at various offsets and
 * comparing or looking for certain values in the same way as the Unix
 * <code>file(1)</code> command.
 * </p>
 * <p>
 * It is important to note that mime matching is not an exact science meaning
 * that a positive match does not guarantee the returned mime type is correct.
 * It is only a best guess method of matching and should be used with this in
 * mind.
 * </p>
 * <p>
 * Both the file extension mapping and the magic mime number rules can be
 * extended by the user.
 * </p>
 * <p>
 * The extension mime mappings are loaded in the following way.
 * <ol>
 * <li>Load the properties file from the mime utility jar named
 * <code>eu.medsea.mime.mime-types.properties</code>.</li>
 * <li>Locate and load a file named <code>.mime-types.properties</code> from the
 * users home directory if one exists.</li>
 * <li>Locate and load a file named <code>mime-types.properties</code> from the
 * classpath if one exists</li>
 * <li>locate and load a file named by the JVM property
 * <code>mime-mappings</code> i.e.
 * <code>-Dmime-mappings=../my-mime-types.properties</code></li>
 * </ol>
 * Each property file loaded will add to the list of extensions. If there is a
 * clash of extension names then the last one loaded wins, this makes it
 * possible to completely change the mime types associated to a file extension
 * declared in previous property files.
 * </p>
 * <p>
 * We acquired many mappings from many different sources on the net for the
 * extension mappings. The internal list is quite large and there can be many
 * associated mime types. These may not match what you are expecting so you can
 * add the mapping you want to change to your own property file following the
 * rules above. If you provide a mapping for an extension then any previously
 * loaded mappings will be removed and only the mappings you define will be
 * returned. This can be used to map certain extensions that are incorrectly
 * returned for our environment defined in the internal property file.
 * </p>
 * <p>
 * If we have not provided a mapping for a file extension that you know the mime
 * type for you can add this to your custom property file so that a correct mime
 * type is returned for you.
 * <p>
 * The magic mime rules files are loaded in the following way.
 * <ol>
 * <li>From a JVM system property <code>magic-mime</code> i.e
 * <code>-Dmagic-mime=../my/magic/mime/rules</code></li>
 * <li>From any file named <code>magic.mime</code> that can be found on the
 * classpath</li>
 * <li>From a file named <code>.magic.mime</code> in the users home directory</li>
 * <li>From the normal Unix locations <code>/usr/share/file/magic.mime</code>
 * and <code>/etc/magic.mime</code> (in that order)</li>
 * <li>From the internal <code>magic.mime</code> file
 * <code>eu.medsea.mime.magic.mime</code> if, and only if, no files are located
 * in step 4 above.</li>
 * </ol>
 * Each rule file is appended to the end of the existing rules so the earlier in
 * the sequence you define a rule means this will take precedence over rules
 * loaded later.
 * </p>
 * <p>
 * As with the extension mappings you can add new mime mapping rules using the
 * syntax defined for the Unix magic.mime file by placing these rules in any of
 * the files or locations listed above. You can also change an existing mapping
 * rule by redefining the existing rule in one of the files listed above. This
 * is handy for some of the more sketchy rules defined in the existing Unix
 * magic.mime files.
 * </p>
 * <p>
 * When using the utility methods we always try to return a mime type even if no
 * mapping can be found. In the case of no mapping we have defined the utility
 * to return <code>application/octet-stream</code> by default. This can be
 * overridden and you can have a no-match return any mime type you define, even
 * unofficial mime types that you made up just for your application such as
 * <code>application/x-unknown-mime-type</code>. This allows your application to
 * provide special handling on a no-match such as executing a custom business
 * process.
 * </p>
 * <p>
 * We use the <code>application/directory</code> mime type to identify
 * directories. Even though this is not an official mime type it seems to be
 * well accepted on the net as an unofficial mime type so we thought it was OK
 * for us to use as well.
 * </p>
 *
 * @author Steven McArdle.
 * @deprecated Use {@link eu.medsea.mimeutil.MimeUtil} instead!
 *
 */
public class MimeUtil {
	/**
	 * Mime type used to identify no match
	 */
	public static String UNKNOWN_MIME_TYPE  = eu.medsea.mimeutil.MimeUtil.UNKNOWN_MIME_TYPE.toString();

	/**
	 * Mime type used to identify a directory
	 */
	public static final String DIRECTORY_MIME_TYPE = eu.medsea.mimeutil.MimeUtil.DIRECTORY_MIME_TYPE.toString();

	/**
	 * Get the native byte order of the OS on which you are running. It will be
	 * either big or little endian. This is used internally for the magic mime
	 * rules mapping.
	 *
	 * @return ByteOrder
	 */
	public static ByteOrder getNativeOrder() {
		return eu.medsea.mimeutil.MimeUtil.getNativeOrder();
	}

	/**
	 * Get the mime type of the data in the specified {@link InputStream}.
	 * Therefore, the <code>InputStream</code> must support mark and reset (see
	 * {@link InputStream#markSupported()}). If it does not support mark and
	 * reset, an {@link MimeException} is thrown.
	 *
	 * @param in
	 *            the stream from which to read the data.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if the specified <code>InputStream</code> does not support
	 *             mark and reset (see {@link InputStream#markSupported()}).
	 */
	public static String getMimeType(InputStream in) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(in, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * Get the mime type of a file using a path which can be relative to the JVM
	 * or an absolute path. The path can point to a file or directory location
	 * and if the path does not point to an actual file or directory the
	 * {@link #UNKNOWN_MIME_TYPE}is returned.
	 * <p>
	 * Their is an exception to this and that is if the <code>fname</code>
	 * parameter does NOT point to a real file or directory and extFirst is
	 * <code>true</code> then a match against the file extension could be found
	 * and would be returned.
	 * </p>
	 *
	 * @param fname
	 *            points to a file or directory
	 * @param extFirst
	 *            if <code>true</code> will first use file extension mapping and
	 *            then then <code>magic.mime</code> rules. If <code>false</code>
	 *            it will try to match the other way around i.e.
	 *            <code>magic.mime</code> rules and then file extension.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if while using the <code>magic.mime</code> rules there is a
	 *             problem processing the file.
	 */
	public static String getMimeType(String fname, boolean extFirst)
			throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(fname, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * This is a convenience method where the order of lookup is set to
	 * extension mapping first.
	 *
	 * @see #getMimeType(String fname, boolean extFirst)
	 */
	public static String getMimeType(String fname) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(fname, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * Get the mime type of a file using a <code>File</code> object which can be
	 * relative to the JVM or an absolute path. The path can point to a file or
	 * directory location and if the path does not point to an actual file or
	 * directory the {@link #UNKNOWN_MIME_TYPE}is returned.
	 * <p>
	 * Their is an exception to this and that is if the <code>file</code>
	 * parameter does NOT point to a real file or directory and extFirst is
	 * <code>true</code> then a match against the file extension could be found
	 * and would be returned.
	 * </p>
	 *
	 * @param file
	 *            points to a file or directory
	 * @param extFirst
	 *            if <code>true</code> will first use file extension mapping and
	 *            then then <code>magic.mime</code> rules. If <code>false</code>
	 *            it will try to match the other way around i.e.
	 *            <code>magic.mime</code> rules and then file extension.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if while using the <code>magic.mime</code> rules there is a
	 *             problem processing the file.
	 */
	public static String getMimeType(File file, boolean extFirst)
			throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(file, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * This is a convenience method where the order of lookup is set to
	 * extension mapping first.
	 *
	 * @see #getMimeType(File f, boolean extFirst)
	 */
	public static String getMimeType(File f) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(f, new MimeType(UNKNOWN_MIME_TYPE)).toString();
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
	public static String getPreferedMimeType(String accept, String canProvide) {
		return eu.medsea.mimeutil.MimeUtil.getPreferedMimeType(accept, canProvide).toString();
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
	public static double getMimeQuality(String mimeType) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeQuality(mimeType);
	}


	/**
	 * Get the mime type of a file using the <code>magic.mime</code> rules
	 * files.
	 *
	 * @param file
	 *            is a {@link File} object that points to a file or directory.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if the file cannot be parsed.
	 */
	public static String getMagicMimeType(File file) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(file, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * Utility method to get the major part of a mime type i.e. the bit before
	 * the '/' character
	 *
	 * @param mimeType
	 *            you want to get the major part from
	 * @return major component of the mime type
	 * @throws MimeException
	 *             if you pass in an invalid mime type structure
	 */
	public static String getMajorComponent(String mimeType)
			throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMediaType(mimeType);
	}

	/**
	 * Utility method to get the minor part of a mime type i.e. the bit after
	 * the '/' character
	 *
	 * @param mimeType
	 *            you want to get the minor part from
	 * @return minor component of the mime type
	 * @throws MimeException
	 *             if you pass in an invalid mime type structure
	 */
	public static String getMinorComponent(String mimeType)
			throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getSubType(mimeType);
	}

	/**
	 * Get the extension part of a file name defined by the file parameter.
	 *
	 * @param file
	 *            a file object
	 * @return the file extension or null if it does not have one.
	 */
	public static String getFileExtension(File file) {
		return eu.medsea.mimeutil.MimeUtil.getExtension(file);
	}

	/**
	 * Get the extension part of a file name defined by the fname parameter.
	 * There may be no extension or it could be a single part extension such as
	 * .bat or a multi-part extension such as tar.gz
	 *
	 * @param fileName
	 *            a relative or absolute path to a file
	 * @return the file extension or null if it does not have one.
	 */
	public static String getFileExtension(String fileName) {
		return eu.medsea.mimeutil.MimeUtil.getExtension(fileName);
	}

	/**
	 * While all of the property files and magic.mime files are being loaded the
	 * utility keeps a list of mime types it's seen. You can add other mime
	 * types to this list using this method. You can then use the
	 * isMimeTypeKnown(...) utility method to see if a mime type you have
	 * matches one that the utility already understands.
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
	// Add a mime type to the list of known mime types.
	public static void addKnownMimeType(String mimeType) {
		eu.medsea.mimeutil.MimeUtil.addKnownMimeType(mimeType);
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
	public static boolean isMimeTypeKnown(String mimeType) {
		return eu.medsea.mimeutil.MimeUtil.isMimeTypeKnown(mimeType);
	}

	/**
	 * Get the first in a comma separated list of mime types. Useful when using
	 * extension mapping that can return multiple mime types separate by commas
	 * and you only want the first one. Will return UNKNOWN_MIME_TYPE if the
	 * passed in list is null or empty.
	 *
	 * @param mimeTypes
	 *            comma separated list of mime types
	 * @return the first in a comma separated list of mime types or the
	 *         UNKNOWN_MIME_TYPE if the mimeTypes parameter is null or empty.
	 */
	public static String getFirstMimeType(String mimeTypes) {
		return eu.medsea.mimeutil.MimeUtil.getFirstMimeType(mimeTypes).toString();
	}

	/**
	 * Get the mime type of a file using file extension mappings. The file path
	 * can be a relative or absolute or can be a completely non-existent file as
	 * only the extension is important.
	 *
	 * @param file
	 *            is a <code>File</code> object that points to a file or
	 *            directory. If the file or directory cannot be found
	 *            {@link #UNKNOWN_MIME_TYPE} is returned.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if the file cannot be parsed.
	 */
	public static String getExtensionMimeTypes(File file) {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(file, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * Get the mime type of a file using file extension mappings. The file path
	 * can be a relative or absolute or can be a completely non-existent file as
	 * only the extension is important.
	 *
	 * @param fname
	 *            is a path that points to a file or directory. If the file or
	 *            directory cannot be found {@link #UNKNOWN_MIME_TYPE} is
	 *            returned.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if the file cannot be parsed.
	 */
	public static String getExtensionMimeTypes(String fname) {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(fname, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * Get the mime type of a file using the <code>magic.mime</code> rules
	 * files.
	 *
	 * @param fname
	 *            is a path location to a file or directory.
	 * @return the mime type. Never returns <code>null</code> (if the mime type
	 *         cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws MimeException
	 *             if the file cannot be parsed.
	 */
	public static String getMagicMimeType(String fname) throws MimeException {
		return eu.medsea.mimeutil.MimeUtil.getMimeTypes(fname, new MimeType(UNKNOWN_MIME_TYPE)).toString();
	}

	/**
	 * The default mime type returned by a no match i.e. is not matched in
	 * either the extension mapping or magic.mime rules is
	 * application/octet-stream. However, applications may want to treat a no
	 * match different from a match that could return application/octet-stream.
	 * This method allows you to set a different mime type to represent a no
	 * match such as a custom mime type like application/x-unknown-mime-type
	 *
	 * @param mimeType
	 *            set the default returned mime type for a no match.
	 */
	public static void setUnknownMimeType(String mimeType) {
		UNKNOWN_MIME_TYPE = mimeType;
	}
}
