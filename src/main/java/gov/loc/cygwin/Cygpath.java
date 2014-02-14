package gov.loc.cygwin;

import static java.text.MessageFormat.format;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;

public class Cygpath
{
	// Private constructor to prevent instantiation
	private Cygpath() {}
	
	/**
	 * Returns a given path as a unix-style path by calling
	 * out to the <code>cygpath --unix</code> command line.  If the
	 * current environment is not a Windows machine, then the
	 * given path is returned unchanged.
	 * 
	 * @param path The path to be converted.
	 * @return The converted path.  If not on Windows, the same path given.
	 * @throws CygwinException If an error occurs during execution of the
	 * 		   Cygwin command.
	 * 
	 * @see OS#isFamilyWindows()
	 */
	public static String toUnix(String path) throws CygwinException
	{
		String finalPath;
		
		if (OS.isFamilyWindows())
		{
			ByteArrayOutputStream cygpathOut = new ByteArrayOutputStream();
			
			CommandLine cygPath = new CommandLine("cygpath");
			cygPath.addArgument("--unix");
			cygPath.addArgument(path);
			
			try
			{
				DefaultExecutor executor = new DefaultExecutor();
				executor.setStreamHandler(new PumpStreamHandler(cygpathOut));
				
				executor.execute(cygPath);
				finalPath = cygpathOut.toString().trim();
			}
			catch (ExecuteException e)
			{
				int exitValue = e.getExitValue();
				throw new CygwinException(format("Error when executing \"{0}\" (exit value {2}: {1}", cygPath, cygpathOut.toString().trim(), exitValue), e);
			}
			catch (IOException e)
			{
				throw new CygwinException(format("Error when executing \"{0}\": {1}", cygPath, cygpathOut.toString().trim()), e);
			}
		}
		else
		{
			finalPath = path;
		}
		
		return finalPath;
	}
}
