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
package org.openengsb.connector.userprojects.jira.internal.jira;

import java.util.List;

import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.User;

/**
 * Acts as an entity manager for JIRA server.
 *
 */
public interface ModelManager {

    /**
     * Finds all available projects on the JIRA server.
     */
    List<Project> findProjects();
    
    /**
     * Finds all available users on the JIRA server.
     */
    List<User> findUsers();

    /**
     * Finds all available assignments on the JIRA server.
     */
    List<Assignment> findAssignments();
}
