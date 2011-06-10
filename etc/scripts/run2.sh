#!/bin/sh
#
# Licensed to the Austrian Association for Software Tool Integration (AASTI)
# under one or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information regarding copyright
# ownership. The AASTI licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Script used to build the entire servicebus and run it. As opposed to
# run.sh this script does not use the maven-goal openengsb:provision to launch
# the openengsb, but rather extracts the release-zip. This way it behaves very
# much like an actual release.

echo "Be careful in using this script. It does neighter run unit tests nor an upgrade!"

cd $(dirname $0)/../..
ZIPFILE=`ls assembly/target/openengsb-*.zip 2> /dev/null`
if [ ! -e "$ZIPFILE" ]; then
	mvn install -Dmaven.test.skip
	ZIPFILE=`ls assembly/target/openengsb-*.zip 2> /dev/null`
fi
DIRNAME=`echo $ZIPFILE | sed -r s/".zip$"//`
if [ ! -e $DIRNAME ]; then
	unzip $ZIPFILE -d assembly/target
fi

KARAF_DEBUG=true \
KARAF_OPTS="-Dwicket.configuration=development" \
$DIRNAME/bin/openengsb
