'''
Copyright 2010 OpenEngSB Division, Vienna University of Technology

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

'''

def ignored_version_files():
	return ['domains/toolabstraction/maven/src/test/resources/pom.xml',
		'domains/toolabstraction/maven/src/test/resources/build-test/pom.xml',
		'domains/toolabstraction/maven/src/test/resources/deploy-test/pom.xml',
		'domains/toolabstraction/maven/src/test/resources/test-invalid-pom/pom.xml',
		'domains/toolabstraction/maven/src/test/resources/test-unit-fail/pom.xml',
		'domains/toolabstraction/maven/src/test/resources/test-unit-success/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/test-invalid-pom/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/deploy-test/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/test-unit-success/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/build-test/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/test-unit-fail/pom.xml',
		'domains/toolabstraction/maven/target/test-classes/pom.xml']
