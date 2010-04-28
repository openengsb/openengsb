#!/bin/bash
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

# First parameter of this script is the servicemix path and second parameter the maven repository path.

function usage() {
  echo Usage: $0 '[servicemix-home [maven-home]]'
  echo '  'This script will use \$SERVICEMIX_HOME and \$M2_REPO to determine where
  echo '  'servicemix and the maven repository are located if these variables are set.
  echo '  'If they aren\'t set, you have to provide the arguments.
  echo '  'If \~/.m2/repository exists the script assumes it\'s your maven-repo.
  exit 1
}

if [ $# -gt 0 ] ; then
  SERVICEMIX_HOME=$1
fi

if [ ! -d ${SERVICEMIX_HOME:-X} ] ; then
  echo Error: Can\'t find servicemix-home.
  echo Set \$SERVICEMIX_HOME or provide argument 1
  echo
  usage
fi


if [ $# -gt 1 ] ; then
  M2_REPO=$2
else
# if M2_REPO not set then guess it is ~/.m2/repository
  if [ x$M2_REPO = x ] ; then
    if [ -d ~/.m2/repository ] ; then
      M2_REPO=~/.m2/repository
    fi
  fi
fi

if [ ! -d ${M2_REPO:-X} ] ; then
  echo Error: Can\'t find maven-repo.
  echo Set \$M2_REPO or provide argument 2
  echo
  usage
fi


cd $(dirname $0)/..

smx_comp_version=`grep '<servicemix.subprojects.version>' pom.xml | sed 's! *<[^>]*> *!!g'`
openengsb_version=`grep '<version>' pom.xml | head -1 | sed 's! *<[^>]*> *!!g'`

echo Settings:
echo "  OpenEngSB Version: $openengsb_version"
echo "  Servicemix Version: $smx_comp_version"
echo "  Servicemix Home: $SERVICEMIX_HOME"
echo "  Maven Repo: $M2_REPO"
echo

cd package/all
echo Running maven:
mvn install
cd ../../

files=`find . -iname '*-installer.zip' | grep -v package/embedded/`
files="$files package/all/target/openengsb-package-all-${openengsb_version}.zip"
files="features/edb/core/target/openengsb-features-edb-core-${openengsb_version}.zip $files"
files="$M2_REPO/org/apache/servicemix/servicemix-shared/$smx_comp_version/servicemix-shared-${smx_comp_version}-installer.zip $files"

for file in $files; do
  if [ ! -f $file ] ; then
    echo Error: Could not find file $file
    exit 1
  fi
done

echo
echo Deploying:

for file in $files; do
  echo '  '`basename $file`
  cp $file $SERVICEMIX_HOME/deploy/
done

