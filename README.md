# BagIt Library (BIL)
[![Master Branch Build Status](https://travis-ci.org/LibraryOfCongress/bagit-java.svg?branch=master)](https://travis-ci.org/LibraryOfCongress/bagit-java)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gov.loc/bagit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/gov.loc/bagit)

[![Master Branch Dependency Status](https://www.versioneye.com/user/projects/56cc84d818b27104252decc3/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56cc84d818b27104252decc3)

[![License](https://img.shields.io/badge/License-Public--Domain-blue.svg)](https://github.com/LibraryOfCongress/bagit-java/blob/master/LICENSE.txt)


## Description
The BAGIT LIBRARY is a software library intended to support the creation, 
manipulation, and validation of bags. Its current version is 0.97. It is version aware with the earliest
supported version being 0.93.

## Requirements
* Java 8
* gradle

## Build
Inside the bagit-java root directory, run `gradle distZip`. This will create a .zip file under build/distributions,
bagit-\<VERSION\>.zip. To create an office release you should specify the version by running `gradle distZip -Pversion=<VERSION>` 

## Major differences between version 5 and 4.*
##### Commandline
We no longer support a command line interface for the java version of bagit. If you would like a command line interface for bagging, verifying, and other actions please check out our [bagit-python implementation](https://github.com/LibraryOfCongress/bagit-python) or the [ruby based implementation](https://github.com/tipr/bagit) 
##### Serialization
We no longer support directly serializing a bag. But if that is something you require there are plenty of great libraries that offer this capability
##### Fetching
We no longer support fetching. This is due to the various protocalls that could be involved. Again, if this is something you need, there are much better java libraries out there that you can use to fill this functionality.

### Note if using with Eclipse
Simply run `gradle eclipse` and it will automatically create a eclipse project for you that you can import.

### Examples
The "new" bagit interface is very intuitive, but here are some easy to follow examples. Instead of returning messages like in the old interface, now it throws errors so you don't have to parse messages to understand what happened.

##### Create a bag from a folder using version 0.97
```java
File folder = new File("FolderYouWantToBag");
StandardSupportedAlgorithms algorithm = StandardSupportedAlgorithms.MD5;
boolean includeHiddenFiles = false;
Bag bag = BagCreator.bagInPlace(folder, algorithm, includeHiddenFiles);
```
##### Read an existing bag (version 0.93 and higher)
```java
File rootDir = new File("RootDirectoryOfExistingBag");
Bag bag = BagReader.read(rootDir);
```
##### Write a Bag object to disk
```java
File outputDir = new File("WhereYouWantToWriteTheBagTo");
BagWriter.write(bag, outputDir); //where bag is a Bag object
```
##### Verify Complete
```java
boolean ignoreHiddenFiles = true;
BagVerifier.isComplete(bag, ignoreHiddenFiles);
```
##### Verify Valid
```java
boolean ignoreHiddenFiles = true;
BagVerifier.isValid(bag, ignoreHiddenFiles);
```
##### Quickly verify by payload-oxum
```java
boolean ignoreHiddenFiles = true;

if(BagVerifier.canQuickVerify(bag)){
  BagVerifier.quicklyVerify(bag, ignoreHiddenFiles);
}
```

### Roadmap
* Further refine reading and writing of bags version 0.93-0.97
* Integrate new proposed specification we are calling "dot bagit"
* Fix bugs/issues reported with new library (on going)
* Add extensions for more than standard supported algorithms
