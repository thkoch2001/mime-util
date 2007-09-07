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
import java.util.Map;
import java.util.Properties;

public class MimeUtil {

	private static final String [] magicMimeFileLocations = {"/etc/magic.mime", "/usr/share/file/magic.mime"};
	
	public static final String UNKNOWN_MIME_TYPE="application/x-unknown-mime-type";
	
	
	// the native byte order of the underlying OS. "BIG" or "little" Endian
	private static ByteOrder nativeByteOrder = ByteOrder.nativeOrder(); 
	private static Map mimeTypes;

    private static ArrayList mMagicMimeEntries = new ArrayList();

    // Get the native byte order of the OS on which you are running. It will be either
    // Big or little endiun
	public static ByteOrder getNativeOrder() {
		return MimeUtil.nativeByteOrder;
	}
	
	// Always use the magic.mime detection. Do not use the file extension
	public static String getMimeType(File file) {
		String mimeType = null;
		try {
			mimeType = MimeUtil.getMagicMimeType(file);
		}catch(Exception e) {
		}finally {
			if(mimeType == null) {
				mimeType = UNKNOWN_MIME_TYPE;
			}
		}
		return mimeType;
	}

	// Determin the mime type of a file either from its name using
	// file extension mapping and if that fails use the magig.mime rules.
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
	
	
	// Initialise the class in preperation for mime type detection
	static {
		mimeTypes = new Properties();
		// Load the file extension mappings from the internal property file and then 
		// from the custom property file if it can be found on the classpath
		try {
			// Load the default supplied mime types
			((Properties)mimeTypes).load(MimeUtil.class.getClassLoader().getResourceAsStream("eu/medsea/util/mime-types.properties"));
			// Load any classpath provided mime types that either extend or override the default mime types
			InputStream is =  MimeUtil.class.getClassLoader(). getResourceAsStream("mime-types.properties");
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
				System.out.println("No Customisation or Mime Types available.");
			}
		} catch (IOException ignore) {}

		// Now parse and initialise the magic.mime rules
		InputStream is = null;
		// Parse any customised magic.mime file first. This overrides the unix version
		// as any rules you define will always match before the original.
		is = MimeUtil.class.getClassLoader().getResourceAsStream("magic.mime");
		if(is != null) {
			try {
				parse(new InputStreamReader(is));
			}catch(Exception e) {
				e.printStackTrace();
				// We just won't have any customisations
			}finally {
				if(is != null) {
					try {
						is.close();
					}catch(Exception e) {} // ignore
				}
				// Set to null for the next search
				is = null;
			}
		}
		
		// Parse the UNIX magic(5) magic.mime file. 
		// This is used should the mime type not be decernable from the extension 
		// i.e. the file has no extension
		try {
			boolean found = false;
			for(int i = 0; i< magicMimeFileLocations.length; i++) {
				File f = new File(magicMimeFileLocations[i]);
				if(f.exists()) {
					is = new FileInputStream(f);
					found = true;
					break;
				}
			} 
			if (!found) {
				// Use the magic.mime that we ship
				is = MimeUtil.class.getClassLoader().getResourceAsStream("eu/medsea/util/magic.mime");
			}
			parse(new InputStreamReader(is));
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(is != null) {
				try {
					is.close();
				}catch(Exception e) {} // ignore
			}
		}
	}	
	
	public static void main(String [] args) throws IOException {
		System.out.println(mMagicMimeEntries);
		
		File dir = new File(".");
		File [] f = dir.listFiles();
		for(int i = 0; i < f.length; i++) {
			System.out.println("file : " + f[i].getCanonicalPath() + " : mimeType : " + MimeUtil.getMimeType(f[i].getCanonicalPath()));
		}
		
		
		System.out.println(MimeUtil.getMajorComponent("application/abcd"));
		System.out.println(MimeUtil.getMajorComponent("application"));
		System.out.println(MimeUtil.getMajorComponent("/application/abcd"));
		System.out.println(MimeUtil.getMajorComponent(""));
		System.out.println(MimeUtil.getMajorComponent(null));
		
		System.out.println(MimeUtil.getMinorComponent("application/abcd"));
		System.out.println(MimeUtil.getMinorComponent("application"));
		System.out.println(MimeUtil.getMinorComponent("/application/abcd"));
		System.out.println(MimeUtil.getMinorComponent(""));
		System.out.println(MimeUtil.getMinorComponent(null));
		
	}
	
	// Parse the magic.mime file
    private static void parse(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        ArrayList sequence = new ArrayList();
              	
        line = br.readLine();
        while (true) {
             if (line == null) {
                break;
            }
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#') {
            	line = br.readLine();
                continue;
            }
            sequence.add(line);
            
            // read the following lines until a line does not begin with '>' or EOF
            while(true) {
	            line = br.readLine();
	            if(line == null) {
	            	addEntry(sequence);
	            	sequence.clear();
	            	break;
	            }
	            line = line.trim();
	            if (line.length() == 0 || line.charAt(0) == '#') {
	                continue;
	            }
	            if(line.charAt(0) != '>') {
	            	addEntry(sequence);
	            	sequence.clear();
	            	break;
	            }
	            sequence.add(line);
            }
             
        }
        if(!sequence.isEmpty()) {
        	addEntry(sequence);
        }
    }
    
    private static void addEntry(ArrayList aStringArray) {
        try {
			MagicMimeEntry magicEntry = new MagicMimeEntry(aStringArray);
            mMagicMimeEntries.add(magicEntry);
		} catch (InvalidMagicMimeEntryException e) {
			// Continue on but lets print an exception so people can see there is a problem
            e.printStackTrace();
		}
    }
    
    private static String getMagicMimeType(File f) throws IOException {
    	if(f.isDirectory()) {
    		return "application/directory";
    	}
        int len = mMagicMimeEntries.size();
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        for (int i=0; i < len; i++) {
            MagicMimeEntry me = (MagicMimeEntry) mMagicMimeEntries.get(i);
            String mtype = me.getMatch(raf);
            if (mtype != null) {
                return mtype;
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
