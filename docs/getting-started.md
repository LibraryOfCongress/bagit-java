Getting started
===============

### Create a bag from a folder using version 0.97

```java
Path folder=Paths.get("FolderYouWantToBag");
    StandardSupportedAlgorithms algorithm=StandardSupportedAlgorithms.MD5;
    boolean includeHiddenFiles=false;
    Bag bag=BagCreator.bagInPlace(folder,Arrays.asList(algorithm),includeHiddenFiles);
```

### Read an existing bag (version 0.93 and higher)

```java
Path rootDir=Paths.get("RootDirectoryOfExistingBag");
    BagReader reader=new BagReader();
    Bag bag=reader.read(rootDir);
```

### Write a Bag object to disk

```java
Path outputDir=Paths.get("WhereYouWantToWriteTheBagTo");
    BagWriter.write(bag,outputDir); //where bag is a Bag object
```

### Verify Complete

```java
boolean ignoreHiddenFiles=true;
    BagVerifier verifier=new BagVerifier();
    verifier.isComplete(bag,ignoreHiddenFiles);
```

### Verify Valid

```java
boolean ignoreHiddenFiles=true;
    BagVerifier verifier=new BagVerifier();
    verifier.isValid(bag,ignoreHiddenFiles);
```

### Quickly verify by payload-oxum

```java
boolean ignoreHiddenFiles=true;
    if(BagVerifier.canQuickVerify(bag)){
    BagVerifier.quicklyVerify(bag,ignoreHiddenFiles);
    }
```

### Add other checksum algorithms

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
        if ("sha3256".equals(bagitAlgorithmName)) {
            return new MyNewSupportedAlgorithm();
        }

        return StandardSupportedAlgorithms.valueOf(bagitAlgorithmName.toUpperCase());
    }
}
```

and then add the implemented `BagitAlgorithmNameToSupportedAlgorithmMapping`
class to your `BagReader` or `bagVerifier` object before using their methods.

### Check for potential problems

The BagIt format is extremely flexible and allows for some conditions which are
technically allowed but should be avoided to minimize confusion and maximize
portability. The `BagLinter` class allows you to easily check a bag for
warnings:

```java
Path rootDir=Paths.get("RootDirectoryOfExistingBag");
    BagLinter linter=new BagLinter();
    List<BagitWarning> warnings=linter.lintBag(rootDir,Collections.emptyList());
```

You can provide a list of specific warnings to ignore:

```java
Path rootDir=Paths.get("RootDirectoryOfExistingBag");
    BagLinter linter=new BagLinter();
    List<BagitWarning> warnings=linter.lintBag(rootDir,Arrays.asList(BagitWarning.OLD_BAGIT_VERSION);
```

### Serialization

The dans-bagit-lib does not support directly
serializing a bag to an archive file. The examples show how to implement a
custom serializer for the
[zip](https://github.com/DANS-KNAW/dans-bagit-lib/blob/master/src/test/java/nl/knaw/dans/bagit/examples/serialization/CreateZipBagExample.java){:target=_blank:}
and
[tar](https://github.com/DANS-KNAW/dans-bagit-lib/blob/master/src/test/java/nl/knaw/dans/bagit/examples/serialization/CreateTarBagExample.java){:target=_blank:}
formats.

### Fetching

If you need `fetch.txt` functionality, the
[`FetchHttpFileExample` example](https://github.com/DANS-KNAW/dans-bagit-lib/blob/master/src/test/java/nl/knaw/dans/bagit/examples/fetching/FetchHttpFileExample.java){:target=_blank:}
demonstrates how you can implement this feature with your additional application
and workflow requirements.

### Internationalization

All logging and error messages have been put into a [ResourceBundle](https://docs.oracle.com/javase/7/docs/api/java/util/ResourceBundle.html){:target=_blank:}.
This allows for all the messages to be translated to multiple languages and automatically used during runtime.
