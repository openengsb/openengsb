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

import org.openengsb.core.EventHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.drools.events.ScmBranchAlteredEvent;
import org.openengsb.drools.events.ScmBranchCreatedEvent;
import org.openengsb.drools.events.ScmBranchDeletedEvent;
import org.openengsb.drools.events.ScmBranchEvent;
import org.openengsb.drools.events.ScmCheckInEvent;
import org.openengsb.drools.model.MergeResult;

public class RepositoryPoller {
    private SvnConnector svn;
    private String author;
    private EventHelper eventHelper;

    private MergeResult checkoutResult;
    private List<String> branches;

    public void poll() {
        if (checkoutResult == null) {
            checkoutResult = svn.checkout(author);
        }

        inspectBranches();

        if (checkoutResult.getAdds().size() > 0) {
            ScmCheckInEvent e = new ScmCheckInEvent();
            eventHelper.sendEvent(e);
        }
    }

    private void inspectBranches() {
        List<String> newBranches = svn.listBranches();
        ScmBranchEvent e = null;
        Set<String> branchNames = null;

        if (branches == null) {
            branches = newBranches;
        } else {
            if (branches.size() < newBranches.size()) {
                e = new ScmBranchCreatedEvent();

                branchNames = new HashSet<String>(newBranches);
                branchNames.removeAll(branches);
            } else if (branches.size() > newBranches.size()) {
                e = new ScmBranchDeletedEvent();

                branchNames = new HashSet<String>(branches);
                branchNames.removeAll(newBranches);
            } else if (!newBranches.equals(branches)) {
                e = new ScmBranchAlteredEvent();

                branchNames = new HashSet<String>(branches);
                Set<String> tempNames = new HashSet<String>(branches);
                tempNames.retainAll(newBranches);
                branchNames.removeAll(tempNames);
            }

            if (e != null) {
                e.setBranches(new ArrayList<String>(branchNames));
                eventHelper.sendEvent(e);
            }
        }

        branches = newBranches;
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
