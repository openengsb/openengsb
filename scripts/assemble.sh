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
cd $(dirname $0)
SCRIPT_DIR=`pwd`

# build entire project from root
cd $SCRIPT_DIR/../
mvn package -Pintegration-test,license-check,docs

# assembly product
cd package/assembly
mvn dependency:unpack
mvn assembly:assembly
mvn antrun:run

# copy assembly to root folder
cp target/*.zip $SCRIPT_DIR/../

