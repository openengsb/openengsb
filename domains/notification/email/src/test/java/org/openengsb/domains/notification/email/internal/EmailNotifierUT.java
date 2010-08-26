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
package org.openengsb.domains.notification.email.internal;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.domains.notification.implementation.model.Attachment;
import org.openengsb.domains.notification.implementation.model.Notification;

import java.util.ArrayList;
import java.util.Date;

public class EmailNotifierUT {

    @Before
    public void setUp() {

    }

    @Test
    public void testToSendAnEmail() {
        EmailNotifier notifier = new EmailNotifier("notifier");
        notifier.setSmtpAuth("true");
        notifier.setSmtpStarttls("true");
        notifier.setSmtpUser("openengsb.notification.test@gmail.com");
        notifier.setSmtpHost("smtp.gmail.com");
        notifier.setPassword("pwd-openengsb");
        notifier.setUser("openengsb.notification.test@gmail.com");

        Notification not = new Notification();
        not.setRecipient("my@gmail.com");
        not.setSubject("TestMail" + new Date());
        not.setMessage("This is a test mail");
        not.setAttachments(new ArrayList<Attachment>());

        notifier.notify(not);
    }
}
