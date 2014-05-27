#!/bin/bash
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

# This script is used in the Travis-CI build environment to execute the
# integration test, and works around the issues of the integration test
# environment where tests would sporadically fail due to race conditions.
# The script finds all Java files Named *IT.java and executes them individually
# in a maven command. A failing test (possibly due to a race condition), will
# be retried a specified amount of times.

ITEST_DIR=./itests/
DEFAULT_RETRY=3;

run-test-retry() {
  for i in `seq 1 $2`; do
    echo "[INFO] Running test $1 - Run # $i"

    if mvn test -Dtest=$1; then
      return 0
    fi

    # TODO: filter out assertion errors (which shouldn't trigger a retry)
  done

  return 1
}

cd $ITEST_DIR

for test in `find -name "*IT.java" -exec sh -c 'basename {} | cut -d'.' --complement -f2-' \;`; do
  if ! run-test-retry $test $DEFAULT_RETRY; then
    echo "[ERROR] Test $test FAILED after $DEFAULT_RETRY retries"
    exit 1
  fi
done

exit 0
