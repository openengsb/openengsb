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
package org.openengsb.connector.svn.test.unit.constants;

/**
 * Bean that holds the constants for Unit-Testing. The actual values are
 * injected via spring.
 * 
 */
public class SvnScmDomainTestConstants {
    public String FILE_TO_ADD;
    public String REPOSITORY;
    public String REPOSITORY_NO_BRANCH;
    public String TRUNK;
    public String BRANCHES;
    public String BRANCHLESS_REPOSITORY_TRUNK;
    public String BRANCHLESS_REPOSITORY_BRANCHES;
    public String[] WORKING_COPIES;
    public String EXPORT_PATH;
    public String IMPORT_PATH;
    public String IMPORT_FILE;
    public String REFERENCE_REPOSITORY;
    public String REFERENCE_REPOSITORY_NO_BRANCH;
    public String AUTHOR;

    public String DELETE_FILE;
    public String MERGE_FILE;
    public String TEST_FILE;
    public String UPDATE_FILE;
    public String[] INITIAL_FILES;
    public String NOT_EXISTING_FILE;
    public String WORKING_COPY_LEAVING_FILE;
    public String SUB_PATH;

    public String DEFAULT_WORKING_COPY;

    public void setFILE_TO_ADD(String file_to_add) {
        this.FILE_TO_ADD = file_to_add;
    }

    public void setWORKING_COPIES(String[] working_copy) {
        this.WORKING_COPIES = working_copy;
    }

    public void setREFERENCE_REPOSITORY(String reference_repository) {
        this.REFERENCE_REPOSITORY = reference_repository;
    }

    public void setAUTHOR(String author) {
        this.AUTHOR = author;
    }

    public void setDELETE_FILE(String delete_file) {
        this.DELETE_FILE = delete_file;
    }

    public void setMERGE_FILE(String merge_file) {
        this.MERGE_FILE = merge_file;
    }

    public void setTEST_FILE(String test_file) {
        this.TEST_FILE = test_file;
    }

    public void setINITIAL_FILES(String[] initial_files) {
        this.INITIAL_FILES = initial_files;
    }

    public void setNOT_EXISTING_FILE(String not_existing_file) {
        this.NOT_EXISTING_FILE = not_existing_file;
    }

    public void setWORKING_COPY_LEAVING_FILE(String working_copy_leaving_file) {
        this.WORKING_COPY_LEAVING_FILE = working_copy_leaving_file;
    }

    public void setSUB_PATH(String sub_path) {
        this.SUB_PATH = sub_path;
    }

    public void setUPDATE_FILE(String update_file) {
        this.UPDATE_FILE = update_file;
    }

    public void setTRUNK(String trunk) {
        this.TRUNK = trunk;
    }

    public void setBRANCHES(String branches) {
        this.BRANCHES = branches;
    }

    public void setREPOSITORY(String repository) {
        this.REPOSITORY = repository;
    }

    public void setBRANCHLESS_REPOSITORY_TRUNK(String branchless_repository_trunk) {
        this.BRANCHLESS_REPOSITORY_TRUNK = branchless_repository_trunk;
    }

    public void setREPOSITORY_NO_BRANCH(String repository_no_branch) {
        this.REPOSITORY_NO_BRANCH = repository_no_branch;
    }

    public void setREFERENCE_REPOSITORY_NO_BRANCH(String reference_repository_no_branch) {
        this.REFERENCE_REPOSITORY_NO_BRANCH = reference_repository_no_branch;
    }

    public void setBRANCHLESS_REPOSITORY_BRANCHES(String branchless_repository_branches) {
        this.BRANCHLESS_REPOSITORY_BRANCHES = branchless_repository_branches;
    }

    public void setEXPORT_PATH(String export_path) {
        this.EXPORT_PATH = export_path;
    }

    public void setIMPORT_PATH(String import_path) {
        this.IMPORT_PATH = import_path;
    }

    public void setIMPORT_FILE(String import_file) {
        this.IMPORT_FILE = import_file;
    }

    public void setDEFAULT_WORKING_COPY(String default_working_copy) {
        this.DEFAULT_WORKING_COPY = default_working_copy;
    }
}
