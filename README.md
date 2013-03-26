OpenEngSB Framework
==========================

The OpenEngSB Framework is the underlying engine of the OpenEngSB implementing and wiring together all required
concepts and tools to provide a common integration environment.

[![Build Status](https://travis-ci.org/openengsb/openengsb-framework.png?branch=master)](https://travis-ci.org/openengsb/openengsb-framework)

How to build
==========================

* Install JDK 6 or higher

  You can install [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or
  [OpenJDK](http://openjdk.java.net/install/index.html) depending on the OS you use.
  Other JVM implementations should also work, but are untested.

* Install [Maven 3 or higher](http://maven.apache.org/download.html)

  Be sure to follow the provided [installation instructions](http://maven.apache.org/download.html#Installation)

* configure **JAVA_HOME** and **PATH** environment variables

  make sure the JAVA_HOME environment variable points to the path of your JDK installation and that both **javac** and
  **mvn** are available in your PATH-variable

* Make sure you have at least 500MB of free space in your home-directory

  By default all external dependencies required for the build are downloaded to your home-directory
  ($HOME/.m2/repository or %HOME%\.m2\repository).
  You can configure the path of this directory as described in the
  [Maven Configuration Guide](http://maven.apache.org/guides/mini/guide-configuring-maven.html)

* Make sure that enough memory is available to maven during the build-process

  Therefore set the **MAVEN_OPTS** environment variable to *"-Xmx2048m -XX:MaxPermSize=512m"*.

* Run **mvn install** from the project's root directory

  This might take some time depending on your internet connection and hardware configuration (maybe about 30 min)

That's it. You can find the distributable zip-archive at *assembly/target/openengsb-framework-\<version\>.zip*.
It contains all binaries and third party dependencies required to run the openengsb-framework in Windows or *nix
environments.
To run the openengsb-framework distribution you need to extract the zip-archive and execute the corresponding
startup-script in the distribution's bin-directory (openengsb.bat for Windows, openengsb for *nix OS).

To get started with domains and connectors that are already available, have a look at
http://openengsb.org/index/download.html

Scripts
-------
Scripts which help you to create projects, run the workspace and so on are stored in a separated script folder
(etc/scripts).
Its not required to use them but they may help you with your effort.

Full Tests
----------
Before creating a pull request, run the following command:

etc/scripts/pre-push.sh (or "mvn openengsb:prePush")

Further Information
-------------------
This readme gives only the most important information for developer. General information about this project is located
at http://openengsb.org.
The detailed developer and user documentation is located at http://openengsb.org/index/documentation.html.

