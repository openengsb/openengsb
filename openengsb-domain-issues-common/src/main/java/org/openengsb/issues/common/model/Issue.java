/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.issues.common.model;

public class Issue {

    private String summary;
    private String description;
    private String reporter;
    private String owner;
    private String type;
    private String priority;

    public Issue() {
    }

    public Issue(String summary, String description, String reporter, String owner, String type, String priority) {
        this.summary = summary;
        this.description = description;
        this.reporter = reporter;
        this.owner = owner;
        this.type = type;
        this.priority = priority;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Issue))
            return false;

        Issue other = (Issue) obj;

        return (this.summary.equals(other.summary) && this.description.equals(other.description)
                && this.reporter.equals(other.reporter) && this.owner.equals(other.owner)
                && this.type.equals(other.type) && this.priority.equals(other.priority));
    }
}
