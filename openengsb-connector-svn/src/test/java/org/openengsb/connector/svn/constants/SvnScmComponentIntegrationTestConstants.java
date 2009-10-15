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
package org.openengsb.connector.svn.constants;

/**
 * Bean holding the constant needed for Integration-Testing. For
 * Integration-Tests we also need the fields from Unit-Testing and therefore
 * this class extends SvnScmDomainTestConstants.
 */
public class SvnScmComponentIntegrationTestConstants extends SvnScmDomainTestConstants {
    public String XBEAN_XML_NAME;
    public String TEST_NAMESPACE;
    public String CHECKOUT_WC1_TRUNK_SERVICE_NAME;
    public String CHECKOUT_WC2_TRUNK_SERVICE_NAME;
    public String CHECKOUT_WC2_BRANCHES_SERVICE_NAME;
    public String CHECKOUT_WC1_NO_BRANCHES_TRUNK_SERVICE_NAME;
    public String CHECKOUT_WC1_NO_BRANCHES_REPOSITORY_SERVICE_NAME;
    public String CHECKOUT_WC2_NO_BRANCHES_BRANCHES_SERVICE_NAME;
    public String CHECKOUT_BOTH_CONNECTIONS_SET;
    public String CHECKOUT_ONLY_CONNECTION_SET;
    public String CHECKOUT_NO_CONNECTION_SET;
    public String CHECKOUT_NO_WORKING_COPY_SET_SERVICE_NAME;
    public String ADD_SERVICE_NAME;
    public String DELETE_SERVICE_NAME;
    public String[] COMMIT_SERVICE_NAMES;
    public String UPDATE_SERVICE_NAME;
    public String BRANCH_SERVICE_NAME;
    public String LIST_BRANCHES_SERVICE_NAME;
    public String SWITCH_BRANCH_SERVICE_NAME;
    public String MERGE_SERVICE_NAME;
    public String BLAME_SERVICE_NAME;
    public String DIFF_SERVICE_NAME;
    public String EXPORT_SERVICE_NAME;
    public String IMPORT_SERVICE_NAME;
    public String LOG_SERVICE_NAME;
    public String IMPORT_NO_CONNECTION_SET_SERVICE_NAME;
    public String BRANCH_ONLY_CONNECTION_SET_SERVICE_NAME;
    public String COMMIT_ONLY_CONNECTION_SET_SERVICE_NAME;
    public String REPOSITORY_ENV_NAME;
    public String REPOSITORY_NO_BRANCH_ENV_NAME;
    public String TRUNK_ENV_NAME;
    public String NO_BRANCH_TRUNK_ENV_NAME;
    public String BRANCHES_ENV_NAME;
    public String NO_BRANCH_BRANCHES_ENV_NAME;
    public String WORKING_COPY1_ENV_NAME;
    public String WORKING_COPY2_ENV_NAME;

    public void setXBEAN_XML_NAME(String xbean_xml_name) {
        this.XBEAN_XML_NAME = xbean_xml_name;
    }

    public void setTEST_NAMESPACE(String test_namespace) {
        this.TEST_NAMESPACE = test_namespace;
    }

    public void setADD_SERVICE_NAME(String add_service_name) {
        this.ADD_SERVICE_NAME = add_service_name;
    }

    public void setDELETE_SERVICE_NAME(String delete_service_name) {
        this.DELETE_SERVICE_NAME = delete_service_name;
    }

    public void setCOMMIT_SERVICE_NAMES(String[] commit_service_names) {
        this.COMMIT_SERVICE_NAMES = commit_service_names;
    }

    public void setUPDATE_SERVICE_NAME(String update_service_name) {
        this.UPDATE_SERVICE_NAME = update_service_name;
    }

    public void setBRANCH_SERVICE_NAME(String branch_service_name) {
        this.BRANCH_SERVICE_NAME = branch_service_name;
    }

    public void setLIST_BRANCHES_SERVICE_NAME(String list_branches_service_name) {
        this.LIST_BRANCHES_SERVICE_NAME = list_branches_service_name;
    }

    public void setSWITCH_BRANCH_SERVICE_NAME(String switch_branch_service_name) {
        this.SWITCH_BRANCH_SERVICE_NAME = switch_branch_service_name;
    }

    public void setMERGE_SERVICE_NAME(String merge_service_name) {
        this.MERGE_SERVICE_NAME = merge_service_name;
    }

    public void setBLAME_SERVICE_NAME(String blame_service_name) {
        this.BLAME_SERVICE_NAME = blame_service_name;
    }

    public void setDIFF_SERVICE_NAME(String diff_service_name) {
        this.DIFF_SERVICE_NAME = diff_service_name;
    }

    public void setEXPORT_SERVICE_NAME(String export_service_name) {
        this.EXPORT_SERVICE_NAME = export_service_name;
    }

    public void setIMPORT_SERVICE_NAME(String import_service_name) {
        this.IMPORT_SERVICE_NAME = import_service_name;
    }

    public void setLOG_SERVICE_NAME(String log_service_name) {
        this.LOG_SERVICE_NAME = log_service_name;
    }

    public void setREPOSITORY_ENV_NAME(String repository_env_name) {
        this.REPOSITORY_ENV_NAME = repository_env_name;
    }

    public void setREPOSITORY_NO_BRANCH_ENV_NAME(String repository_no_branch_env_name) {
        this.REPOSITORY_NO_BRANCH_ENV_NAME = repository_no_branch_env_name;
    }

    public void setTRUNK_ENV_NAME(String trunk_env_name) {
        this.TRUNK_ENV_NAME = trunk_env_name;
    }

    public void setNO_BRANCH_TRUNK_ENV_NAME(String no_branch_trunk_env_name) {
        this.NO_BRANCH_TRUNK_ENV_NAME = no_branch_trunk_env_name;
    }

    public void setCHECKOUT_WC1_TRUNK_SERVICE_NAME(String checkout_wc1_trunk_service_name) {
        this.CHECKOUT_WC1_TRUNK_SERVICE_NAME = checkout_wc1_trunk_service_name;
    }

    public void setCHECKOUT_WC2_TRUNK_SERVICE_NAME(String checkout_wc2_trunk_service_name) {
        this.CHECKOUT_WC2_TRUNK_SERVICE_NAME = checkout_wc2_trunk_service_name;
    }

    public void setCHECKOUT_WC1_NO_BRANCHES_TRUNK_SERVICE_NAME(String checkout_wc1_no_branches_trunk_service_name) {
        this.CHECKOUT_WC1_NO_BRANCHES_TRUNK_SERVICE_NAME = checkout_wc1_no_branches_trunk_service_name;
    }

    public void setCHECKOUT_WC1_NO_BRANCHES_REPOSITORY_SERVICE_NAME(
            String checkout_wc1_no_branches_repository_service_name) {
        this.CHECKOUT_WC1_NO_BRANCHES_REPOSITORY_SERVICE_NAME = checkout_wc1_no_branches_repository_service_name;
    }

    public void setBRANCHES_ENV_NAME(String branches_env_name) {
        this.BRANCHES_ENV_NAME = branches_env_name;
    }

    public void setCHECKOUT_WC2_BRANCHES_SERVICE_NAME(String checkout_wc2_branches_service_name) {
        this.CHECKOUT_WC2_BRANCHES_SERVICE_NAME = checkout_wc2_branches_service_name;
    }

    public void setNO_BRANCH_BRANCHES_ENV_NAME(String no_branch_branches_env_name) {
        this.NO_BRANCH_BRANCHES_ENV_NAME = no_branch_branches_env_name;
    }

    public void setCHECKOUT_WC2_NO_BRANCHES_BRANCHES_SERVICE_NAME(String checkout_wc2_no_branches_branches_service_name) {
        this.CHECKOUT_WC2_NO_BRANCHES_BRANCHES_SERVICE_NAME = checkout_wc2_no_branches_branches_service_name;
    }

    public void setCHECKOUT_BOTH_CONNECTIONS_SET(String checkout_both_connections_set) {
        this.CHECKOUT_BOTH_CONNECTIONS_SET = checkout_both_connections_set;
    }

    public void setCHECKOUT_ONLY_CONNECTION_SET(String checkout_only_connection_set) {
        this.CHECKOUT_ONLY_CONNECTION_SET = checkout_only_connection_set;
    }

    public void setCHECKOUT_NO_CONNECTION_SET(String checkout_no_connection_set) {
        this.CHECKOUT_NO_CONNECTION_SET = checkout_no_connection_set;
    }

    public void setIMPORT_NO_CONNECTION_SET_SERVICE_NAME(String import_no_connection_set_service_name) {
        this.IMPORT_NO_CONNECTION_SET_SERVICE_NAME = import_no_connection_set_service_name;
    }

    public void setBRANCH_ONLY_CONNECTION_SET_SERVICE_NAME(String branch_only_connection_set_service_name) {
        this.BRANCH_ONLY_CONNECTION_SET_SERVICE_NAME = branch_only_connection_set_service_name;
    }

    public void setCOMMIT_ONLY_CONNECTION_SET_SERVICE_NAME(String commit_only_connection_set_service_name) {
        this.COMMIT_ONLY_CONNECTION_SET_SERVICE_NAME = commit_only_connection_set_service_name;
    }

    public void setCHECKOUT_NO_WORKING_COPY_SET_SERVICE_NAME(String checkout_no_working_copy_set_service_name) {
        this.CHECKOUT_NO_WORKING_COPY_SET_SERVICE_NAME = checkout_no_working_copy_set_service_name;
    }

    public void setWORKING_COPY1_ENV_NAME(String working_copy1_env_name) {
        this.WORKING_COPY1_ENV_NAME = working_copy1_env_name;
    }

    public void setWORKING_COPY2_ENV_NAME(String working_copy2_env_name) {
        this.WORKING_COPY2_ENV_NAME = working_copy2_env_name;
    }
}
