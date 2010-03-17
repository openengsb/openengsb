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

import java.util.Date;
import java.util.List;

public class Issue {
    public enum IssuePriority {
        IMMEDIATE, URGENT, HIGH, NORMAL, LOW, NONE
    }

    public enum IssueResolution {
        OPEN, REOPENED, FIXED, INVALID, WONTFIX, DUPLICATE, WORKSFORM, UNABLETOPRODUCE, NOTFIXABLE, NOCHANGEREQUIRED, SUSPENDED
    }

    public enum IssueSeverity {
        BLOCK, CRASH, MAJOR, MINOR, TWEAK, TEXT, TRIVIAL, FEATURE
    }

    public enum IssueStatus {
        NEW, ASSIGNED, CLOSED
    }

    public enum IssueType {
        BUG, IMPROVEMENT, NEW_FEATURE, TASK
    }

    private Integer id;
    private String summary;
    private String description;
    private String reporter;
    private String owner;
    private String affectedVersion;
    private IssuePriority priority;
    private IssueSeverity severity;
    private IssueResolution resolution;
    private IssueStatus status;
    private IssueType type;
    private List<Comment> comments;
    private Date creationTime;
    private Date lastChange;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getAffectedVersion() {
        return affectedVersion;
    }

    public void setAffectedVersion(String affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    public IssueResolution getResolution() {
        return resolution;
    }

    public void setResolution(IssueResolution resolution) {
        this.resolution = resolution;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastChange() {
        return lastChange;
    }

    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

}