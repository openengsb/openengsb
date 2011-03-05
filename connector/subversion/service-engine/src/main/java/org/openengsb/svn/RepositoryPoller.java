/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.svn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.EventHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.drools.events.ScmBranchCreatedEvent;
import org.openengsb.drools.events.ScmBranchDeletedEvent;
import org.openengsb.drools.events.ScmCheckInEvent;
import org.openengsb.drools.events.ScmDirectoryEvent;
import org.openengsb.drools.events.ScmTagCreatedEvent;
import org.openengsb.drools.events.ScmTagDeletedEvent;

public class RepositoryPoller {
    private Log log = LogFactory.getLog(getClass());

    private SvnConnector connector;
    private String context;

    private EventHelper eventHelper;

    public void poll() {
        if (!isInitialCreationPossible()) {
            return;
        }

        UpdateResult result = null;
        try {
            result = this.connector.update();
        } catch (Exception e) {
            this.log.error("Update of repository is not possible. Maybe the Repository is not setup correctly.", e);
            return;
        }

        handleAddedBranches(result);
        handleAddedTags(result);
        handleDeletedBranches(result);
        handleDeletedTags(result);
        handleNewCommits(result);
    }

    private boolean isInitialCreationPossible() {
        if (!this.connector.getWorkingCopyFile().exists()) {
            try {
                this.connector.checkout();
            } catch (Exception e) {
                this.log.error("Repository not setup correctly. It's not possible to do a checkout.", e);
                return false;
            }
        }
        return true;
    }

    private void handleNewCommits(UpdateResult result) {
        if (result.getCommitted().size() > 0) {
            this.log.info("Committed directories: " + result.getCommitted().size());
            for (String dir : result.getCommitted()) {
                sendEvent(new ScmCheckInEvent(), dir);
            }
        }
    }

    private void handleDeletedTags(UpdateResult result) {
        if (result.getDeletedTags().size() > 0) {
            this.log.info("Deleted tags: " + result.getDeletedTags().size());
            for (String dir : result.getDeletedTags()) {
                sendEvent(new ScmTagDeletedEvent(), dir);
            }
        }
    }

    private void handleDeletedBranches(UpdateResult result) {
        if (result.getDeletedBranches().size() > 0) {
            this.log.info("Deleted branches: " + result.getDeletedBranches().size());
            for (String dir : result.getDeletedBranches()) {
                sendEvent(new ScmBranchDeletedEvent(), dir);
            }
        }
    }

    private void handleAddedTags(UpdateResult result) {
        if (result.getAddedTags().size() > 0) {
            this.log.info("Added tags: " + result.getAddedTags().size());
            for (String dir : result.getAddedTags()) {
                sendEvent(new ScmTagCreatedEvent(), dir);
            }
        }
    }

    private void handleAddedBranches(UpdateResult result) {
        if (result.getAddedBranches().size() > 0) {
            this.log.info("Added branches: " + result.getAddedBranches().size());
            for (String dir : result.getAddedBranches()) {
                sendEvent(new ScmBranchCreatedEvent(), dir);
            }
        }
    }

    private void sendEvent(ScmDirectoryEvent e, String dir) {
        e.setDirectory(dir);
        this.eventHelper.sendEvent(e);
    }

    public void setConnector(SvnConnector connector) {
        this.connector = connector;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEndpoint(OpenEngSBEndpoint endpoint) {
        MessageProperties msgProperties = new MessageProperties(this.context, null);
        this.eventHelper = endpoint.createEventHelper(msgProperties);
    }

}
