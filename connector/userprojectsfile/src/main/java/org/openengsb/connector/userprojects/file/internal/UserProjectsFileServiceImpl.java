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

package org.openengsb.connector.userprojects.file.internal;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.userprojects.UserProjectsDomain;
import org.openengsb.domain.userprojects.model.Assignment;
import org.openengsb.domain.userprojects.model.Project;
import org.openengsb.domain.userprojects.model.Role;
import org.openengsb.domain.userprojects.model.User;

public class UserProjectsFileServiceImpl extends AbstractOpenEngSBConnectorService implements UserProjectsDomain {

    public UserProjectsFileServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void updateAssignments(List<Assignment> assignments) {
        throw new NotImplementedException();
    }

    @Override
    public void updateProjects(List<Project> projects) {
        throw new NotImplementedException();
    }

    @Override
    public void updateRoles(List<Role> roles) {
        throw new NotImplementedException();
    }

    @Override
    public void updateUsers(List<User> users) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAssignments(List<Assignment> assignments) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteProjects(List<Project> projects) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteRoles(List<Role> roles) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteUsers(List<User> users) {
        throw new NotImplementedException();
    }

}
