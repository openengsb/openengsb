====
    Licensed to the Austrian Association for Software Tool Integration (AASTI)
    under one or more contributor license agreements. See the NOTICE file
    distributed with this work for additional information regarding copyright
    ownership. The AASTI licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file except in compliance
    with the License. You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

How to build
==========================
* Install JDK 6 or higher

  You can install either [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or
[OpenJDK](http://openjdk.java.net/install/index.html) depending on the OS you use.

* Install [Maven 3 or higher](http://maven.apache.org/download.html)

  Be sure to follow the provided [installation instructions](http://maven.apache.org/download.html#Installation)

* configure **JAVA_HOME** and **PATH** environment variables

  make sure the JAVA_HOME environment variable points to the path of your JDK installation and that both **javac** and
  **mvn** are available in your PATH-variable

* Make sure you have at least 500MB of free space in your home-directory

  By default all external dependencies required for the build are downloaded to your home-directory
  ($HOME/.m2/repository or %HOME%\.m2\repository).
  You can configure the path of this directory as by following the
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

A distribution of the framework containing all domains and connectors that are developed and maintained by the
OpenEngSB Team, have a look at https://github.com/openengsb/openengsb.


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
The detailed developer and user documentation is located at http://openengsb.org/manual/index.html.

