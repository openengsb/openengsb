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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * This class renders the success or error message after the processing of an
 * xlink, readable for users.
 */
public class UserResponsePage extends WebPage {
    
    public UserResponsePage(String msg, String hostId, boolean isError) {
        createPage(msg, hostId, isError);
    }
    
    public UserResponsePage(PageParameters parameters, String msg, String hostId, boolean isError) {
        createPage(msg, hostId, isError);
    }
    
    private void createPage(String msg, String hostId, boolean isError) {
        add(new Label("hostId", hostId));
        add(new Label("responseMessage", msg));
    }
}
