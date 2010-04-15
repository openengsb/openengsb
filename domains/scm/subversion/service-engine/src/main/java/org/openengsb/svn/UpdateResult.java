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
package org.openengsb.svn;

import java.util.ArrayList;
import java.util.List;

public class UpdateResult {
    private List<String> addedBranches = new ArrayList<String>();
    private List<String> addedTags = new ArrayList<String>();
    private List<String> deletedBranches = new ArrayList<String>();
    private List<String> deletedTags = new ArrayList<String>();
    private List<String> committed = new ArrayList<String>();

    public List<String> getAddedBranches() {
        return addedBranches;
    }

    public void setAddedBranches(List<String> addedBranches) {
        this.addedBranches = addedBranches;
    }

    public List<String> getAddedTags() {
        return addedTags;
    }

    public void setAddedTags(List<String> addedTags) {
        this.addedTags = addedTags;
    }

    public List<String> getDeletedBranches() {
        return deletedBranches;
    }

    public void setDeletedBranches(List<String> deletedBranches) {
        this.deletedBranches = deletedBranches;
    }

    public List<String> getDeletedTags() {
        return deletedTags;
    }

    public void setDeletedTags(List<String> deletedTags) {
        this.deletedTags = deletedTags;
    }

    public List<String> getCommitted() {
        return committed;
    }

    public void setCommitted(List<String> committed) {
        this.committed = committed;
    }

}
