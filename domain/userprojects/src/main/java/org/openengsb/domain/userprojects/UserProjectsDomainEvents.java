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

package org.openengsb.domain.userprojects;

import org.openengsb.core.api.DomainEvents;
import org.openengsb.domain.userprojects.event.UpdateAssignmentEvent;
import org.openengsb.domain.userprojects.event.UpdateProjectsEvent;
import org.openengsb.domain.userprojects.event.UpdateRolesEvent;
import org.openengsb.domain.userprojects.event.UpdateUserEvent;

/**
 * This interface can be used by connectors to check-in data into the EngSB. By throwing one of the given events you can
 * update information in the EngSB database.
 */
public interface UserProjectsDomainEvents extends DomainEvents {

    void raiseEvent(UpdateUserEvent e);

    void raiseEvent(UpdateProjectsEvent e);

    void raiseEvent(UpdateRolesEvent e);

    void raiseEvent(UpdateAssignmentEvent e);
}
