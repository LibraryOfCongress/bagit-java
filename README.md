# BagIt Library (BIL)
[![Travis-CI Build Status (Linux)](https://travis-ci.org/LibraryOfCongress/bagit-java.svg?branch=master)](https://travis-ci.org/LibraryOfCongress/bagit-java)
[![Appveyor Build Status (Windows)](https://ci.appveyor.com/api/projects/status/ilsnisus965d56n6?svg=true)](https://ci.appveyor.com/project/johnscancella/bagit-java)
[![CircleCI](https://circleci.com/gh/LibraryOfCongress/bagit-java/tree/master.svg?style=shield)](https://circleci.com/gh/LibraryOfCongress/bagit-java)
[![Coverage Status](https://coveralls.io/repos/github/LibraryOfCongress/bagit-java/badge.svg?branch=master)](https://coveralls.io/github/LibraryOfCongress/bagit-java?branch=master)
[![Maven Central](https://img.shields.io/badge/maven%20central-4.12.1-brightgreen.svg)](http://search.maven.org/#artifactdetails%7Cgov.loc%7Cbagit%7C4.12.1%7Cjar)
[![License](https://img.shields.io/badge/License-Public--Domain-blue.svg)](https://github.com/LibraryOfCongress/bagit-java/blob/master/LICENSE.txt)
[![javadoc.io](https://img.shields.io/badge/javadoc.io-4.12.1-blue.svg)](http://www.javadoc.io/doc/gov.loc/bagit/4.12.1)

[//]: # (see https://github.com/jirutka/maven-badges once you have deployed past 5.0-BETA on maven central so that it will automatically update)
[//]: # (see https://github.com/moznion/javadocio-badges for automatic javadoc)

## Description
The BAGIT LIBRARY is a software library intended to support the creation,
manipulation, and validation of bags. Its current version is 0.97. It is version aware with the earliest
supported version being 0.93.

## Requirements
* Java 8
* gradle (for development only)

## Support
1. The Digital Curation Google Group (https://groups.google.com/d/forum/digital-curation) is an open discussion list that reaches many of the contributors to and users of this open-source project
2. If you have found a bug please create a new issue on [the issues page](https://github.com/LibraryOfCongress/bagit-java/issues)
3. To contact a developer at the Library of Congress please email repo-dev@listserv.loc.gov
4. If you would like to contribute, please submit a [pull request](https://help.github.com/articles/creating-a-pull-request/)

## Major differences between version 5 and 4.*
##### Command Line Interface

The 5.x versions do not include a command-line interface.
Users who need a command-line utility can continue to use the latest 4.x release
([download 4.12.1](https://github.com/LibraryOfCongress/bagit-java/releases/download/v4.12.1/bagit-4.12.1.zip)
or switch to an alternative implementation such as
[bagit-python](https://github.com/LibraryOfCongress/bagit-python) or
[BagIt for Ruby](https://github.com/tipr/bagit).

##### Serialization
Starting with the 5.x versions bagit-java no longer supports directly
serializing a bag to an archive file. The examples show how to implement a
custom serializer for the
[zip](https://github.com/LibraryOfCongress/bagit-java/blob/master/src/test/java/gov/loc/repository/bagit/examples/serialization/CreateZipBagExample.java)
and
[tar](https://github.com/LibraryOfCongress/bagit-java/blob/master/src/test/java/gov/loc/repository/bagit/examples/serialization/CreateTarBagExample.java)
formats.

##### Fetching
The 5.x versions do not include a core `fetch.txt` implementation. If you need
this functionality, the
[`FetchHttpFileExample` example](https://github.com/LibraryOfCongress/bagit-java/blob/master/src/test/java/gov/loc/repository/bagit/examples/fetching/FetchHttpFileExample.java)
demonstrates how you can implement this feature with your additional application
and workflow requirements.

##### New Interfaces

The 5.x version is a complete rewrite of the bagit-java library which attempts
to follow modern Java practices and will require some changes to existing code:

### Examples of using the new bagit-java library

##### Create a bag from a folder using version 0.97
```java
Path folder = Paths.get("FolderYouWantToBag");
StandardSupportedAlgorithms algorithm = StandardSupportedAlgorithms.MD5;
boolean includeHiddenFiles = false;
Bag bag = BagCreator.bagInPlace(folder, algorithm, includeHiddenFiles);
```

##### Read an existing bag (version 0.93 and higher)
```java
Path rootDir = Paths.get("RootDirectoryOfExistingBag");
BagReader reader = new BagReader();
Bag bag = reader.read(rootDir);
```

##### Write a Bag object to disk
```java
Path outputDir = Paths.get("WhereYouWantToWriteTheBagTo");
BagWriter.write(bag, outputDir); //where bag is a Bag object
```

##### Verify Complete
```java
boolean ignoreHiddenFiles = true;
BagVerifier verifier = new BagVerifier();
verifier.isComplete(bag, ignoreHiddenFiles);
```

##### Verify Valid
```java
boolean ignoreHiddenFiles = true;
BagVerifier verifier = new BagVerifier();
verifier.isValid(bag, ignoreHiddenFiles);
```

##### Quickly verify by payload-oxum
```java
boolean ignoreHiddenFiles = true;
BagVerifier verifier = new BagVerifier();

if(verifier.canQuickVerify(bag)){
  verifier.quicklyVerify(bag, ignoreHiddenFiles);
}
```

##### Add other checksum algorithms

You only need to implement 2 interfaces:

```java
public class MyNewSupportedAlgorithm implements SupportedAlgorithm {
  @Override
  public String getMessageDigestName() {
    return "SHA3-256";
  }
  @Override
  public String getBagitName() {
    return "sha3256";
  }
}

public class MyNewNameMapping implements BagitAlgorithmNameToSupportedAlgorithmMapping {
  @Override
  public SupportedAlgorithm getMessageDigestName(String bagitAlgorithmName) {
    if("sha3256".equals(bagitAlgorithmName)){
      return new MyNewSupportedAlgorithm();
    }

    return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase());
  }
}
```

and then add the implemented `BagitAlgorithmNameToSupportedAlgorithmMapping`
class to your `BagReader` or `bagVerifier` object before using their methods.

#### Check for potential problems

The BagIt format is extremely flexible and allows for some conditions which are
technically allowed but should be avoided to minimize confusion and maximize
portability. The `BagLinter` class allows you to easily check a bag for
warnings:

```java
Path rootDir = Paths.get("RootDirectoryOfExistingBag");
BagLinter linter = new BagLinter();
List<BagitWarning> warnings = linter.lintBag(rootDir, Collections.emptyList());
```

You can provide a list of specific warnings to ignore:

```java
dependencycheckth rootDir = Paths.get("RootDirectoryOfExistingBag");
BagLinter linter = new BagLinter();
List<BagitWarning> warnings = linter.lintBag(rootDir, Arrays.asList(BagitWarning.OLD_BAGIT_VERSION);
```

## Developing Bagit-Java
Bagit-Java uses [Gradle](https://gradle.org/) for its build system. Check out the great [documentation](https://docs.gradle.org/current/userguide/userguide_single.html) to learn more.
##### Running tests and code quality checks
Inside the bagit-java root directory, run `gradle check`.
##### Uploading to maven central
1. Follow their guides
  1. http://central.sonatype.org/pages/releasing-the-deployment.html
  2. https://issues.sonatype.org/secure/Dashboard.jspa
2. Once you have access, to create an office release and upload it you should specify the version by running `gradle -Pversion=<VERSION> uploadArchives`
  1. *Don't forget to tag the repository!*

### Note if using with Eclipse
Simply run `gradle eclipse` and it will automatically create a eclipse project for you that you can import.

### Roadmap for this library
* Further refine reading and writing of bags version 0.93-0.97
* Fix bugs/issues reported with new library (on going)
