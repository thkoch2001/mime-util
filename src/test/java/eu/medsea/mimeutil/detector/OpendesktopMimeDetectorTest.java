package eu.medsea.mimeutil.detector;

import java.io.File;

import junit.framework.TestCase;

public class OpendesktopMimeDetectorTest extends TestCase {

	private OpendesktopMimeDetector mimeDetector = new OpendesktopMimeDetector();

	public void testGetDescription() {
		assertEquals(mimeDetector.getDescription(), "Resolve mime types for files and streams using the Opendesktop shared mime.cache file. Version [1.1].");
	}

	public void testGetMimeTypesFile() {

		assertEquals("[text/plain]", mimeDetector.getMimeTypesFile(new File("abc.txt")).toString());
		assertEquals("[text/x-makefile]", mimeDetector.getMimeTypesFile(new File("makefile")).toString());
		assertEquals("[text/x-makefile]", mimeDetector.getMimeTypesFile(new File("Makefile")).toString());
		assertEquals("[image/x-win-bitmap]", mimeDetector.getMimeTypesFile(new File("x.cur")).toString());
		assertEquals("[application/vnd.ms-tnef]", mimeDetector.getMimeTypesFile(new File("winmail.dat")).toString());
		assertEquals("[text/x-troff-mm]", mimeDetector.getMimeTypesFile(new File("abc.mm")).toString());
		assertEquals("[text/x-readme]", mimeDetector.getMimeTypesFile(new File("README")).toString());
		assertEquals("[video/x-anim]", mimeDetector.getMimeTypesFile(new File("abc.anim5")).toString());
		assertEquals("[video/x-anim]", mimeDetector.getMimeTypesFile(new File("abc.animj")).toString());
		assertEquals("[text/x-readme]", mimeDetector.getMimeTypesFile(new File("READMEFILE")).toString());
		assertEquals("[text/x-readme]", mimeDetector.getMimeTypesFile(new File("READMEanim3")).toString());
		assertEquals("[text/x-log]", mimeDetector.getMimeTypesFile(new File("README.log")).toString());
		assertEquals("[text/x-readme]", mimeDetector.getMimeTypesFile(new File("README.file")).toString());
		assertEquals("[application/x-compress]", mimeDetector.getMimeTypesFile(new File("README.Z")).toString());
		assertEquals("[]", mimeDetector.getMimeTypesFile(new File("READanim3")).toString());
	}
}
