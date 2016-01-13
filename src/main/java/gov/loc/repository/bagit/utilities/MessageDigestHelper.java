package gov.loc.repository.bagit.utilities;

import gov.loc.repository.bagit.Manifest.Algorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.Security;
import java.text.MessageFormat;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MessageDigestHelper
{
	static {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static final Log log = LogFactory.getLog(MessageDigestHelper.class);
	private static final int BUFFERSIZE = 65536;

	public static String generateFixity(File file, Algorithm algorithm)
	{
		try {
			log.debug("Generating fixity for " + file.toString());
			return generateFixity(new FileInputStream(file), algorithm);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String generateFixity(InputStream in, Algorithm algorithm)
	{
		try {
			MessageDigest md = MessageDigest.getInstance(algorithm.javaSecurityAlgorithm, "BC");
			byte[] dataBytes = new byte[BUFFERSIZE];
			int nread = in.read(dataBytes);
			while (nread > 0) {
				md.update(dataBytes, 0, nread);
				nread = in.read(dataBytes);
			}
			return new String(Hex.encodeHex(md.digest()));

		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			IOUtils.closeQuietly(in);
		}

	}

	public static boolean fixityMatches(InputStream in, Algorithm algorithm, String fixity)
	{
		if (fixity == null) {
			return false;
		}
		String generatedFixity = generateFixity(in, algorithm);
		log.debug(MessageFormat.format("Generated fixity is {0}.  Check fixity is {1}.",
				generatedFixity, fixity));
		if (generatedFixity.equalsIgnoreCase(fixity)) {
			return true;
		}
		return false;
	}

}
