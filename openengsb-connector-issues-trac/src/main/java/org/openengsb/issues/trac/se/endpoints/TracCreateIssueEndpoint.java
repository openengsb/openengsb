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

package org.openengsb.issues.trac.se.endpoints;

import org.openengsb.issues.common.IssueDomain;
import org.openengsb.issues.common.endpoints.AbstractCreateIssueEndpoint;
import org.openengsb.issues.common.exceptions.IssueDomainException;
import org.openengsb.issues.trac.TracConnector;

/**
 * @org.apache.xbean.XBean element="create-issue"
 */
public class TracCreateIssueEndpoint extends AbstractCreateIssueEndpoint {

    private String url;
    private String username;
    private String password;

    private IssueDomain issueDomain;

    @Override
    protected IssueDomain createIssueDomain() throws IssueDomainException {
        if (issueDomain == null) {
            try {
                issueDomain = new TracConnector(url, username, password);
            } catch (Exception e) {
                throw new IssueDomainException("Error creating TracConnector.", e);
            }
        }
        return issueDomain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public IssueDomain getIssueDomain() {
        return issueDomain;
    }

    public void setIssueDomain(IssueDomain issueDomain) {
        this.issueDomain = issueDomain;
    }

}
