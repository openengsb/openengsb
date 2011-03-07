/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.jira.internal.models.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

public interface XmlRpcService {

    String login(String username, String password) throws Exception;

    boolean logout(String token);

    Hashtable<?, ?> getServerInfo(String token);

    Vector<?> getProjectsNoSchemes(String token) throws Exception;

    Vector<?> getVersions(String token, String projectKey) throws Exception;

    Vector<?> getComponents(String token, String projectKey) throws Exception;

    Vector<?> getIssueTypesForProject(String token, String projectId) throws Exception;

    Vector<?> getSubTaskIssueTypesForProject(String token, String projectId) throws Exception;

    Vector<?> getIssueTypes(String token) throws Exception;

    Vector<?> getSubTaskIssueTypes(String token) throws Exception;

    Vector<?> getPriorities(String token) throws Exception;

    Vector<?> getStatuses(String token) throws Exception;

    Vector<?> getResolutions(String token) throws Exception;

    Hashtable<?, ?> getUser(String token, String username) throws Exception;

    Vector<?> getSavedFilters(String token) throws Exception;

    Vector<?> getFavouriteFilters(String token) throws Exception;

    Hashtable<?, ?> getIssue(String token, String issueKey) throws Exception;

    Hashtable<?, ?> createIssue(String token, java.util.Hashtable<?, ?> rIssueStruct) throws Exception;

    Hashtable<?, ?> updateIssue(String token, String issueKey, java.util.Hashtable<?, ?> fieldValues) throws Exception;

    boolean addComment(String token, String issueKey, String comment) throws Exception;

    Vector<?> getIssuesFromFilter(String token, String filterId) throws Exception;

    Vector<?> getIssuesFromTextSearch(String token, String searchTerms) throws Exception;

    Vector<?> getIssuesFromTextSearchWithProject(String token, java.util.Vector<?> projectKeys, String searchTerms,
            int maxNumResults) throws Exception;

    Vector<?> getComments(String token, String issueKey) throws Exception;

}