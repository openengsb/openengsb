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
package org.openengsb.connector.svn;

import org.apache.log4j.Logger;
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
    private Logger log = Logger.getLogger(getClass());

    private SvnConnector svn;
    private String context;

    private EventHelper eventHelper;

    public void init() {
        svn.checkout();
    }

    public void poll() {
        UpdateResult result = svn.update();

        if (result.getAddedBranches().size() > 0) {
            log.info("Added branches: " + result.getAddedBranches().size());
            for (String dir : result.getAddedBranches()) {
                sendEvent(new ScmBranchCreatedEvent(), dir);
            }
        }

        if (result.getAddedTags().size() > 0) {
            log.info("Added tags: " + result.getAddedTags().size());
            for (String dir : result.getAddedTags()) {
                sendEvent(new ScmTagCreatedEvent(), dir);
            }
        }

        if (result.getDeletedBranches().size() > 0) {
            log.info("Deleted branches: " + result.getDeletedBranches().size());
            for (String dir : result.getDeletedBranches()) {
                sendEvent(new ScmBranchDeletedEvent(), dir);
            }
        }

        if (result.getDeletedTags().size() > 0) {
            log.info("Deleted tags: " + result.getDeletedTags().size());
            for (String dir : result.getDeletedTags()) {
                sendEvent(new ScmTagDeletedEvent(), dir);
            }
        }

        if (result.getCommitted().size() > 0) {
            log.info("Committed directories: " + result.getCommitted().size());
            for (String dir : result.getCommitted()) {
                sendEvent(new ScmCheckInEvent(), dir);
            }
        }
    }
    
    private void sendEvent(ScmDirectoryEvent e, String dir) {
        e.setDirectory(dir);
        eventHelper.sendEvent(e);
    }

    public void setConfiguration(SvnConfiguration configuration) {
        svn = new SvnConnector(configuration);
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEndpoint(OpenEngSBEndpoint endpoint) {
        MessageProperties msgProperties = new MessageProperties(context, null);
        eventHelper = endpoint.createEventHelper(msgProperties);
    }

}
