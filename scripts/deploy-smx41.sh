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
  echo Usage: $0 servicemix-home maven-home
  exit 1
}

if [ $# != 2 ] ;
then
  usage
fi

if [ ! -d $1/deploy ] ;
then
  echo Error: servicemix-home directory does not exist
  usage
fi

if [ ! -d $2/repository ] ;
then
  echo Error: maven-home directory does not exist
  usage
fi

cd $(dirname $0)/..

files="
$2/repository/org/apache/servicemix/servicemix-shared/2010.01/servicemix-shared-2010.01-installer.zip
features/edb/core/target/openengsb-features-edb-core-*.zip
core/context/service-engine/target/openengsb-context-se-*-installer.zip
core/workflow/service-engine/target/openengsb-workflow-se-*-installer.zip
domains/build/implementation/service-engine/target/openengsb-domains-build-implementation-se-*-installer.zip
domains/build/maven/service-engine/target/openengsb-domains-build-maven-se-*-installer.zip
domains/deploy/implementation/service-engine/target/openengsb-domains-deploy-implementation-se-*-installer.zip
domains/deploy/maven/service-engine/target/openengsb-domains-deploy-maven-se-*-installer.zip
domains/issues/implementation/service-engine/target/openengsb-domains-issues-implementation-se-*-installer.zip
domains/issues/trac/service-engine/target/openengsb-domains-issues-trac-se-*-installer.zip
domains/notification/email/service-engine/target/openengsb-domains-notification-email-se-*-installer.zip
domains/notification/implementation/service-engine/target/openengsb-domains-notification-implementation-se-*-installer.zip
domains/notification/twitter/service-engine/target/openengsb-domains-notification-twitter-se-*-installer.zip
domains/report/implementation/service-engine/target/openengsb-domains-report-implementation-se-*-installer.zip
domains/report/plaintext/service-engine/target/openengsb-domains-report-plaintext-se-*-installer.zip
domains/scm/implementation/service-engine/target/openengsb-domains-scm-implementation-se-*-installer.zip
domains/scm/subversion/service-engine/target/openengsb-domains-scm-subversion-se-*-installer.zip
domains/test/implementation/service-engine/target/openengsb-domains-test-implementation-se-*-installer.zip
domains/test/maven/service-engine/target/openengsb-domains-test-maven-se-*-installer.zip
features/edb/service-engine/target/openengsb-features-edb-se-*-installer.zip
features/link/http-processor/service-engine/target/openengsb-features-link-http-processor-se-*-installer.zip
package/all/target/openengsb-package-all-*.zip"

for file in $files;
do
  echo Deploying `basename $file`
  cp $file $1/deploy/
done

