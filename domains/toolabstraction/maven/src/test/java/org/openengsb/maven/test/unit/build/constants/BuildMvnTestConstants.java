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

package org.openengsb.maven.test.unit.build.constants;

public class BuildMvnTestConstants {

    private String test_project;
    private String test_project_fail;
    private String test_project_dep;
    private String test_project_invalid_pom;
    private String test_settings_file;

    public void setTest_project(String test_project) {
        this.test_project = test_project;
    }

    public String getTest_project() {
        return this.test_project;
    }

    public void setTest_project_fail(String test_project_fail) {
        this.test_project_fail = test_project_fail;
    }

    public String getTest_project_fail() {
        return this.test_project_fail;
    }

    public void setTest_project_dep(String test_project_dep) {
        this.test_project_dep = test_project_dep;
    }

    public String getTest_project_dep() {
        return this.test_project_dep;
    }

    public void setTest_project_invalid_pom(String test_project_invalid_pom) {
        this.test_project_invalid_pom = test_project_invalid_pom;
    }

    public String getTest_project_invalid_pom() {
        return this.test_project_invalid_pom;
    }

    public void setTest_settings_file(String test_settings_file) {
        this.test_settings_file = test_settings_file;
    }

    public String getTest_settings_file() {
        return this.test_settings_file;
    }

}
