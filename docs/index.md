MANUAL
======
Library with classes and functions for working with the BagIt format

DESCRIPTION
-----------
BagIt is a set of hierarchical file layout conventions designed to
support storage and transfer of arbitrary digital content. A "bag"
consists of a directory containing the payload files and other
accompanying metadata files known as "tag" files. The "tags" are
metadata files intended to facilitate and document the storage and
transfer of the bag. Processing a bag does not require any
understanding of the payload file contents, and the payload files can
be accessed without processing the BagIt metadata.

This BagIt library is a software library intended to support the creation,
manipulation, and validation of bags. Its current version is 0.97. It is version aware with the earliest
supported version being 0.93.

See: <https://datatracker.ietf.org/doc/html/rfc8493>{:target=_blank}.

This library was first developed by the [LibraryOfCongress](https://github.com/LibraryOfCongress/bagit-java/){:target=_blank:} and
forked by DANS-KNAW.

INSTALLATION
------------

To use this library in a Maven-based project:

1. Include in your `pom.xml` a declaration for the DANS maven repository:

        <repositories>
            <!-- possibly other repository declarations here ... -->
            <repository>
                <id>DANS</id>
                <releases>
                    <enabled>true</enabled>
                </releases>
                <url>https://maven.dans.knaw.nl/releases/</url>
            </repository>
        </repositories>

2. Include a dependency on this library.

        <dependency>
            <groupId>nl.knaw.dans</groupId>
            <artifactId>dans-bagit-lib</artifactId>
            <version>{version}</version> <!-- <=== FILL LIBRARY VERSION TO USE HERE -->
        </dependency>
