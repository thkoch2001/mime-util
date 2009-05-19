package eu.medsea.mimeutil.detector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.TextFileMimeDetector;
import eu.medsea.mimeutil.TextMimeType;
import eu.medsea.mimeutil.handler.TextMimeHandler;
import eu.medsea.util.EncodingGuesser;

import junit.framework.TestCase;

public class TextFileMimeDetectorTest extends TestCase {

	static {
		// We can never register or unregister the TextFileMimeDetector. This is coded in and will
		// be used only when no mime type has been returned from any other registered MimeDetector.

		// In this case there are NO other registered MimeDetectors so it acts as a default.
		// The following would result in an Exception being thrown
		// MimeUtil.registerMimeDetector("eu.medsea.mimeutil.TextFileMimeDetector");


	}

	public void setUp() {
		// We will initialise the encodings with all those supported by the JVM
		EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
	}

	public void tearDown() {
		EncodingGuesser.setSupportedEncodings(new ArrayList());
	}

	// We don't register any MimeDetector(s) so the default TextFileMimeDetector will be used

	public void testGetMimeTypesFile() {

		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/a.html")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/b-jpg.img")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/b.jpg")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/c-gif.img")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/c.gif")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/d-png.img")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e-svg.img")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e.svg")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e.xml")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e[xml]")).contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/f.tar.gz")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/log4j.properties")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/magic.mime")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/mime-types.properties")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/plaintext")).contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/plaintext.txt")).contains("text/plain"));

		// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
		// and is therefore considered to be a text file. This is a small risk with small binary files.
		assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/test.bin")).contains("text/plain"));
	}

	public void testGetMimeTypesString() {
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/a.html").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/b-jpg.img").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/b.jpg").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/c-gif.img").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/c.gif").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/d-png.img").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/e-svg.img").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/e.svg").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/e.xml").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/e[xml]").contains("text/plain"));
		assertFalse(MimeUtil.getMimeTypes("src/test/resources/f.tar.gz").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/log4j.properties").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/magic.mime").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/mime-types.properties").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/plaintext").contains("text/plain"));
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/plaintext.txt").contains("text/plain"));

		// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
		// and is therefore considered to be a text file. This is a small risk with small binary files.
		assertTrue(MimeUtil.getMimeTypes("src/test/resources/test.bin").contains("text/plain"));

	}

	public void testGetMimeTypesStringWithExtensionMimeDetector() {
		try {
			MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");

			assertTrue(MimeUtil.getMimeTypes("src/test/resources/a.html").contains("text/plain"));

			assertFalse(MimeUtil.getMimeTypes("src/test/resources/b-jpg.img").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/b.jpg").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/c-gif.img").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/c.gif").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/d-png.img").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/d.png").contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes("src/test/resources/f.tar.gz").contains("text/plain"));

			assertTrue(MimeUtil.getMimeTypes("src/test/resources/e-svg.img").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/e.svg").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/e.xml").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/e[xml]").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/log4j.properties").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/magic.mime").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/mime-types.properties").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/plaintext").contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/plaintext.txt").contains("text/plain"));

			// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
			// and is therefore considered to be a text file. This is a small risk with small binary files.
			assertTrue(MimeUtil.getMimeTypes("src/test/resources/test.bin").contains("text/plain"));


			// As the ExtensionMimeDetector should also returned a text/plain MimeType for an extension of .txt
			// lets make sure the specificity has been updated and its still a TextMimeType
			Collection mimeTypes = MimeUtil.getMimeTypes("src/test/resources/plaintext.txt");
			assertTrue(mimeTypes.contains("text/plain"));
			Collection retain = new HashSet();
			retain.add("text/plain");
			mimeTypes.retainAll(retain);
			MimeType mimeType = (MimeType)mimeTypes.iterator().next();
			assertTrue(mimeType instanceof TextMimeType);
			assertTrue(((TextMimeType)mimeType).getSpecificity() == 2);

		}finally{
			// We want this to unregister no matter what
			MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		}
	}

	public void testGetMimeTypesStream() {
		try {

			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/a.html").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/b-jpg.img").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/b.jpg").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/c-gif.img").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/c.gif").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/d-png.img").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e-svg.img").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e.svg").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e.xml").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/e[xml]").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(MimeUtil.getMimeTypes(new File("src/test/resources/f.tar.gz").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/log4j.properties").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/magic.mime").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/mime-types.properties").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/plaintext").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/plaintext.txt").toURI().toURL().openStream()).contains("text/plain"));

			// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
			// and is therefore considered to be a text file. This is a small risk with small binary files.
			assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/test.bin").toURI().toURL().openStream()).contains("text/plain"));
		}catch(Exception e) {
			fail("Should never get here");
		}
	}

	public void testGetMimeTypesStreamEnsureStreamIsReset() {
		try {
			InputStream in = (new File("src/test/resources/a.html").toURI().toURL()).openStream();
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(MimeUtil.getMimeTypes(in).contains("text/plain"));

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// Read some text from the stream so we can check that it's definitely been reset in the getMimeTypes() method
			assertEquals("<html>", br.readLine()); // This is only contained in the first line of the file so reset must have happened correctly
		}catch(Exception e) {
			fail("Should never get here");
		}
	}

	public void testAddMimeHandler() {
		TextFileMimeDetector.registerTextMimeHandler(new XMLHandler());
		TextFileMimeDetector.registerTextMimeHandler(new SVGHandler());

		// Even though the next handler would match and change the mime subType it
		// will never fire as the SVGHandler returns true from it's handle(...)
		// method so no further Handler(s) will fire
		TextFileMimeDetector.registerTextMimeHandler(new NeverFireHandler());

		Collection c = MimeUtil.getMimeTypes("src/test/resources/e.xml");
		assertTrue(c.size() == 1);
		assertTrue(c.contains("text/xml"));

		c = MimeUtil.getMimeTypes("src/test/resources/e.svg");
		assertTrue(c.size() == 1);
		assertTrue(c.contains("image/svg+xml"));
	}

	class XMLHandler implements TextMimeHandler {

		public boolean handle(TextMimeType mimeType, String content) {
			if(content.startsWith("<?xml")) {
				mimeType.setMimeType(new MimeType("text/xml"));

				// Now lets find the encoding if possible
				int index = content.indexOf("encoding=\"");
				if(index != -1) {
					int endindex = content.indexOf("\"", index+10);
					mimeType.setEncoding(content.substring(index+10, endindex));
					// return true; we don't want to say we have handled this so other handlers can better determine the actual type of XML
				}
			}
			return false;
		}
	}

	class SVGHandler implements TextMimeHandler {
		public boolean handle(TextMimeType mimeType, String content) {
			if(mimeType.equals(new MimeType("text/xml"))) {
				if(content.contains("<svg  ")) {
					mimeType.setMimeType(new MimeType("image/svg+xml"));
					return true;
				}
			}
			return false;
		}
	}

	class NeverFireHandler implements TextMimeHandler {
		public boolean handle(TextMimeType mimeType, String content) {
			if("svg+xml".equals(mimeType.getSubType())) {
				mimeType.setMediaType("very-funny");
			}
			return false;
		}
	}
}
