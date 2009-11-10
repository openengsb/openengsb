/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.drools.MessageHelper;

@SuppressWarnings("serial")
public class MessageHelperImpl implements MessageHelper {

    private static Log log = LogFactory.getLog(MessageHelperImpl.class);

    @Override
    public boolean createIssue(String subject, String body) {
        log.info("would create issue " + subject);
        log.info("issue-text: ");
        log.info(body);
        return true;
    }

    @Override
    public boolean sendNotification(String email, String subject, String body) {
        log.info("would send email-notification to " + email);
        log.info("Subject: " + subject);
        log.info("Body: " + body);
        return true;
    }
}
