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

package org.openengsb.maven.test.constants;

public class TestMvnTestConstants {

    private String test_valid_surefire;
    private String test_invalid_surefire;
    private String test_no_surefire;
    private String test_unit_fail;
    private String test_settings_file;

    public void setTest_valid_surefire(String test_valid_surefire) {
        this.test_valid_surefire = test_valid_surefire;
    }

    public String getTest_valid_surefire() {
        return this.test_valid_surefire;
    }

    public void setTest_invalid_surefire(String test_invalid_surefire) {
        this.test_invalid_surefire = test_invalid_surefire;
    }

    public String getTest_invalid_surefire() {
        return this.test_invalid_surefire;
    }

    public void setTest_no_surefire(String test_no_surefire) {
        this.test_no_surefire = test_no_surefire;
    }

    public String getTest_no_surefire() {
        return this.test_no_surefire;
    }

    public void setTest_unit_fail(String test_unit_fail) {
        this.test_unit_fail = test_unit_fail;
    }

    public String getTest_unit_fail() {
        return this.test_unit_fail;
    }

    public void setTest_settings_file(String test_settings_file) {
        this.test_settings_file = test_settings_file;
    }

    public String getTest_settings_file() {
        return this.test_settings_file;
    }

}
