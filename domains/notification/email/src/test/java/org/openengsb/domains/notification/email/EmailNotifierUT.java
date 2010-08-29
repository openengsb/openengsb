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
package org.openengsb.domains.notification.email;

import java.util.ArrayList;
import java.util.Date;

import javax.mail.MessagingException;

import org.junit.Test;
import org.openengsb.domains.notification.email.internal.EmailNotifier;
import org.openengsb.domains.notification.implementation.model.Attachment;
import org.openengsb.domains.notification.implementation.model.Notification;
import org.springframework.test.annotation.ExpectedException;

public class EmailNotifierUT {

    @Test
    public void testToSendAnEmail() throws Exception {
        EmailNotifier notifier = createNotifier("notifier1", "true", "openengsb.notification.test@gmail.com",
                "smtp.gmail.com", "pwd-openengsb", "openengsb.notification.test@gmail.com", "465");
        Notification notification = createNotification();
        notifier.notifyWithoutExceptionHandling(notification);
    }

    @ExpectedException(MessagingException.class)
    @Test
    public void testToSendAnEmailWithWrongUserdata() throws MessagingException {
        EmailNotifier notifier = createNotifier("notifier2", "true", "doesnotexist", "smtp.gmail.com", "totallyWrong",
                "doesnotexist", "465");
        Notification notification = createNotification();
        notifier.notifyWithoutExceptionHandling(notification);
    }

    private EmailNotifier createNotifier(String name, String smtpAuth, String smtpSender, String smtpHost,
            String smtpPassword, String smtpUser, String smtpPort) {
        EmailNotifier notifier = new EmailNotifier(name);
        notifier.setSmtpAuth(smtpAuth);
        notifier.setSmtpSender(smtpSender);
        notifier.setSmtpHost(smtpHost);
        notifier.setPassword(smtpPassword);
        notifier.setUser(smtpUser);
        notifier.setSmtpPort(smtpPort);
        return notifier;
    }

    private Notification createNotification() {
        Notification notification = new Notification();
        notification.setRecipient("openengsb.notification.test@gmail.com");
        notification.setSubject("TestMail send on " + new Date());
        notification.setMessage("This is a test mail");
        notification.setAttachments(new ArrayList<Attachment>());
        return notification;
    }
}
