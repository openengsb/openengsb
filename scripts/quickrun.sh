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

# Script used to build the entire servicebus and run it directly from maven. This 
# script actually using the jbi:servicemix maven command therefore. In future
# version, when the project is embedded in an webserver this script is for
# change.

echo "Be careful in using this script. It does neighter run unit tests nor an upgrade!"

cd $(dirname $0)/..
mvn install -o -Dmaven.test.skip=true
scripts/deploy.sh $1 $2

