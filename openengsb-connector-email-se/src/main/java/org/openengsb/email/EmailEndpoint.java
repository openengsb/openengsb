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
package org.openengsb.email;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.OpenEngSBEndpoint;
import org.openengsb.drools.NotificationDomain;

/**
 * @org.apache.xbean.XBean element="emailEndpoint"
 *                         description="Email Notification Endpoint"
 */
public class EmailEndpoint extends OpenEngSBEndpoint<NotificationDomain> {

    @Override
    protected NotificationDomain getImplementation(ContextHelper contextHelper) {
        Properties props = getPropertiesFromContext(contextHelper);

        String user = contextHelper.getValue("notification/email/user");
        String password = contextHelper.getValue("notification/email/password");

        return new EmailNotifier(props, user, password);
    }

    private Properties getPropertiesFromContext(ContextHelper contextHelper) {

        Map<String, String> props = contextHelper.getAllValues("notification/email/config");

        Properties properties = new Properties();
        for (Entry<String, String> entry : props.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    @Override
    protected void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws Exception {
        // TODO
    }

}
