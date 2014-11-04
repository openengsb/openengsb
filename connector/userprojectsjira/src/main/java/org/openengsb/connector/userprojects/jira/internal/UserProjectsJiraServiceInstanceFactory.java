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

package org.openengsb.connector.userprojects.jira.internal;

import java.net.URI;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.openengsb.connector.userprojects.jira.internal.jira.JiraClient;
import org.openengsb.connector.userprojects.jira.internal.jira.JiraClientFactory;
import org.openengsb.connector.userprojects.jira.internal.jira.ServerConfig;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.AbstractConnectorInstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserProjectsJiraServiceInstanceFactory extends
        AbstractConnectorInstanceFactory<UserProjectsJiraServiceImpl> {
    private static final Logger LOG = LoggerFactory.getLogger(UserProjectsJiraServiceInstanceFactory.class);

    private SynchronizationService synchronizationService;
    private final Timer syncTimer = new Timer();

    public UserProjectsJiraServiceInstanceFactory() {
    }

    public void setSynchronizationService(SynchronizationService service) {
        synchronizationService = service;
    }

    @Override
    public Connector createNewInstance(String id) {
        return new UserProjectsJiraServiceImpl();
    }

    @Override
    public UserProjectsJiraServiceImpl doApplyAttributes(UserProjectsJiraServiceImpl instance,
        Map<String, String> attributes) {
        if (attributes.containsKey("jiraServer.uri")) {
            ServerConfig.uri = URI.create(attributes.get("jiraServer.uri"));
        }
        if (attributes.containsKey("jiraServer.username")) {
            ServerConfig.username = attributes.get("jiraServer.username");
        }
        if (attributes.containsKey("jiraServer.password")) {
            ServerConfig.password = attributes.get("jiraServer.password");
        }
        if (attributes.containsKey("jiraServer.syncPeriodInMilliseconds")) {
            ServerConfig.syncPeriodInMilliseconds =
                Long.valueOf(attributes.get("jiraServer.syncPeriodInMilliseconds"));
        }

        setupSynchronization();

        return instance;
    }

    private void setupSynchronization() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String oldContext = ContextHolder.get().getCurrentContextId();
                ContextHolder.get().setCurrentContextId("userprojects-jira");
                try {
                    LOG.info("Trying to authenticate to the JIRA server on {}", new Object[] { ServerConfig.uri });
                    JiraClient jiraClient = JiraClientFactory.create();
                    synchronizationService.syncFromJiraServerToOpenEngSB(jiraClient);
                } catch (Exception e) {
                    LOG.error("Could not sync from JIRA server to OpenEngSB. The configuration might be wrong.");
                    LOG.debug("Sync error", e);
                }
                ContextHolder.get().setCurrentContextId(oldContext);
            }
        };

        syncTimer.schedule(task, 0, ServerConfig.syncPeriodInMilliseconds);
    }

    public void destroy() {
        syncTimer.cancel();
    }
}
