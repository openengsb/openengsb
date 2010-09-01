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

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.DomainMethodExecutionException;
import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;
import org.openengsb.domains.notification.email.internal.abstraction.MailProperties;
import org.openengsb.domains.notification.model.Attachment;
import org.openengsb.domains.notification.model.Notification;
import org.springframework.test.annotation.ExpectedException;

import java.util.ArrayList;

public class EmailNotifierTest {

    @Test
    public void testToSendAnEmail() throws Exception {
        MailAbstraction mailMock = Mockito.mock(MailAbstraction.class);
        MailProperties propertiesMock = Mockito.mock(MailProperties.class);
        Mockito.when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = new EmailNotifier("notifier1", mailMock);

        Notification notification = new Notification();
        notification.setRecipient("openengsb.notification.test@gmail.com");
        notification.setSubject("Subject");
        notification.setMessage("Content");
        notification.setAttachments(new ArrayList<Attachment>());

        notifier.notify(notification);
        Mockito.verify(mailMock).send(propertiesMock, "Subject", "Content", "openengsb.notification.test@gmail.com");

    }

    @Test
    @ExpectedException(DomainMethodExecutionException.class)
    public void testThatDomainMethodExecutionExceptionIsThrown() throws Exception {
        MailAbstraction mailMock = Mockito.mock(MailAbstraction.class);
        MailProperties propertiesMock = Mockito.mock(MailProperties.class);
        Mockito.when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = Mockito.mock(EmailNotifier.class);

        Notification notificationMock = Mockito.mock(Notification.class);

        Mockito.doThrow(new DomainMethodExecutionException()).when(mailMock).
                send(Mockito.<MailProperties>anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        notifier.notify(notificationMock);
    }

}
