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

package org.openengsb.scm.common.endpoints;

/**
 * Interface that holds the command-names of all SCM Endpoints
 * 
 * @author patrick
 * 
 */
public interface EndpointCommandNames {
    public static final String ADD_COMMAND_NAME = "add";
    public static final String BLAME_COMMAND_NAME = "blame";
    public static final String BRANCH_COMMAND_NAME = "branch";
    public static final String CHECKOUT_COMMAND_NAME = "checkout";
    public static final String CHECKOUT_OR_UPDATE_COMMAND_NAME = "checkoutOrUpdate";
    public static final String COMMIT_COMMAND_NAME = "commit";
    public static final String DELETE_COMMAND_NAME = "delete";
    public static final String DIFF_COMMAND_NAME = "diff";
    public static final String EXPORT_COMMAND_NAME = "export";
    public static final String IMPORT_COMMAND_NAME = "import";
    public static final String LIST_BRANCHES_COMMAND_NAME = "listBranches";
    public static final String LOG_COMMAND_NAME = "log";
    public static final String MERGE_COMMAND_NAME = "merge";
    public static final String SWITCH_BRANCH_COMMAND_NAME = "switchBranch";
    public static final String UPDATE_COMMAND_NAME = "update";
}
