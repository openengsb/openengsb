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
package  org.openengsb.core.workflow.domains.model;

public class Issue {
    public final static String priorityIMMEDIATE = "IMMEDIATE";
    public final static String priorityURGENT = "URGEND";
    public final static String priorityHIGH = "HIGH";
    public final static String priorityNORMAL = "NORMAL";
    public final static String priorityLOW = "LOW";
    public final static String priorityNONE = "NONE";

    public final static String statusNEW = "NEW";
    public final static String statusASSIGNED = "ASSIGNED";
    public final static String statusCLOSED = "CLOSED";

    public final static String fieldSUMMARY = "SUMMARY";
    public final static String fieldDESCRIPTION = "DESCRIPTION";
    public final static String fieldOWNER = "OWNER";
    public final static String fieldREPORTER = "REPORTER";
    public final static String fieldPRIORITY = "PRIORITY";
    public final static String fieldSTATUS = "STATUS";

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