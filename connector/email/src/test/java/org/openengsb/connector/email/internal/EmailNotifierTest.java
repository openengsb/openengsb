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

package org.openengsb.connector.email.internal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Test;
import org.openengsb.connector.email.internal.abstraction.MailAbstraction;
import org.openengsb.connector.email.internal.abstraction.MailProperties;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.DomainMethodExecutionException;
import org.openengsb.domain.notification.model.Attachment;
import org.openengsb.domain.notification.model.Notification;

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

    @Test(expected = DomainMethodExecutionException.class)
    public void testThatDomainMethodExecutionExceptionIsThrown() throws Exception {
        MailAbstraction mailMock = mock(MailAbstraction.class);
        MailProperties propertiesMock = mock(MailProperties.class);
        when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = new EmailNotifier("", mailMock);

        Notification notificationMock = mock(Notification.class);

        doThrow(new DomainMethodExecutionException()).when(mailMock).send(any(MailProperties.class), anyString(),
                anyString(), anyString());

        notifier.notify(notificationMock);
    }

    @Test
    public void testAliveStateAfterEmailNotifierCreation_ShouldReturnOnline() {
        MailAbstraction mailMock = mock(MailAbstraction.class);
        MailProperties propertiesMock = mock(MailProperties.class);
        when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = new EmailNotifier("notifier1", mailMock);
        when(mailMock.getAliveState()).thenReturn(AliveState.ONLINE);
        assertThat(notifier.getAliveState(), is(AliveState.ONLINE));
    }

    @Test
    public void testemailNotifierShouldBeOnlineAfterFirstSend_ShouldReturnOnline() {
        MailAbstraction mailMock = mock(MailAbstraction.class);
        MailProperties propertiesMock = mock(MailProperties.class);
        when(mailMock.createMailProperties()).thenReturn(propertiesMock);
        EmailNotifier notifier = new EmailNotifier("notifier1", mailMock);

        Notification notification = new Notification();
        notification.setRecipient("openengsb.notification.test@gmail.com");
        notification.setSubject("Subject");
        notification.setMessage("Content");
        notification.setAttachments(new ArrayList<Attachment>());

        when(mailMock.getAliveState()).thenReturn(AliveState.ONLINE);
        notifier.notify(notification);
        assertThat(notifier.getAliveState(), is(AliveState.ONLINE));
    }

}
