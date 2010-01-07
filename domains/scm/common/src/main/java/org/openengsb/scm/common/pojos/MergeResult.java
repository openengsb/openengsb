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
package org.openengsb.scm.common.pojos;

/**
 * A standard POJO that holds references to files, that were changed in some
 * SCM-operation
 */
public class MergeResult {
    private String revision;
    private String[] conflicts; // All paths to the files in conflict. Relative
    // to the working copy's root.
    private String[] merges; // All paths to the files that were merged.
    private String[] adds; // All paths to the files that were added.
    private String[] deletions; // All pats to the files that were deleted.

    public MergeResult() {
        // set default values to avoid NPEs
        String[] emptyArray = new String[0];
        setConflicts(emptyArray);
        setMerges(emptyArray);
        // setUpdates (emptyArray);
        setAdds(emptyArray);
        setDeletions(emptyArray);
    }

    // public String getStringResult()
    // {
    // return stringResult;
    // }
    // public void setStringResult (String stringResult)
    // {
    // this.stringResult = stringResult;
    // }

    public String[] getConflicts() {
        return this.conflicts;
    }

    public void setConflicts(String[] conflicts) {
        this.conflicts = conflicts;
    }

    public String[] getMerges() {
        return this.merges;
    }

    public void setMerges(String[] merges) {
        this.merges = merges;
    }

    // public String[] getUpdates()
    // {
    // return updates;
    // }
    // public void setUpdates (String[] updates)
    // {
    // this.updates = updates;
    // }

    public String[] getAdds() {
        return this.adds;
    }

    public void setAdds(String[] adds) {
        this.adds = adds;
    }

    public String[] getDeletions() {
        return this.deletions;
    }

    public void setDeletions(String[] deletions) {
        this.deletions = deletions;
    }

    public String getRevision() {
        return this.revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
}
