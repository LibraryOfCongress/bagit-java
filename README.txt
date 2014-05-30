BAGIT LIBRARY (BIL)
Version ${pom.version}
BagIt Version ${bagit.version}

DESCRIPTION:
The BAGIT LIBRARY is a software library intended to support the creation, 
manipulation, and validation of bags.  It is version aware.  The earliest
supported version is 0.93.

REQUIREMENTS:
Java 6

COMMANDLINE:
A commandline interface is provided to perform various operations on bag.
Scripts to invoke the commandline interface can be found in the bin directory.
To learn more about the supported operations, invoke the commandline interface
with no arguments.  If you encounter memory issues, the memory allocation can
be increased in the scripts found in the bin directory.

NOTE IF USING WITH ECLIPSE:
There is a known defect with m2eclipse (https://issues.sonatype.org/browse/MNGECLIPSE-1091)
that will cause problems with this project.  To work around the problem, in Eclipse select
the project's Properties, then Maven and unselect "Skip Maven compiler plugin when processing
resources".

FILENAMES WITH BACKSLASHES (\):
The BagIt specification requires that the only valid path separator is the forward slash /. Thus, a
backslash (\) in a file name is completely legal.  However, due to a shortcoming in Commons VFS
backslashes are supported by BIL.  Given platform compatability issues, this is not necessarily
a bad thing.

RELEASE NOTES:

Changes in 4.10.0:
1. Fixes defect in SimpleResult.add*Message() methods.
2. Reports an error when verifying payloads for a bag with no payload manifests.

Changes in 4.9.0:
1. Bug fix for normalization conversion between stored manifest filenames and filenames on disk.
2. Fixed bug in zip compression.
3. Upgraded numerous dependencies.
4. Removed all LC-specific configuration from pom.
5. Added .travis.yml for integration with travis-ci (http://travis-ci.com/).
6. Fixed problems so that compiles under Java 8.

Changes in 4.8.1:
1. Bug fix to FileSystemWriter to handle files that did do not already exist.
2. Bug fix to SizeHelper.

Changes in 4.8:
1. Added additional convenience method to FileHelper.

Changes in 4.7:
1. Handle NPE when encountering improperly formatted bag-info.txt.
2. Added progress reports to bag fetch. 

Changes in 4.6:
1. Added tag file fetch-progress.txt to support progressive fetch and verification.  The file 
	will be removed once the bag is successfully fetched.  In BagFetcher, added verification
	of a fetched file against the checksum in the manifest file.  Added switch verify to fill 
	holey bag and fetch remote bag command-line operations.  This option enables bag verification 
	before the fetch is resumed.
	
Changes in 4.5.1:
1. Added missing braces to conditional blocks in ValidHoleyBagVerifier.  This caused false
	error message when verifying a holey bag.

Changes in 4.5:
1. Adds support for bag-info.txt values that contain line terminators.

Changes in 4.4:
1. Attempt to correct for unicode normalization form in filepaths. Note: Java has
    problems dealing with differences in unicode normalization form 
    (http://www.unicode.org/reports/tr15/tr15-23.html) in filepaths.
    In particular, it is sometimes the case that a java.io.File is that produced
    by java.io.File.listFiles() will fail java.io.File.exists(). This attempts to
    correct for this by trying java.io.File.exists() using different normalization
    forms for the filepath.
2. Improves error handling and logging in FileSystemHelper and TempFileHelper.
3. Added support for writing by copying and moving to FileSystemHelper.  The default
	is now to copy (instead of writing the stream exposed by BagFile).  Added support
	for specifying move in CommandLineBagDriver (--move).
4. Added setUsername()/setPassword() interfaces to all bag fetchers to support
	concurrent clients; each client's credentials will be local to each instance of bag fetcher. 	

Changes in 4.3.1:
1. Changes to pom for maven 3.
2. Fix for NPE in SimpleMessage.

Changes in 4.3:
1. To enhance ability to machine-process SimpleResult, refactored to allow the
    recording of codes, with subject and objects.
2. Removed BagVerifyResult.
3. Fixes defect with zipped bags on Windows.
4. Fixes NPE when splitting bag by size without a bag-info.txt.
5. Added support for limiting additions, updates, and deletes from tag manifests
    using TagManifestVerifierCompleter.
6. Added UpdatePayloadOxumCompleter and added support to commandline driver.
7. Added PayloadOxumVerifier.

Changes in 4.2:
1. Changed most interfaces to extends Closeable instead of declaring own close() method.
2. Improved safety of stream closing throughout code.

Changes in 4.1:
1. Fixes excessive logging.

Changes in 4.0:
1. Added support for BagIt 0.97. The significant change is allowing tag directories. (Note that operations
	are version aware, meaning pre-0.97 bags do not allow tag directories.)
2. Removes Commons VFS.
3. Deprecates support for reading/writing tar, tar bz2, and tar gz.
4. Deprecates support for transferring SWORD and BOB.
5. Added close() method to Bag for closing IO resources. 
6. Clarified logging messages for CompleteVerifier and ValidVerifier.
7. Changed so that bagging-in-place throws an exception if a prebag contains a data directory and tag directories
	for pre-0.97 bags.
8. Added support for fail modes when performing verification:
    * FAIL_FAST:  Fail on first error.
    * FAIL_SLOW:  Fail at end of verification.
    * FAIL_STEP:  Fail after each step of verification. A step is a set of like verification operations.
    * FAIL_STAGE:  Fail after each stage of verification. A stage is a set of logically grouped
			verification operations. For example, when validating a bag, all of the operations to verify
			that a bag is complete is a stage.
9. Added support for compressing zip files.
10. Replaced Commons Httpclient with Apache HttpComponents.

Changes in 3.13:
1. Added support for keeping empty directories when bagging in place.
2. Increased default MAXMEM to 1024m.
3. Added verbose console and log progress reporting to CommandLineBagDriver.
4. Added support for ignoring symbolic links when verifying a bag on a file system.
5. Upgraded to Commons IO 2.0.1.

Changes in 3.12:
1. Added support to CompleteVerifier, PreBag, and BagFactory to ignore specified directories (e.g., lost+found).

Changes in 3.11:
1. Added support to FileSystemWriter to only write files that had mismatch between
	manifest and files on disk.

Changes in 3.10:
1. Added additional list methods to BagInfoTxt.
2. Added chaining completer.
3. Changed completers to not create empty payload manifests.
4. Added support to FileSystemWriter to only write tag files.

Changes in 3.9:
1. Change to licensing terms.
2. Minor modifications to support for splitting bags.

Changes in 3.8:
1. Added support for splitting bags.
2. Added support for resuming fetches of bags.
3. Improved support for filepaths with encoding.

Changes in 3.7.1:
1. Fixed defect in the writing of repeated fields in bag-info.txt.
2. Fixed defect in adding a list of values to bag-info.txt.

Changes in 3.7:
1. Added option to limit added, updated, and deleted files in UpdateCompleter.
2. Added support for repeating fields in bag-info.txt.  The existing Map interface was extended,
    not changed.

Changes in 3.6:
1. Fixed bug with HolePunchers handling of filepaths with spaces.
2. Fixed bug which caused the FileSystem Writer to delete empty directories.
3. Added option for FileSystem Writer to ignore nfs temp files since they can't be deleted.

Changes in 3.5:
1. Fixed bug with support for specifying a manifest delimeter.
2. Added missing files to source zip.
3. Added results log and output for retrieve and fill holey operations.
4. Fixed bug with handling of holey bags missing fetch.txt.
5. Set FTP data transfer sockets timeout.

Changes in 3.4:
1. Fixed critical bug that disallowed payload files to have tag manifest names.
2. Changed logging so each invocation produces a unique log file.
3. Added a new results log written to working directory for failed verification commandline operations.
4. Reduced output to System.out when invoking commandline.
5. Added support for reporting BIL version number.

Changes in 3.3:
1. Added support for HTTPS, including lax certificate handling via the --relaxssl option.
2. Fixed problems with the console authenticator.
3. Changed socket timeout from infinity to 20 seconds for http fetches.
4. Made adding data to payload progress monitorable and cancellable (AddFilesToPayloadOperation)
5. Made whitespace used in creating manifests configurable.
6. Smarter handling of relative paths in manifests. 

Changes in 3.2:
1. Fixed handling of bag-info.txt with colons in the value.
2. Added Update Completer, which updates the manifests and bag-info.txt for a modified bag.
3. Added support for retrieving a bag exposed by a web server without first having a local
	holey bag.
4. Added support for BIL versions 0.93 and 0.94.
5. Changed default number of spaces in manifests to 2.

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


For questions or problems, please post to the loc-xferutils mailing list at https://lists.sourceforge.net/lists/listinfo/loc-xferutils-mail.
For further assistance, contact Justin Littman (jlit@loc.gov).
