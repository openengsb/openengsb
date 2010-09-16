/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.notification.email.internal;

import java.util.ArrayList;

import org.junit.Test;
import org.openengsb.core.common.DomainMethodExecutionException;
import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;
import org.openengsb.domains.notification.email.internal.abstraction.MailProperties;
import org.openengsb.domains.notification.model.Attachment;
import org.openengsb.domains.notification.model.Notification;
import org.springframework.test.annotation.ExpectedException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailNotifierTest {

    @Test
    public void testToSendAnEmail() throws Exception {
        MailAbstraction mailMock = mock(MailAbstraction.class);
        MailProperties propertiesMock = mock(MailProperties.class);
        when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = new EmailNotifier("notifier1", mailMock);

        Notification notification = new Notification();
        notification.setRecipient("openengsb.notification.test@gmail.com");
        notification.setSubject("Subject");
        notification.setMessage("Content");
        notification.setAttachments(new ArrayList<Attachment>());

        notifier.notify(notification);
        verify(mailMock).send(propertiesMock, "Subject", "Content", "openengsb.notification.test@gmail.com");

    }

    @Test
    @ExpectedException(DomainMethodExecutionException.class)
    public void testThatDomainMethodExecutionExceptionIsThrown() throws Exception {
        MailAbstraction mailMock = mock(MailAbstraction.class);
        MailProperties propertiesMock = mock(MailProperties.class);
        when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = mock(EmailNotifier.class);

        Notification notificationMock = mock(Notification.class);

        doThrow(new DomainMethodExecutionException()).when(mailMock).send(any(MailProperties.class), anyString(),
                anyString(), anyString());

        notifier.notify(notificationMock);
    }

}
