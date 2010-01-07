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

cd $(dirname $0)/../core
mvn install -DskipTests=true

cd ../workflow/drools/domains
mvn install -DskipTests=true
mvn assembly:assembly

mv target/*guvnorUpload.jar target/guvnorUpload.tmp
rm target/*.jar
mv target/guvnorUpload.tmp ../../../target/openengsb-guvnor-model.jar

echo -e "\n\nYou can find the model in the target directory of the root project."

