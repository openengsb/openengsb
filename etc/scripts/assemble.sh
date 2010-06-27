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

# get and save path of script dir in SCRIPT_DIR
cd $(dirname $0)/../../
ASSEMBLY_ROOT_DIR=`pwd`

# build entire project from root
mvn clean install -Pintegration-test,license-check,docs

# run deploy to copy installers into fake servicemix_home
SERVICEMIX_HOME=$ASSEMBLY_ROOT_DIR/package/assembly/target/installers/

# make sure directory exists
mkdir -p $SERVICEMIX_HOME/deploy/

sh $ASSEMBLY_ROOT_DIR/etc/scripts/deploy.sh

# assembly product
cd $ASSEMBLY_ROOT_DIR/package/assembly
mvn dependency:unpack
mvn assembly:assembly

# copy assembly to root folder
mv target/*.zip $ASSEMBLY_ROOT_DIR/target

echo -e "\n\nYou can find the assembled project in the target directory of the root project."

