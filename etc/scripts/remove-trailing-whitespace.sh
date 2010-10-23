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

function trim_file(){
	SRC=$1
	echo $SRC
	if [ $SRC ]; then
		cp $SRC $SRC.orig
		mode=`stat --format=%a $SRC`
		cat $SRC | sed -r s/"[[:space:]]+$"// > $SRC.new
		chmod $mode $SRC.new
		mv $SRC.new $SRC
	fi
}

function trim_all(){
	PATTERN=$1
	find . -iname $PATTERN | egrep -v "^.$" | egrep -v "./.git" |\
	while read SRC; do
		#file=`echo $SRC | sed s/"^\.\/"//`
		echo $SRC
		file $SRC | grep directory > /dev/null
		if [ $? != 1 ]; then
			continue
		fi
		trim_file "$SRC"
	done
}


trim_all '*.java'
trim_all '*.xml'

#trim_file 'pom.xml'
#trim_file 'domains/pom.xml'
status=0
if [ $(find -iname '*.orig' | wc -l) != "0" ]; then
	status=1
fi

find -iname '*.orig' -delete

echo "mod: $status"
exit $status
