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

# This script changes the copyright header version from one year to another. Simply provide
# the actual year as first param and the next year as the second param. Finally the year have
# to be set manually in the following files:
# * NOTICE

echo "This script requires perl to run. Please read the documentatation provided in this script."

cd $(dirname $0)/../../
perl -e "s/Copyright $1 OpenEngSB/Copyright $2 OpenEngSB/g;" -pi $(find . -type f)

