/**

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
   
 */

package org.openengsb.maven.test.unit.deploy.constants;

public class DeployMvnTestConstants {
    public String REPOSITORY;
    public String GROUP_ID_PATH;
    public String VERSION;
    public String DEPLOYED_VERSION;
    public String[] MAVEN_METADATA_FILES;
    public Integer EXPECTED_DEPLOYED_FILES;

    public String[] VALID_ARTIFACTS;
    public String VALID_ARTIFACT;

    public String MISSING_POM_ARTIFACT;
    public String INVALID_POM_ARTIFACT;
    public String NON_EXISTING_ARTIFACT;
    public String[] INVALID_ARTIFACTS;

    public String[] SOME_VALID_SOME_INVALID_ARTIFACTS;

    public void setREPOSITORY(String repository) {
        this.REPOSITORY = repository;
    }

    public void setGROUP_ID_PATH(String group_id_path) {
        this.GROUP_ID_PATH = group_id_path;
    }

    public void setVERSION(String version) {
        this.VERSION = version;
    }

    public void setDEPLOYED_VERSION(String deployed_version) {
        this.DEPLOYED_VERSION = deployed_version;
    }

    public void setMAVEN_METADATA_FILES(String[] maven_metadata_files) {
        this.MAVEN_METADATA_FILES = maven_metadata_files;
    }

    public void setEXPECTED_DEPLOYED_FILES(Integer expected_deployed_files) {
        this.EXPECTED_DEPLOYED_FILES = expected_deployed_files;
    }

    public void setVALID_ARTIFACTS(String[] valid_artifacts) {
        this.VALID_ARTIFACTS = valid_artifacts;
    }

    public void setVALID_ARTIFACT(String valid_artifact) {
        this.VALID_ARTIFACT = valid_artifact;
    }

    public void setMISSING_POM_ARTIFACT(String missing_pom_artifact) {
        this.MISSING_POM_ARTIFACT = missing_pom_artifact;
    }

    public void setINVALID_POM_ARTIFACT(String invalid_pom_artifact) {
        this.INVALID_POM_ARTIFACT = invalid_pom_artifact;
    }

    public void setNON_EXISTING_ARTIFACT(String non_existing_artifact) {
        this.NON_EXISTING_ARTIFACT = non_existing_artifact;
    }

    public void setINVALID_ARTIFACTS(String[] invalid_artifacts) {
        this.INVALID_ARTIFACTS = invalid_artifacts;
    }

    public void setSOME_VALID_SOME_INVALID_ARTIFACTS(String[] some_valid_some_invalid_artifacts) {
        this.SOME_VALID_SOME_INVALID_ARTIFACTS = some_valid_some_invalid_artifacts;
    }
}
