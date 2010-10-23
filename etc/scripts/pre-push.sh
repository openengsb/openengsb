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

MVN_COMMAND="mvn clean install -Plicense-check,integration-test,checkstyle,docs"

function check_for_maven_3(){
	mvn --version | grep "^Apache Maven 3\." > /dev/null
}

function exec_mvn(){
	$MVN_COMMAND $@
}

function exec_mvn3(){
	exec_mvn -rf ":$1"
}

cd $(dirname $0)/../..

if [ -z "$1" ]; then
	exec_mvn
else
	if check_for_maven_3; then
		exec_mvn3 $1
	else
		echo "WARNING: Maven 3.0+ is required to specify an entrypoint"
		exec_mvn
	fi
fi
