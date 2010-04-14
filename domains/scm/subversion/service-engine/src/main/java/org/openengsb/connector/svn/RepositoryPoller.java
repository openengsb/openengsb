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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openengsb.core.EventHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.drools.events.ScmBranchAlteredEvent;
import org.openengsb.drools.events.ScmBranchCreatedEvent;
import org.openengsb.drools.events.ScmBranchDeletedEvent;
import org.openengsb.drools.events.ScmCheckInEvent;
import org.openengsb.drools.events.ScmDirectoryEvent;
import org.openengsb.drools.events.ScmTagCreatedEvent;

public class RepositoryPoller {
    private Logger log = Logger.getLogger(getClass());

    private SvnConnector svn;
    private String author;
    private EventHelper eventHelper;

    private String revision;
    private List<String> branches;
    private List<String> tags;

    public void poll() {
        inspectCheckins();
        inspectBranches();
        inspectTags();
    }

    private void inspectBranches() {
        List<String> newBranches = svn.listBranches();
        ScmDirectoryEvent e = null;
        Set<String> branchNames = null;

        if (branches != null) {
            if (branches.size() < newBranches.size()) {
                e = new ScmBranchCreatedEvent();

                branchNames = new HashSet<String>(newBranches);
                branchNames.removeAll(branches);
                log.info("Found " + branchNames.size() + " new branches");
            } else if (branches.size() > newBranches.size()) {
                e = new ScmBranchDeletedEvent();

                branchNames = new HashSet<String>(branches);
                branchNames.removeAll(newBranches);
                log.info("Found " + branchNames.size() + " deleted branches");
            } else {
                branchNames = new HashSet<String>(branches);
                Set<String> tempNames = new HashSet<String>(branches);
                tempNames.retainAll(newBranches);
                branchNames.removeAll(tempNames);

                if (branchNames.size() > 0) {
                    e = new ScmBranchAlteredEvent();
                    log.info("Found " + branchNames.size() + " changed branches");
                }
            }

            if (e != null) {
                e.setDirectories(new ArrayList<String>(branchNames));
                eventHelper.sendEvent(e);
            }
        }

        branches = newBranches;
    }

    private void inspectTags() {
        List<String> newTags = svn.listTags();

        if (tags != null && tags.size() < newTags.size()) {
            ScmTagCreatedEvent e = new ScmTagCreatedEvent();

            Set<String> tagNames = new HashSet<String>(newTags);
            tagNames.removeAll(tags);
            log.info("Found " + tagNames.size() + " new tags");

            e.setDirectories(new ArrayList<String>(tagNames));
            eventHelper.sendEvent(e);
        }

        tags = newTags;
    }

    private void inspectCheckins() {
        String newRevision = svn.checkout(author).getRevision();

        if (revision != null && !newRevision.equals(revision)) {
            ScmCheckInEvent e = new ScmCheckInEvent();
            e.setRevision(newRevision);
            log.info("Detected checkin to revision " + newRevision);

            eventHelper.sendEvent(e);
        }

        revision = newRevision;
    }

    public void setConfiguration(SvnConfiguration configuration) {
        svn = new SvnConnector(configuration);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setEndpoint(OpenEngSBEndpoint endpoint) {
        MessageProperties msgProperties = new MessageProperties("42", null);
        eventHelper = endpoint.createEventHelper(msgProperties);
    }

}
