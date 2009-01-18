package eu.medsea.util.test;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eu.medsea.util.MimeUtil;

public class Test {

	private static boolean TEST_INPUT_STREAM = false;

	public static void main(String[] args)
	throws Exception
	{
		File fileOrDirectory;
		if (args.length < 1)
			fileOrDirectory = new File("DoesNotExist").getAbsoluteFile().getParentFile();
		else
			fileOrDirectory = new File(args[0]);

		TestStatusTotal testStatusTotal = new TestStatusTotal();
		testFileOrDirectory(fileOrDirectory, testStatusTotal);
		testStatusTotal.writeReport();
	}

	private static class TestStatusTotal {
		private long startTestTimestamp = System.currentTimeMillis();
		private long filesTestedCount = 0;
		private long filesCorrectCount = 0;
		private LinkedList filesIncorrectMimeType = new LinkedList();
		private LinkedList filesUnknownMimeType = new LinkedList();

		private long accumulatedDurationMimeUtil = 0;
		private long accumulatedDurationUnixFile = 0;

		public void addTestStatusFile(TestStatusFile testStatusFile) {
			++filesTestedCount;

			if (MimeUtil.UNKNOWN_MIME_TYPE.equals(testStatusFile.getMimeTypeMimeUtil())) {
				if (MimeUtil.UNKNOWN_MIME_TYPE.equals(testStatusFile.getMimeTypeUnixFile()))
					++filesCorrectCount;
				else
					filesUnknownMimeType.add(testStatusFile);
			}
			else {
				if (testStatusFile.getMimeTypeMimeUtil().equals(testStatusFile.getMimeTypeUnixFile()))
					++filesCorrectCount;
				else
					filesIncorrectMimeType.add(testStatusFile);
			}
		}

		public long getFilesTestedCount() {
			return filesTestedCount;
		}
		public long getFilesCorrectCount() {
			return filesCorrectCount;
		}
		public List getFilesIncorrectMimeType() {
			return Collections.unmodifiableList(filesIncorrectMimeType);
		}
		public List getFilesUnknownMimeType() {
			return Collections.unmodifiableList(filesUnknownMimeType);
		}

		public long getAccumulatedDurationMimeUtil() {
			return accumulatedDurationMimeUtil;
		}
		public void incAccumulatedDurationMimeUtil(long duration) {
			accumulatedDurationMimeUtil += duration;
		}
		public long getAccumulatedDurationUnixFile() {
			return accumulatedDurationUnixFile;
		}
		public void incAccumulatedDurationUnixFile(long duration) {
			accumulatedDurationUnixFile += duration;
		}
		public void writeReport()
		{
			try {
				long duration = System.currentTimeMillis() - startTestTimestamp;

				Writer w = new BufferedWriter(new FileWriter("TestResult.txt"));
				w.append("*** Test result - Overview ***");
				w.append("\nFlag TEST_INPUT_STREAM:       " + TEST_INPUT_STREAM);
				w.append("\nFiles tested:                 " + filesTestedCount);
				w.append("\nDuration:                     " + duration + " msec");
				w.append("\nDuration MimeUtil:            " + accumulatedDurationMimeUtil + " msec");
				w.append("\nDuration /usr/bin/file:       " + accumulatedDurationUnixFile + " msec");
				w.append("\nDuration:                     " + duration + " msec");
				w.append("\nFiles correctly identified:   " + filesCorrectCount);
				w.append("\nFiles with unknown mime-type: " + filesUnknownMimeType.size());
				w.append("\nFiles with wrong mime-type:   " + filesIncorrectMimeType.size());
				w.append("\n\n*** Test result - Details ***");
				w.append("\n\nFiles with unknown mime-type (file - mimeUtilMimeType - unixFileMimeType):");
				for (Iterator it = filesUnknownMimeType.iterator(); it.hasNext();) {
					TestStatusFile testStatusFile = (TestStatusFile) it.next();
					w.append('\n');
					w.append(testStatusFile.getFile().getAbsolutePath());
					w.append('\t');
					w.append(testStatusFile.getMimeTypeMimeUtil());
					w.append('\t');
					w.append(testStatusFile.getMimeTypeUnixFile());
				}
				w.append("\n\nFiles with wrong mime-type (file - mimeUtilMimeType - unixFileMimeType):");
				for (Iterator it = filesIncorrectMimeType.iterator(); it.hasNext();) {
					TestStatusFile testStatusFile = (TestStatusFile) it.next();
					w.append('\n');
					w.append(testStatusFile.getFile().getAbsolutePath());
					w.append('\t');
					w.append(testStatusFile.getMimeTypeMimeUtil());
					w.append('\t');
					w.append(testStatusFile.getMimeTypeUnixFile());
				}
				w.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	private static class TestStatusFile {
		private File file;
		private String mimeTypeMimeUtil;
		private String mimeTypeUnixFile;
		public TestStatusFile(File file, String mimeTypeMimeUtil, String mimeTypeUnixFile) {
			this.file = file;
			this.mimeTypeMimeUtil = mimeTypeMimeUtil;
			this.mimeTypeUnixFile = mimeTypeUnixFile;
		}

		public File getFile() {
			return file;
		}

		public String getMimeTypeMimeUtil() {
			return mimeTypeMimeUtil;
		}

		public String getMimeTypeUnixFile() {
			return mimeTypeUnixFile;
		}
	}

	private static void testFileOrDirectory(File fileOrDirectory, TestStatusTotal testStatusTotal)
	throws IOException
	{
		if (fileOrDirectory.isFile()) {
			long startMimeUtil = System.currentTimeMillis();

			String mimeType;
			if (TEST_INPUT_STREAM && fileOrDirectory.isFile()) {
				InputStream in = new BufferedInputStream(new FileInputStream(fileOrDirectory));
				try {
					mimeType = MimeUtil.getMimeType(in);
				} finally {
					in.close();
				}
			}
			else
				mimeType = MimeUtil.getMimeType(fileOrDirectory);

			testStatusTotal.incAccumulatedDurationMimeUtil(System.currentTimeMillis() - startMimeUtil);

//			if (!MimeUtil.UNKNOWN_MIME_TYPE.equals(mimeType) && !"application/directory".equals(mimeType))
//				System.out.println(fileOrDirectory.getAbsolutePath() + ": " + mimeType);

			long startUnixFile = System.currentTimeMillis();
			String mimeTypeFromUnixFile = getMimeTypeFromUnixFile(fileOrDirectory);
			testStatusTotal.incAccumulatedDurationUnixFile(System.currentTimeMillis() - startUnixFile);

//			if (mimeTypeFromUnixFile == null) // unix tool not available or didn't behave the way we expect
//				System.out.println(fileOrDirectory.getAbsolutePath() + ": " + mimeType);
//			else if (!mimeTypeFromUnixFile.equals(mimeType))
//				System.out.println("mime-type mismatch! file=" + fileOrDirectory.getAbsolutePath() + " :: MimeUtil.getMimeType()=" + mimeType + " :: mimeTypeFromUnixFile=" + mimeTypeFromUnixFile);

			testStatusTotal.addTestStatusFile(
					new TestStatusFile(
							fileOrDirectory,
							mimeType,
							mimeTypeFromUnixFile
					)
			);

			if (testStatusTotal.getFilesTestedCount() % 100 == 0) {
				System.out.println(
						"Files tested: "
						+ testStatusTotal.getFilesTestedCount()
						+ " (" + testStatusTotal.getFilesCorrectCount()
						+ " correct, "
						+ testStatusTotal.getFilesUnknownMimeType().size()
						+ " unknown, "
						+ testStatusTotal.getFilesIncorrectMimeType().size()
						+ " wrong) :: accumulatedDurations: "
						+ testStatusTotal.getAccumulatedDurationMimeUtil()
						+ " mimeUtil, "
						+ testStatusTotal.getAccumulatedDurationUnixFile()
						+ " unixFile"
				);

				if (testStatusTotal.getFilesTestedCount() % 1000 == 0)
					testStatusTotal.writeReport();
			}
		}

		if (fileOrDirectory.isDirectory()) {
			File[] files = fileOrDirectory.listFiles();
			for (int i = 0; i < files.length; ++i)
				testFileOrDirectory(files[i], testStatusTotal);
		}
	}

	private static String getMimeTypeFromUnixFile(File file)
	{
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "/usr/bin/file", "--mime-type", file.getAbsolutePath() });
			Reader r = new InputStreamReader(p.getInputStream());

			int processResult = p.waitFor();
			if (processResult != 0)
				return null;

			char[] buf = new char[2];
			StringBuilder sb = new StringBuilder();
			while (true) {
				int charsRead = r.read(buf);
				if (charsRead < 0)
					break;

				sb.append(buf, 0, charsRead);
			}
			String result = sb.toString();

			// expected format of this result is /path/to/file: mime-type-major/minor
			int lastColon = result.lastIndexOf(':');
			if (lastColon < 0)
				return null;

			result = result.substring(lastColon + 1).trim();
			return result;
		} catch (Exception e) {
			return null;
		}
	}
}
