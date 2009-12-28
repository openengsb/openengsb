/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.maven.test.unit.deploy.constants;

public class DeployMvnTestConstantsIntegration {
    public String XBEAN_XML_NAME = "spring-test-xbean-deploy.xml";

    public String VALID_ARTIFACTS_SERVICE_NAME = "validArtifacts";
    public String INVALID_ARTIFACTS_SERVICE_NAME = "invalidArtifacts";

    public String TEST_NAMESPACE = "urn:test";

    public String REPOSITORY = "target/testRepository/";

    public String RESOURCES_DIRECTORY = "src/test/resources/";
    public String VALID_ARTIFACT = this.RESOURCES_DIRECTORY + "deploy-valid1";
    public String MISSING_POM_ARTIFACT = this.RESOURCES_DIRECTORY + "deploy-missingPom";

    public void setTEST_NAMESPACE(String test_namespace) {
        this.TEST_NAMESPACE = test_namespace;
    }

    public void setREPOSITORY(String repository) {
        this.REPOSITORY = repository;
    }

    public void setRESOURCES_DIRECTORY(String resources_directory) {
        this.RESOURCES_DIRECTORY = resources_directory;
    }

    public void setVALID_ARTIFACT(String valid_artifact) {
        this.VALID_ARTIFACT = valid_artifact;
    }

    public void setMISSING_POM_ARTIFACT(String missing_pom_artifact) {
        this.MISSING_POM_ARTIFACT = missing_pom_artifact;
    }

    public void setXBEAN_XML_NAME(String xbean_xml_name) {
        this.XBEAN_XML_NAME = xbean_xml_name;
    }

    public void setVALID_ARTIFACTS_SERVICE_NAME(String valid_artifacts_service_name) {
        this.VALID_ARTIFACTS_SERVICE_NAME = valid_artifacts_service_name;
    }

    public void setINVALID_ARTIFACTS_SERVICE_NAME(String invalid_artifacts_service_name) {
        this.INVALID_ARTIFACTS_SERVICE_NAME = invalid_artifacts_service_name;
    }
}
