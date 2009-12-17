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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.core.OpenEngSBEndpoint;
import org.openengsb.drools.NotificationDomain;

/**
 * @org.apache.xbean.XBean element="emailEndpoint"
 *                         description="Email Notification Endpoint"
 */
public class EmailEndpoint extends OpenEngSBEndpoint<NotificationDomain> {

    private EmailNotifier emailNotifier = new EmailNotifier("smtp.gmail.com", "openengsb@gmail.com", "pwd");

    @Override
    protected NotificationDomain getImplementation() {
        return emailNotifier;
    }

    @Override
    protected void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        // TODO
    }

}
