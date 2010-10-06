/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.issue.trac.internal.models;

public class Issue {
    
    public static final String PRIORITYIMMEDIATE = "IMMEDIATE";
    public static final String PRIORITYURGENT = "URGEND";
    public static final String PRIORITYHIGH = "HIGH";
    public static final String PRIORITYNORMAL = "NORMAL";
    public static final String PRIORITYLOW = "LOW";
    public static final String PRIORITYNONE = "NONE";

    public static final String STATUSNEW = "NEW";
    public static final String STATUSASSIGNED = "ASSIGNED";
    public static final String STATUSCLOSED = "CLOSED";

    public static final String FIELDSUMMARY = "SUMMARY";
    public static final String FIELDDESCRIPTION = "DESCRIPTION";
    public static final String FIELDOWNER = "OWNER";
    public static final String FIELDREPORTER = "REPORTER";
    public static final String FIELDPRIORITY = "PRIORITY";
    public static final String FIELDSTATUS = "STATUS";

    private Integer id;
    private String summary;
    private String description;
    private String owner;
    private String reporter;
    private String priority;
    private String status;

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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}