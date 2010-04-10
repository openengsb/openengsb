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

cd $(dirname $0)/..
cp -u $2/org/apache/servicemix/servicemix-shared/2010.01/servicemix-shared-2010.01-installer.zip $1/deploy
cp -u features/edb/core/target/openengsb-features-edb-core-1.0.0-SNAPSHOT.zip $1/deploy
cp -u core/context/service-engine/target/openengsb-context-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u core/workflow/service-engine/target/openengsb-workflow-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/build/implementation/service-engine/target/openengsb-domains-build-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/build/maven/service-engine/target/openengsb-domains-build-maven-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/deploy/implementation/service-engine/target/openengsb-domains-deploy-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/deploy/maven/service-engine/target/openengsb-domains-deploy-maven-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/issues/implementation/service-engine/target/openengsb-domains-issues-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/issues/trac/service-engine/target/openengsb-domains-issues-trac-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/notification/email/service-engine/target/openengsb-domains-notification-email-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/notification/implementation/service-engine/target/openengsb-domains-notification-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/notification/twitter/service-engine/target/openengsb-domains-notification-twitter-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/report/implementation/service-engine/target/openengsb-domains-report-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/report/plaintext/service-engine/target/openengsb-domains-report-plaintext-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/scm/implementation/service-engine/target/openengsb-domains-scm-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/scm/subversion/service-engine/target/openengsb-domains-scm-subversion-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/test/implementation/service-engine/target/openengsb-domains-test-implementation-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u domains/test/maven/service-engine/target/openengsb-domains-test-maven-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u features/edb/service-engine/target/openengsb-features-edb-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u features/link/http-processor/service-engine/target/openengsb-features-link-http-processor-se-1.0.0-SNAPSHOT-installer.zip $1/deploy
cp -u package/all/target/openengsb-package-all-1.0.0-SNAPSHOT.zip $1/deploy
