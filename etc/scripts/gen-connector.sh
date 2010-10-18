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

CUR_DIR=`pwd`

DEFAULT_DOMAIN=`basename $CUR_DIR`
echo -n "Domain Name (is $DEFAULT_DOMAIN): "
read DOMAIN
if [ "$DOMAIN" = "" ]; then
	DOMAIN=$DEFAULT_DOMAIN
fi

DEFAULT_INTERFACE="${DOMAIN~}Domain"
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

DEFAULT_VERSION="1.0.0-SNAPSHOT"
echo -n "Version (is $DEFAULT_VERSION): "
read VERSION
if [ "$VERSION" = "" ]; then
	VERSION=$DEFAULT_VERSION
fi

DEFAULT_NAME="OpenEngSB :: Domains :: ${DOMAIN~} :: ${CONNECTOR~}"
echo -n "Project Name (is $DEFAULT_NAME): "
read NAME
if [ "$NAME" = "" ]; then
	NAME=$DEFAULT_NAME
fi

domainGroupId="org.openengsb.domains.$DOMAIN"
domainArtifactIdPrefix="openengsb-domains-$DOMAIN"
artifactId="$domainArtifactIdPrefix-$CONNECTOR"

mvn archetype:generate \
	-DarchetypeGroupId="org.openengsb.tooling.archetypes" \
	-DarchetypeArtifactId="openengsb-tooling-archetypes-connector" \
	-DarchetypeVersion="$VERSION" \
	-DparentArtifactId="$domainArtifactIdPrefix-parent" \
	-DdomainArtifactId="$domainArtifactIdPrefix-implementation" \
	-DartifactId="$artifactId" \
	-DgroupId="$domainGroupId" \
	-Dversion="$VERSION" \
	-DdomainInterface="$INTERFACE" \
	-Dpackage="$domainGroupId.$CONNECTOR" \
	-DparentPackage="$domainGroupId" \
	-Dname="$NAME"

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
fi

echo ""
echo "DON'T FORGET TO ADD THE CONNECTOR TO THE INTEGRATIONTEST PROJECT!"
echo "SUCCESS"
echo ""
