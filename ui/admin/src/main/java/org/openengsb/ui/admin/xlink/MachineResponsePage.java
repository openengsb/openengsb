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
package org.openengsb.ui.admin.xlink;


import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * This class renders the webpage´s response during a XLink 'local-switch'.
 * It is adapted to be processable for machine-clients. The response contains 
 * the message as body and the HTTP-Status code as header.
 */
public class MachineResponsePage extends WebPage {
    
    boolean isSuccess;
    
    public MachineResponsePage(String msg, boolean isSuccess) {
        this.isSuccess = isSuccess;
        createPage(msg);
    }
    
    public MachineResponsePage(PageParameters parameters, String msg, boolean isSuccess) {
        this.isSuccess = isSuccess;
        createPage(msg);
    }
    
    private void createPage(String msg) {
        add(new Label("message", msg));
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        if (!isSuccess) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    

    @Override
    public boolean isVersioned() {
        return isSuccess;
    }

    @Override
    public boolean isErrorPage() {
        return !isSuccess;
    }     
    
}
