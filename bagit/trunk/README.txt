BAGIT LIBRARY (BIL)
Version ${pom.version}
BagIt Version ${bagit.version}

DESCRIPTION:
The BAGIT LIBRARY is a software library intended to support the creation, 
manipulation, and validation of bags.  It is version aware.  The earliest
supported version is 0.95.

REQUIREMENTS:
Java 6

COMMANDLINE:
A commandline interface is provided to perform various operations on bag.
Scripts to invoke the commandline interface can be found in the bin directory.
To learn more about the supported operations, invoke the commandline interface
with no arguments.  If you encounter memory issues, the memory allocation can
be increased in the scripts found in the bin directory.

LICENSES FOR BUNDLED DEPENDENCIES:
 * JSAP - http://www.martiansoftware.com/jsap/license.html
 * Classworlds - http://classworlds.codehaus.org/license.html
 * Commons Logging - http://commons.apache.org/logging/license.html
 * Commons IO - http://commons.apache.org/io/license.html
 * Commons Codec - http://commons.apache.org/codec/license.html
 * Commons VFS - http://commons.apache.org/vfs/license.html
 * Commons HttpClient - http://hc.apache.org/httpclient-3.x/license.html
 * Commons Net - http://commons.apache.org/net/license.html
 * Commons Exec - http://commons.apache.org/exec/license.html
 * Ant - http://ant.apache.org/license.html
 * Log4j - http://logging.apache.org/log4j/1.2/license.html
 * Dom4j - http://www.dom4j.org/dom4j-1.6.1/license.html
 * Jaxen - http://fisheye.codehaus.org/browse/~raw,r=1340/trunk/jaxen/jaxen/LICENSE.txt

NOTE IF USING WITH ECLIPSE:
There is a known defect with m2eclipse (https://issues.sonatype.org/browse/MNGECLIPSE-1091)
that will cause problems with this project.  To work around the problem, in Eclipse select
the project's Properties, then Maven and unselect "Skip Maven compiler plugin when processing
resources".

RELEASE NOTES:

Changes in 3.2:
1. Fixed handling of bag-info.txt with colons in the value.
2. Added Update Completer, which updates the manifests and bag-info.txt for a modified bag.

Changes in 3.1:
1. Updates to bag.bat.
2. Added support for tolerating additional directories in bag_dir.
3. Added support for adding external bag-info.txt when creating bag or bagging-in-place
	from commandline.
4. Added support for updating tag manifests only.

Changes in 3.0:
1. Numerous changes to Bag interface for clarity, consistency, and simplification.
2. Add support for visitor pattern.  Changed Writers to use visitor.
3. Writer (formerly BagWriter), Completeter (formerly CompletionStrategy), Hole Puncher (formerly Bag.makeHoley())
	return a new Bag instead of modifying existing bag.
4. Added support for cancelling long-running operations.
5. Added support for monitoring progress of long-running operations.
5. Changed DefaultCompleter to re-use existing fixities rather than always re-generating.
6. Added multithreading of manifest generation and checking.
7. Added support for filling holey bags using http, ftp, and rsync.
8. Added support for deleting payload files by directory.
9. Added commandline support for adding directory contents to payload (as opposed to adding directory).
10. Added support for bag-in-place.
11. Improved usability of commandline interface.

Changes in 2.4:
1. Added support for getting lists of standard and non-standard fields in manifests.

Changes in 2.3:
1. Trial implementation of writer for depositing serialized bags using SWORD.
2. Trial implementation of writer for depositing unserialized bags using BOB.
3. Implementation of writer for tar gz.
4. Implementation of writer for tar bz2.
5. Refactored commandline driver.
6. Fixed bug in determining if bags are complete.
7. Added license information.
8. Add verifyPayloadManifests() and verifyTagManifests() to Bag.

Changes in 2.2:
1. Fixed bug with Window filepaths.

Changes in 2.1:
1. Changed Payload-Ossum to Payload-Oxsum.
2. Updated separator for manifests.
3. Made bag-info.txt labels case-insensitive.
4. Added additional bag-info.txt methods for Bagging-Date, Bag-Count, Bag-Size, and Payload-Oxum.
5. Changed to only include the tar-related classes from Ant, rather than the entire dependency.
6. Added version-aware handling of filepath delimiters. 


For questions or problems, contact Justin Littman (jlit@loc.gov).