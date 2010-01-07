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

package org.openengsb.issues.common.model;

import java.util.Date;
import java.util.List;

public class Issue {

    private String id;
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
    private Project project;
    private Date creationTime;
    private Date lastChange;

    public Issue() {
    }

    public Issue(String summary, String description, String reporter, String owner, IssueType type,
            IssuePriority priority, IssueSeverity severity, String affectedVersion) {
        this.summary = summary;
        this.description = description;
        this.reporter = reporter;
        this.owner = owner;
        this.type = type;
        this.priority = priority;
        this.severity = severity;
        this.affectedVersion = affectedVersion;
    }

    /**
     * Supplies the identifier of the issue
     *
     * @return id - identifier of the issue
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the issue to identify it.
     *
     * @param id - the id identifies the issue
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the summary of the issue
     *
     * @return summary - short issue description
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary, a short description of the issue
     *
     * @param summary - short description
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Return the detailed issue description
     *
     * @return description - detailed description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the detailed description of the issue
     *
     * @param description - detailed description of the issue
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the reporter of the issue
     *
     * @return reporter - author of the issue
     */
    public String getReporter() {
        return reporter;
    }

    /**
     * Sets the reporter(user of the issue tracker) of the issue
     *
     * @param reporter - the author of the issue
     */
    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    /**
     * Return the owner of the issue
     *
     * @return owner - person who has assigned to
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the issue
     *
     * @param owner - the person who has assigned to
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets the version of the product in which the topic of this issue
     * occurred.
     *
     * @return
     */
    public String getAffectedVersion() {
        return affectedVersion;
    }

    /**
     * Sets the version of the product in which the topic of this issue
     * occurred.
     *
     * @param affectedVersion
     */
    public void setAffectedVersion(String affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    /**
     * Returns the priority of the issue
     *
     * @return priority - the priority of the issue
     * @see IssuePriority
     */
    public IssuePriority getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the issue
     *
     * @param priority - uses the enumeration values for setting
     * @see IssuePriority
     */
    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }

    /**
     * Returns the severity of the issue
     *
     * @return severity - the severity of the issue
     * @see IssueSeverity
     */
    public IssueSeverity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity of the issue
     *
     * @param severity - uses the enumeration values for setting
     * @see IssueSeverity
     */
    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    /**
     * Returns the resolution of the issue
     *
     * @return resolution - the resolution of the issue
     * @see IssueResolution
     */
    public IssueResolution getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution of the issue
     *
     * @param resolution - uses the enumeration values for setting
     * @see IssueResolution
     */
    public void setResolution(IssueResolution resolution) {
        this.resolution = resolution;
    }

    /**
     * Returns the status of the issue
     *
     * @return status - the status of the issue
     * @see IssueStatus
     */
    public IssueStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the issue
     *
     * @param status - uses the enumeration values for setting
     * @see IssueStatus
     */
    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    /**
     * Returns the type of the issue
     *
     * @return type - the type of the issue
     * @see IssueType
     */
    public IssueType getType() {
        return type;
    }

    /**
     * Sets the type of the issue
     *
     * @param type - the type of the issue
     */
    public void setType(IssueType type) {
        this.type = type;
    }

    /**
     * Returns a list of comments added to the issue
     *
     * @return comments - a list of comments
     * @see Comment
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Sets the comments that are added to the issue At the creation time of
     * course null
     *
     * @param comments - a list of comments, default null
     * @see Comment
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * Returns the project the issue is being filed against
     *
     * @return project - the project the issue is being filed against
     * @see Project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project of issue
     *
     * @param project - the project the issue is being filed against
     * @see IsProjectTrackable
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Returns the creation time of the issue
     *
     * @return creationTime - time the issue was created
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the creation time of the issue due to the issue tracker
     *
     * @param creationTime - time the issue was created
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Returns the last change time
     *
     * @return lastChange - the time the last change was committed
     */
    public Date getLastChange() {
        return lastChange;
    }

    /**
     * Sets the time the last change was updated
     *
     * @param lastChange - the time the last change was updated
     */
    public void setLastChange(Date lastChange) {
        this.lastChange = lastChange;
    }

    /**
     * Compares this Issue to another Issue
     *
     * @param obj
     * @return true if the given Issue is equal to this Issue, false otherwise
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Issue))
            return false;

        Issue other = (Issue) obj;

        return (equalOrBothNull(this.id, other.id) && equalOrBothNull(this.summary, other.summary)
                && equalOrBothNull(this.description, other.description)
                && equalOrBothNull(this.reporter, other.reporter) && equalOrBothNull(this.owner, other.owner)
                && equalOrBothNull(this.priority, other.priority) && equalOrBothNull(this.type, other.type)
                && equalOrBothNull(this.severity, other.severity) && equalOrBothNull(this.affectedVersion,
                other.affectedVersion));
    }

    private boolean equalOrBothNull(Object o1, Object o2) {
        return ((o1 == null && o2 == null) || o1.equals(o2));
    }
}
