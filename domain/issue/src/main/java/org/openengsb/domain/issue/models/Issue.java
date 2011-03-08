/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domain.issue.models;

public class Issue {



    public enum Priority implements IssueAttribute {
        IMMEDIATE,
        URGEND,
        HIGH,
        NORMAL,
        LOW,
        NONE
    }

    public enum Status implements IssueAttribute {
        NEW,
        ASSIGNED,
        CLOSED,
        UNASSIGNED

    }

    public enum Field implements IssueAttribute {
        Field,
        SUMMARY,
        DESCRIPTION,
        OWNER,
        REPORTER,
        PRIORITY,
        STATUS,
        TYPE
    }

    public enum Type implements IssueAttribute {
        BUG,
        TASK,
        NEW_FEATURE,
        IMPROVEMENT
    }

    private String id;
    private String summary;
    private String description;
    private String owner;
    private String reporter;
    private Priority priority;
    private Status status;
    private String dueVersion;
    private Type type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDueVersion() {
        return dueVersion;
    }

    public void setDueVersion(String dueVersion) {
        this.dueVersion = dueVersion;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
