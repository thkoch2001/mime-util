package eu.medsea.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import eu.medsea.util.log.Logger;

public class MimeUtil
{
	private static Logger logger = Logger.getLogger(MimeUtil.class);
	private static final String[] magicMimeFileLocations = {
		"/etc/magic.mime",
		"/usr/share/file/magic.mime",
		"/usr/share/mimelnk/magic"
	};

	public static final String UNKNOWN_MIME_TYPE="application/x-unknown-mime-type";
//	public static final String UNKNOWN_MIME_TYPE="application/octet-stream"; // Falling back to this is probably better than an unknown mime type (and what the /usr/bin/file command does). Hmmm... maybe we should make this behaviour configurable. Marco.


	// the native byte order of the underlying OS. "BIG" or "little" Endian
	private static ByteOrder nativeByteOrder = ByteOrder.nativeOrder();
	private static Map mimeTypes;

    private static ArrayList mMagicMimeEntries = new ArrayList();

    // Get the native byte order of the OS on which you are running. It will be either
    // Big or little endiun
	public static ByteOrder getNativeOrder() {
		return MimeUtil.nativeByteOrder;
	}

	/**
	 * Get the mime type of the data in the specified {@link InputStream}. Therefore,
	 * the <code>InputStream</code> must support mark and reset
	 * (see {@link InputStream#markSupported()}). If it does not support mark and reset,
	 * an {@link IllegalArgumentException} is thrown.
	 *
	 * @param the stream from which to read the data.
	 * @return the mime type. Never returns <code>null</code> (if the mime type cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 * @throws IllegalArgumentException if the specified <code>InputStream</code> does not support mark and reset (see {@link InputStream#markSupported()}).
	 */
	public static String getMimeType(InputStream in)
	throws IllegalArgumentException
	{
		if (!in.markSupported())
			throw new IllegalArgumentException("InputStream does not support mark and reset!");

		String mimeType = null;
		int len = mMagicMimeEntries.size();
		try {
			List matchingEntries = new LinkedList();
			for (int i=0; i < len; i++) {
				MagicMimeEntry me = (MagicMimeEntry) mMagicMimeEntries.get(i);
//				mimeType = me.getMatch(in);
//				if (mimeType != null)
//					break;
				MatchingMagicMimeEntry matchingMagicMimeEntry = me.getMatch(in);
	            if (matchingMagicMimeEntry != null) {
//	            	if (matchingMagicMimeEntry.isExactMatch())
//	            		return matchingMagicMimeEntry.getMimeType();
//	            	else
//	            		notExactlyMatchingEntries.add(matchingMagicMimeEntry);
	            	matchingEntries.add(matchingMagicMimeEntry);
	            }
			}

	        MatchingMagicMimeEntry mostSpecificMatchingEntry = getMostSpecificMatchingEntry(matchingEntries);
	        if (mostSpecificMatchingEntry != null)
	        	mimeType = mostSpecificMatchingEntry.getMimeType();

	        String textPlain = checkForTextPlain(in);
	        if (textPlain != null)
	        	return textPlain;

		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(mimeType == null) {
				mimeType = UNKNOWN_MIME_TYPE;
			}
		}

		return mimeType;
	}

	/**
	 * Get the mime type of a file. Always use the magic.mime detection. Do not use the file extension.
	 *
	 * @param file the file of which to get the mime type.
	 * @return the mime type. Never returns <code>null</code>.
	 */
	public static String getMimeType(File file) {
		return getMimeType(file, false);
	}

	/**
	 * Get the mime type of a file. If <code>tryUsingFileExtension</code> is set to <code>true</code> then we will try
	 * to use both matching from file extension and then from the magic.mime if that fails. Else we just match from the magic.mime.
	 *
	 * @param file the file of which to get the mime type.
	 * @param tryUsingFileExtension whether to try to find out the mime type by file extension. If <code>false</code>, it is only determined
	 *		by magic number. If <code>true</code>, we first try to find the name by file extension - the magic numbers are only used
	 *		if the file extension doesn't exist or cannot be resolved to a mime type.
	 * @return the mime type. Never returns <code>null</code> (if the mime type cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 */
	public static String getMimeType(File file, boolean tryUsingFileExtension) {
		String mimeType = null;
		try {
			if(tryUsingFileExtension) {
				mimeType = MimeUtil.getMimeType(file.getCanonicalPath());
			} else {
				mimeType = MimeUtil.getMagicMimeType(file);
			}
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}finally {
			if(mimeType == null) {
				mimeType = UNKNOWN_MIME_TYPE;
			}
		}
		return mimeType;
	}

	/**
	 *
	 * @return the mime type. Never returns <code>null</code> (if the mime type cannot be found, {@link #UNKNOWN_MIME_TYPE} is returned).
	 */
	// Determine the mime type of a file either from its name using
	// file extension mapping and if that fails use the magic.mime rules.
	// This requires that the name you pass is the FULL path to the file
	// You are trying to detect or the magic.mime rule detection will fail
	// as it will be unable to construct a File object without the path.
	public static String getMimeType(String fname) {
		String mimeType = null;
		if(fname == null || "".equals(fname.trim())){
			fname = "";
		}
		// Get the file extension
		String ext = MimeUtil.getFileExtension(fname);
		if(ext == null || "".equals(ext.trim())) {
			// Lets sniff this as we cannot tell from the file extention
			mimeType = MimeUtil.getMimeType(new File(fname));
		} else {
			// First try case sensitive
			mimeType = (String)mimeTypes.get(ext);
			if(mimeType == null || "".equals(mimeType.trim())) {
				//try again case insensitive (lower case)
				mimeType = (String)mimeTypes.get(ext.toLowerCase());
				if(mimeType == null || "".equals(mimeType.trim())) {
					// lets sniff it
					mimeType = MimeUtil.getMimeType(new File(fname));
				}
			}
		}
		return mimeType;
	}

	// This doesn't actually detect a mime type. What it does do is give you a best match
	// for your requirements. Lets say you get the mime type from a file and it returns
	// a comma seperated list of mime types. You want to pass this back to a browser as
	// a particular format i.e. only one of the available formats, so you pass in a list
	// of formats you will accept i.e. from the browser HTTP accept header and this method
	// will try to determine which of the available mime types best suits your needs.
	public static String getPreferedMimeType(String wanted, String canProvide) {

		// The following is typical of what may be specified in the HTTP Accept header.
		// text/xml, application/xml, application/xhtml+xml, text/html;q=0.9, text/plain;q=0.8, video/x-mng, image/png, image/jpeg, image/gif;q=0.2, text/css, */*;q=0.1
		// The quality parameter (q) indicates how well the user agent handles the MIME type.
		// A value of 1 indicates the MIME type is understood perfectly,
		// and a value of 0 indicates the MIME type isn't understood at all.
		// The reason the image/gif MIME type contains a quality parameter of 0.2, is to indicate that PNG is preferred over GIF
		// if the server is using content negotiation to deliver either a PNG or a GIF to user agents.
		// Similarly, the text/html quality parameter has been lowered a little,
		// to ensure that the XML MIME types are given in preference if content negotiation is being used to serve an XHTML document.
		if(wanted == null || wanted.trim().length() == 0) {
			wanted = "";
		}
		String [] wantedArray = wanted.split(",");
		String [] canProvideArray = canProvide.split(",");
		if(canProvideArray.length == 1) {
			// this is all we have to offer
			return canProvideArray[0].trim();
		}
		// Else lets check the rest.

		// TODO. Improve the matching. We need to check for the QoS part to determine the
		// best match. See note above
		for(int i = 0; i < wantedArray.length; i++) {
			if(wantedArray[i].trim().equals("*/*")) {
				return canProvideArray[0].trim();
			}
			for(int j = 0; j < canProvideArray.length; j++) {
				if(wantedArray[i].trim().compareToIgnoreCase(canProvideArray[j].trim()) == 0) {
					return wantedArray[i].trim();
				}
			}
		}
		// Assume a wanted type of '*/*' exists and just return the first canProvide type
		return canProvideArray[0].trim();
	}

	// Initialise the class in preparation for mime type detection
	private static void initMimeTypeProperties()
	{
		mimeTypes = new Properties();
		// Load the file extension mappings from the internal property file and then
		// from the custom property file if it can be found on the classpath
		try {
			// Load the default supplied mime types
			((Properties)mimeTypes).load(MimeUtil.class.getClassLoader().getResourceAsStream("eu/medsea/util/mime-types.properties"));
			// Load any classpath provided mime types that either extend or override the default mime types
			String mimeTypesProperties = "mime-types.properties";
			InputStream is =  MimeUtil.class.getClassLoader(). getResourceAsStream(mimeTypesProperties);
			if(is != null) {
				try {
					Properties props = new Properties();
					props.load(is);
					if(props.size() > 0) {
						System.out.println("Customisations of Mime Types are available");
						mimeTypes.putAll(props);
					}
				}finally {
					if(is != null) {
						is.close();
					}
				}
			} else {
				logger.info("No customisations of mime types available (resource \"" + mimeTypesProperties + "\" not found).");
			}
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private static void initMimeTypeMagicRules()
	{
		// Now parse and initialise the magic.mime rules
		InputStream is = null;
		// Parse any customised magic.mime file first. This overrides the unix version
		// as any rules you define will always match before the original.
		is = MimeUtil.class.getClassLoader().getResourceAsStream("magic.mime");
		if(is != null) {
			try {
				parse("resource:magic.mime", new InputStreamReader(is));
			}catch(Exception e) {
				logger.warn(e.getMessage(), e);
				// We just won't have any customisations
			}finally {
				if(is != null) {
					try {
						is.close();
					}catch(Exception e) {
						// ignore, but log in debug mode
						if (logger.isDebugEnabled())
							logger.debug(e.getMessage(), e);
					}
				}
				// Set to null for the next search
				is = null;
			}
		}

//		// Parse the UNIX magic(5) magic.mime file.
//		try {
//			boolean found = false;
//			for(int i = 0; i< magicMimeFileLocations.length; i++) {
//				File f = new File(magicMimeFileLocations[i]);
//				if(f.exists()) {
//					is = new FileInputStream(f);
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				// Use the magic.mime that we ship
//				is = MimeUtil.class.getClassLoader().getResourceAsStream("eu/medsea/util/magic.mime");
//			}
//			parse(new InputStreamReader(is));
//		} catch(Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		finally {
//			if(is != null) {
//				try {
//					is.close();
//				}catch(Exception e) {
//					// ignore, but log in debug mode
//					logger.debug(e.getMessage(), e);
//				}
//			}
//		}


		// Parse the UNIX magic(5) magic.mime files. Since there can be multiple, we have to load all of them.
		// We save, how many entries we have now, in order to fall back to our default magic.mime that we ship,
		// if no entries were read from the OS.
		int mMagicMimeEntriesSizeBeforeReadingOS = mMagicMimeEntries.size();
		for(int i = 0; i< magicMimeFileLocations.length; i++) {
			String magicMimeFileLocation = magicMimeFileLocations[i];
			List magicMimeFiles = getMagicFilesFromMagicMimeFileLocation(magicMimeFileLocation);

			for (Iterator itFile = magicMimeFiles.iterator(); itFile.hasNext(); ) {
				File f = (File) itFile.next();
				try {
					if(f.exists()) {
						is = new FileInputStream(f);
						parse(f.getAbsolutePath(), new InputStreamReader(is));
					}
				} catch(Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					if(is != null) {
						try {
							is.close();
						}catch(Exception e) {
							// ignore, but log in debug mode
							if (logger.isDebugEnabled())
								logger.debug(e.getMessage(), e);
						}
						is = null;
					}
				}
			}
		}

		if (mMagicMimeEntriesSizeBeforeReadingOS == mMagicMimeEntries.size()) {
			// Use the magic.mime that we ship
			try {
				String resource = "eu/medsea/util/magic.mime";
				is = MimeUtil.class.getClassLoader().getResourceAsStream(resource);
				parse("resource:" + resource, new InputStreamReader(is));
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(is != null) {
					try {
						is.close();
					}catch(Exception e) {
						// ignore, but log in debug mode
						if (logger.isDebugEnabled())
							logger.debug(e.getMessage(), e);
					}
					is = null;
				}
			}
		}
	}

	private static List getMagicFilesFromMagicMimeFileLocation(String magicMimeFileLocation)
	{
		List magicMimeFiles = new LinkedList();
		if (magicMimeFileLocation.indexOf('*') < 0) {
			magicMimeFiles.add(new File(magicMimeFileLocation));
		}
		else {
			int lastSlashPos = magicMimeFileLocation.lastIndexOf('/');
			File dir;
			String fileNameSimplePattern;
			if (lastSlashPos < 0) {
				dir = new File("someProbablyNotExistingFile").getAbsoluteFile().getParentFile();
				fileNameSimplePattern = magicMimeFileLocation;
			}
			else {
				String dirName = magicMimeFileLocation.substring(0, lastSlashPos);
				if (dirName.indexOf('*') >= 0)
					throw new UnsupportedOperationException("The wildcard '*' is not allowed in directory part of the location! Do you want to implement expressions like /path/**/*.mime for recursive search? Please do!");

				dir = new File(dirName);
				fileNameSimplePattern = magicMimeFileLocation.substring(lastSlashPos + 1);
			}

			if (!dir.isDirectory())
				return Collections.EMPTY_LIST;

			String s = fileNameSimplePattern.replaceAll("\\.", "\\\\.");
			s = s.replaceAll("\\*", ".*");
			Pattern fileNamePattern = Pattern.compile(s);

			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				if (fileNamePattern.matcher(file.getName()).matches())
					magicMimeFiles.add(file);
			}
		}
		return magicMimeFiles;
	}

	static {
		initMimeTypeProperties();
		initMimeTypeMagicRules();
	}

	public static void main(String [] args) throws IOException {
		System.out.println(mMagicMimeEntries);

		File dir = null;
		if(args.length == 0) {
			dir = new File(".");
		} else {
			dir = new File(args[0]);
		}
		File [] f = dir.listFiles();
		for(int i = 0; i < f.length; i++) {
			System.out.println("(Using name) file : " + f[i].getCanonicalPath() + " : mimeType : " + MimeUtil.getMimeType(f[i].getCanonicalPath()));
			System.out.println("(Using file) file : " + f[i].getCanonicalPath() + " : mimeType : " + MimeUtil.getMimeType(f[i]));
			System.out.println("-----------------------");
		}
	}

	// Parse the magic.mime file
    private static void parse(String magicFile, Reader r) throws IOException {
    	long start = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(r);
        String line;
        ArrayList sequence = new ArrayList();

        long lineNumber = 0;
        line = br.readLine();
        if (line != null) ++lineNumber;
        while (true) {
             if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
            	line = br.readLine();
            	if (line != null) ++lineNumber;
                continue;
            }
            sequence.add(line);

            // read the following lines until a line does not begin with '>' or EOF
            while(true) {
	            line = br.readLine();
	            if (line != null) ++lineNumber;
	            if(line == null) {
	            	addEntry(magicFile, lineNumber, sequence);
	            	sequence.clear();
	            	break;
	            }
	            line = line.trim();
	            if (line.length() == 0 || line.charAt(0) == '#') {
	                continue;
	            }
	            if(line.charAt(0) != '>') {
	            	addEntry(magicFile, lineNumber, sequence);
	            	sequence.clear();
	            	break;
	            }
	            sequence.add(line);
            }

        }
        if(!sequence.isEmpty()) {
        	addEntry(magicFile, lineNumber, sequence);
        }

        if (logger.isDebugEnabled())
        	logger.debug("Parsing \"" + magicFile + "\" took " + (System.currentTimeMillis() - start) + " msec.");
    }

    private static void addEntry(String magicFile, long lineNumber, ArrayList aStringArray) {
        try {
			MagicMimeEntry magicEntry = new MagicMimeEntry(aStringArray);
            mMagicMimeEntries.add(magicEntry);
		} catch (InvalidMagicMimeEntryException e) {
			// Continue on but lets print an exception so people can see there is a problem
            logger.warn(e.getClass().getName() + ": " + e.getMessage() + ": file \"" + magicFile + "\": before or at line " + lineNumber, e);
		}
    }

    private static MatchingMagicMimeEntry getMostSpecificMatchingEntry(List notExactlyMatchingEntries)
    {
    	MatchingMagicMimeEntry mostSpecificMatchingEntry = null;
        for (Iterator it = notExactlyMatchingEntries.iterator(); it.hasNext();) {
			MatchingMagicMimeEntry entry = (MatchingMagicMimeEntry) it.next();
			if (mostSpecificMatchingEntry == null)
				mostSpecificMatchingEntry = entry;
			else if (mostSpecificMatchingEntry.getSpecificity() < entry.getSpecificity())
				mostSpecificMatchingEntry = entry;
		}
        return mostSpecificMatchingEntry;
    }

    private static String checkForTextPlain(InputStream in)
    {
    	// read the first 1024 bytes of what may be a text file.
    	byte[] content = new byte[1024];

    	try {
    		in.mark(1024);
    		try {
    			int offset = 0;
    			while (true) {
    				int bytesRead = in.read(content, offset, content.length - offset);
    				if (bytesRead < 0)
    					break;

    				offset += bytesRead;
    			}
    			if (offset < content.length) {
    				byte[] tmp = new byte[offset];
    				System.arraycopy(content, 0, tmp, 0, tmp.length);
    				content = tmp;
    			}
    		} finally {
    			in.reset();
    		}
    	} catch (IOException e) {
    		logger.warn(e.getClass().getName() + ": " + e.getMessage(), e);
    		return null;
    	}

    	return checkForTextPlain(content);
    }

    private static String checkForTextPlain(RandomAccessFile raf)
    {
    	// read the first 1024 bytes of what may be a text file.
    	byte[] content = new byte[1024];

    	try {
    		raf.seek(0);
			if (raf.length() < 1024)
				content = new byte[(int)raf.length()];
			else
				content = new byte[1024];

			raf.readFully(content);
		} catch (IOException e) {
			logger.warn(e.getClass().getName() + ": " + e.getMessage(), e);
			return null;
		}

		return checkForTextPlain(content);
    }

    private static String checkForTextPlain(byte[] content)
    {
    	if (content.length == 0)
    		return "application/x-empty";

    	// TODO we should check for all valid encodings - or at least for UTF-8 - right now, we only check for ASCII

    	for (int i = 0; i < content.length; i++) {
			int b = content[i] & 0xff;
			if (b < 9)
				return null;

			if (b > 175)
				return null;
		}
    	return "text/plain";
    }

    private static String getMagicMimeType(File f) throws IOException {
    	if(f.isDirectory()) {
    		return "application/directory";
    	}
        int len = mMagicMimeEntries.size();
        RandomAccessFile raf = null;
        try {
	        raf = new RandomAccessFile(f, "r");
	        List matchingEntries = new LinkedList();
	        for (int i=0; i < len; i++) {
	            MagicMimeEntry me = (MagicMimeEntry) mMagicMimeEntries.get(i);
	            MatchingMagicMimeEntry matchingMagicMimeEntry = me.getMatch(raf);
	            if (matchingMagicMimeEntry != null) {
//	            	if (matchingMagicMimeEntry.isExactMatch())
//	            		return matchingMagicMimeEntry.getMimeType();
//	            	else
//	            		notExactlyMatchingEntries.add(matchingMagicMimeEntry);
	            	matchingEntries.add(matchingMagicMimeEntry);
	            }
//	            String mtype = me.getMatch(raf);
//	            if (mtype != null) {
//	                return mtype;
//	            }
	        }

	        MatchingMagicMimeEntry mostSpecificMatchingEntry = getMostSpecificMatchingEntry(matchingEntries);
	        if (mostSpecificMatchingEntry != null)
	        	return mostSpecificMatchingEntry.getMimeType();

	        String textPlain = checkForTextPlain(raf);
	        if (textPlain != null)
	        	return textPlain;
        } finally {
        	if(raf != null) {
        		try {
        			raf.close();
        		}catch(Exception ignore) {}
        	}
        }
        return null;
    }

    // Utility method to get the major part of a mime type
    // i.e. the bit before the '/' character
    public static String getMajorComponent(String mimeType) {
    	if(mimeType == null) {
    		return "";
    	}
    	int offset = mimeType.indexOf("/");
    	if(offset == -1) {
    		return mimeType;
    	} else {
    		return mimeType.substring(0, offset);
    	}
    }

    // Utility method to get the minor part of a mime type
    // i.e. the bit after the '/' character
    public static String getMinorComponent(String mimeType) {
    	if(mimeType == null) {
    		return "";
    	}
    	int offset = mimeType.indexOf("/");
    	if(offset == -1) {
    		return mimeType;
    	} else {
    		return mimeType.substring(offset+1);
    	}
    }

    // Utility method that gets the extension of a file from its name if it has one
    public static String getFileExtension(File file) {
    	try {
    		return MimeUtil.getFileExtension(file.getCanonicalPath());
    	}catch(IOException e) {
    		return "";
    	}
    }

    // Utility method that gets the extension of a file from its name if it has one
	public static String getFileExtension(String fileName) {
		if (fileName == null || fileName.lastIndexOf(".") < 0) {
			return "";
		}
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		// Could be that the path actually had a '.' in it so lets check
		if(extension.contains(File.separator)) {
			extension = "";
		}
		return extension;
	}
}
