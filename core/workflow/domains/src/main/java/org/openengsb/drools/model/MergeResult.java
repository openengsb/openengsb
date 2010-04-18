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
package org.openengsb.drools.model;

import java.util.Collections;
import java.util.List;

/**
 * A standard POJO that holds references to files, that were changed in some
 * SCM-operation
 */
public class MergeResult {
    private String revision;
    private List<String> conflicts; // All paths to the files in conflict.
    // Relative
    // to the working copy's root.
    private List<String> merges; // All paths to the files that were merged.
    private List<String> adds; // All paths to the files that were added.
    private List<String> deletions; // All pats to the files that were deleted.

    public MergeResult() {
        // set default values to avoid NPEs
        List<String> emptyList = Collections.emptyList();
        setConflicts(emptyList);
        setMerges(emptyList);
        setAdds(emptyList);
        setDeletions(emptyList);
    }

    public List<String> getConflicts() {
        return this.conflicts;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    public List<String> getMerges() {
        return this.merges;
    }

    public void setMerges(List<String> merges) {
        this.merges = merges;
    }

    public List<String> getAdds() {
        return this.adds;
    }

    public void setAdds(List<String> adds) {
        this.adds = adds;
    }

    public List<String> getDeletions() {
        return this.deletions;
    }

    public void setDeletions(List<String> deletions) {
        this.deletions = deletions;
    }

    public String getRevision() {
        return this.revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public boolean hasConflicts() {
        return this.conflicts != null && this.conflicts.size() > 0;
    }
}
