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

package org.openengsb.issues.common.messages;

public class CreateIssueResponseMessage {
    private String createdIssueId;
    private CreateIssueStatus status;
    private String statusMessage;

    public CreateIssueResponseMessage() {
    }

    public CreateIssueResponseMessage(String createdIssueId, CreateIssueStatus status, String statusMessage) {
        this.createdIssueId = createdIssueId;
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public void setCreatedIssueId(String createdIssueId) {
        this.createdIssueId = createdIssueId;
    }

    public String getCreatedIssueId() {
        return createdIssueId;
    }

    public void setStatus(CreateIssueStatus status) {
        this.status = status;
    }

    public CreateIssueStatus getStatus() {
        return status;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}