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

import java.io.File;
import java.util.Map;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.AbstractConnectorInstanceFactory;

public class UserProjectsFileServiceInstanceFactory extends
        AbstractConnectorInstanceFactory<UserProjectsFileServiceImpl> {

    private SynchronizationService synchronizationService;

    public void setSynchronizationService(SynchronizationService service) {
        synchronizationService = service;
    }

    public UserProjectsFileServiceInstanceFactory() {
    }

    @Override
    public Connector createNewInstance(String id) {
        return new UserProjectsFileServiceImpl();
    }

    @Override
    public UserProjectsFileServiceImpl doApplyAttributes(UserProjectsFileServiceImpl instance,
        Map<String, String> attributes) {
        if (attributes.containsKey("baseDir")) {
            File baseDir = new File(attributes.get("baseDir"));
            if (baseDir.exists()) {
                String oldContext = ContextHolder.get().getCurrentContextId();
                ContextHolder.get().setCurrentContextId("userprojects-jira");
                synchronizationService.syncFromFilesToOpenEngSB(baseDir);
                ContextHolder.get().setCurrentContextId(oldContext);
            }
        }

        return instance;
    }
}
