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
package org.openengsb.scm.common.test.unit.constants;

/**
 * Class holding injected constants for testing purposes.
 * 
 */
public class GeneralEndpointUnitTestConstants {
    public String ADD_FILE;
    public String BLAME_FILE;
    public String BRANCH_NAME;
    public String BRANCH_COMMIT_MESSAGE;
    public String CHECKOUT_AUTHOR;
    public String COMMIT_AUTHOR;
    public String COMMIT_MESSAGE;
    public String DELETE_FILE;
    public String DIFF_FILE;
    public String DIFF_REVISION;
    public String EXPORT_DESTINATION;
    public String IMPORT_SOURCE;
    public String IMPORT_COMMIT_MESSAGE;
    public String IMPORT_AUTHOR;
    public String[] LOG_FILES;
    public String LOG_START_REVISION;
    public String LOG_END_REVISION;
    public String MERGE_BRANCH_NAME;
    public String SWITCH_BRANCH_BRANCH_NAME;

    public void setADD_FILE(String add_file) {
        this.ADD_FILE = add_file;
    }

    public void setBLAME_FILE(String blame_file) {
        this.BLAME_FILE = blame_file;
    }

    public void setBRANCH_NAME(String branch_name) {
        this.BRANCH_NAME = branch_name;
    }

    public void setBRANCH_COMMIT_MESSAGE(String branch_commit_message) {
        this.BRANCH_COMMIT_MESSAGE = branch_commit_message;
    }

    public void setCHECKOUT_AUTHOR(String checkout_author) {
        this.CHECKOUT_AUTHOR = checkout_author;
    }

    public void setCOMMIT_AUTHOR(String commit_author) {
        this.COMMIT_AUTHOR = commit_author;
    }

    public void setCOMMIT_MESSAGE(String commit_message) {
        this.COMMIT_MESSAGE = commit_message;
    }

    public void setDELETE_FILE(String delete_file) {
        this.DELETE_FILE = delete_file;
    }

    public void setDIFF_FILE(String diff_file) {
        this.DIFF_FILE = diff_file;
    }

    public void setDIFF_REVISION(String diff_revision) {
        this.DIFF_REVISION = diff_revision;
    }

    public void setEXPORT_DESTINATION(String export_destination) {
        this.EXPORT_DESTINATION = export_destination;
    }

    public void setIMPORT_SOURCE(String import_source) {
        this.IMPORT_SOURCE = import_source;
    }

    public void setIMPORT_COMMIT_MESSAGE(String import_commit_message) {
        this.IMPORT_COMMIT_MESSAGE = import_commit_message;
    }

    public void setIMPORT_AUTHOR(String import_author) {
        this.IMPORT_AUTHOR = import_author;
    }

    public void setLOG_FILES(String[] log_files) {
        this.LOG_FILES = log_files;
    }

    public void setLOG_START_REVISION(String log_start_revision) {
        this.LOG_START_REVISION = log_start_revision;
    }

    public void setLOG_END_REVISION(String log_end_revision) {
        this.LOG_END_REVISION = log_end_revision;
    }

    public void setMERGE_BRANCH_NAME(String merge_branch_name) {
        this.MERGE_BRANCH_NAME = merge_branch_name;
    }

    public void setSWITCH_BRANCH_BRANCH_NAME(String switch_branch_branch_name) {
        this.SWITCH_BRANCH_BRANCH_NAME = switch_branch_branch_name;
    }
}
