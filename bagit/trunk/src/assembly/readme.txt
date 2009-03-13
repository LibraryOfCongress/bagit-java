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
with no arguments.

LICENSES FOR BUNDLED DEPENDENCIES:
 * JSAP - http://www.martiansoftware.com/jsap/license.html
 * Classworlds - http://classworlds.codehaus.org/license.html
 * Commons Logging - http://commons.apache.org/logging/license.html
 * Commons IO - http://commons.apache.org/io/license.html
 * Commons Codec - http://commons.apache.org/codec/license.html
 * Commons VFS - http://commons.apache.org/vfs/license.html
 * Commons HttpClient - http://hc.apache.org/httpclient-3.x/license.html
 * Ant - http://ant.apache.org/license.html
 * Log4j - http://logging.apache.org/log4j/1.2/license.html
 * Dom4j - http://www.dom4j.org/dom4j-1.6.1/license.html
 * Jaxen - http://fisheye.codehaus.org/browse/~raw,r=1340/trunk/jaxen/jaxen/LICENSE.txt
 
RELEASE NOTES:
Changes in 2.3:
1. Trial implementation of writer for depositing serialized bags using SWORD.
2. Trial implementation of writer for depositing unserialized bags using BOB.
3. Implementation of writer for tar gz.
4. Implementation of writer for tar bz2.
5. Refactored commandline driver.
6. Fixed bug in determining if bags are complete.

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