#!/bin/sh
#
#   Copyright 2010 OpenEngSB Division, Vienna University of Technology
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# Helper script to generate an OpenEngSB domain. Tries to guess need variables
# based on provided input.

DEFAULT_DOMAIN="mydomain"
echo -n "Domain Name (is mydomain): "
read DOMAIN
if [ "$DOMAIN" = "" ]; then
	DOMAIN=$DEFAULT_DOMAIN
fi

DEFAULT_VERSION="1.0.0-SNAPSHOT"
echo -n "Version (is $DEFAULT_VERSION): "
read VERSION
if [ "$VERSION" = "" ]; then
	VERSION=$DEFAULT_VERSION
fi

DEFAULT_NAME_PREFIX="OpenEngSB :: Domains :: ${DOMAIN~}"
echo -n "Prefix for project names (is $DEFAULT_NAME_PREFIX): "
read NAME_PREFIX
if [ "$NAME_PREFIX" = "" ]; then
	NAME_PREFIX=$DEFAULT_NAME_PREFIX
fi

mvn archetype:generate \
	-DarchetypeGroupId="org.openengsb.archetypes" \
	-DarchetypeArtifactId="openengsb-archetypes-domain" \
	-DarchetypeVersion="$VERSION" \
	-DgroupId="org.openengsb.domains.$DOMAIN" \
	-DartifactId="openengsb-domains-$DOMAIN-parent" \
	-Dversion="$VERSION" \
	-DimplementationArtifactId="openengsb-domains-$DOMAIN-implementation" \
	-Dpackage="org.openengsb.domains.$DOMAIN" \
	-Dname="$NAME_PREFIX :: Parent" \
	-DimplementationName="$NAME_PREFIX :: Implementation"

if [ $? != 0 ]; then
	exit $?
fi

echo ""
echo "DON'T FORGET TO ADD THE DOMAIN TO THE INTEGRATION TEST PROJECT!"
echo "SUCCESS"
echo ""
