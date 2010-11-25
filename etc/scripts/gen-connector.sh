#!/bin/bash
#
# Copyright 2010 OpenEngSB Division, Vienna University of Technology
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Helper script to generate an OpenEngSB connector project. Tries to guess
# needed variables based on provided input.

capitalize_ichar ()          #  Capitalizes initial character
{                            #+ of argument string(s) passed.

  string0="$@"               # Accepts multiple arguments.

  firstchar=${string0:0:1}   # First character.
  string1=${string0:1}       # Rest of string(s).

  FirstChar=`echo "$firstchar" | tr a-z A-Z`
                             # Capitalize first character.

  echo "$FirstChar$string1"  # Output to stdout.

}

CUR_DIR=`pwd`

DEFAULT_DOMAIN="domainname"
echo -n "Domain Name (is $DEFAULT_DOMAIN): "
read DOMAIN
if [ "$DOMAIN" = "" ]; then
	DOMAIN=$DEFAULT_DOMAIN
fi

CAP_DOMAIN=`capitalize_ichar ${DOMAIN}`
DEFAULT_INTERFACE="${CAP_DOMAIN}Domain"
echo -n "Domain Interface (is $DEFAULT_INTERFACE): "
read INTERFACE
if [ "$INTERFACE" = "" ]; then
	INTERFACE=$DEFAULT_INTERFACE
fi

echo -n "Connector Name: "
read CONNECTOR
if [ "$CONNECTOR" = "" ]; then
	echo "Error: Connector Name is needed"
	exit 1
fi

DEFAULT_VERSION="1.1.0-SNAPSHOT"
echo -n "Version (is $DEFAULT_VERSION): "
read VERSION
if [ "$VERSION" = "" ]; then
	VERSION=$DEFAULT_VERSION
fi

CAP_CONNECTOR=`capitalize_ichar ${CONNECTOR}`

DEFAULT_NAME="OpenEngSB :: Connector :: ${CAP_CONNECTOR}"
echo -n "Project Name (is $DEFAULT_NAME): "
read NAME
if [ "$NAME" = "" ]; then
	NAME=$DEFAULT_NAME
fi

domainGroupId="org.openengsb.domain.$DOMAIN"
domainArtifactIdPrefix="openengsb-domain-$DOMAIN"
artifactId="openengsb-connector-$CONNECTOR"
mvn archetype:generate \
	-DarchetypeGroupId="org.openengsb.tooling.archetypes" \
	-DarchetypeArtifactId="openengsb-tooling-archetypes-connector" \
	-DarchetypeVersion="$VERSION" \
	-DdomainArtifactId="$domainArtifactIdPrefix" \
	-DartifactId="$artifactId" \
    -DconnectorNameLC="$CONNECTOR" \
    -DgroupId="org.openengsb.connector" \
	-Dversion="$VERSION" \
	-DdomainInterface="$INTERFACE" \
	-Dpackage="org.openengsb.connector.$CONNECTOR" \
	-DdomainPackage="$domainGroupId" \
	-Dname="$NAME"\
    -DconnectorName="${CAP_CONNECTOR}"

if [ $? != 0 ]; then
	exit $?
fi

if [ -e "$artifactId" ]; then
	if [ ! -e "$CONNECTOR" ]; then
		echo "INFO: Renaming project from '$artifactId' to '$CONNECTOR'"
		mv "$artifactId" "$CONNECTOR"
		if [ -f "pom.xml" ]; then
			sed "s/<module>$artifactId<\/module>/<module>$CONNECTOR<\/module>/" pom.xml >pom.xml.new
			mv pom.xml.new pom.xml
		fi
	else
		echo "WARNING: Renaming of project to '$CONNECTOR' not possible, project already exists!"
	fi
else 
    echo "gnaa $artifactId"
fi

echo ""
echo "DON'T FORGET TO ADD THE CONNECTOR TO YOUR RELEASE/ASSEMBLY PROJECT!"
echo "SUCCESS"
echo ""
