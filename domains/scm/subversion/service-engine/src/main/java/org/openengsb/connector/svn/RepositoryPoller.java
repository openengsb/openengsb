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

import java.util.List;

import org.openengsb.drools.model.MergeResult;

public class RepositoryPoller {
    private MergeResult checkoutResult;
    private List<String> branches;
    private SvnConnector svn;

    public void poll() {
        if (checkoutResult == null) {
            checkoutResult = svn.checkout("openengsb");
        }

        List<String> tempbranches = svn.listBranches();
        if (branches == null) {
            branches = tempbranches;
        } else {
            if (branches.size() < tempbranches.size()) {
                // Got new branches - create event
            } else if (branches.size() > tempbranches.size()) {
                // Lost some branches - create event
            } else {
                for (int i = 0; i < branches.size(); i++) {
                    if (!branches.get(i).equals(tempbranches.get(i))) {
                        // Branch changed - create event
                    }
                }
            }
        }

        branches = tempbranches;

        if (checkoutResult.getAdds().size() > 0) {
            // Got new adds - create event
        }
    }

    public void setConfiguration(SvnConfiguration configuration) {
        svn = new SvnConnector(configuration);
    }

}
