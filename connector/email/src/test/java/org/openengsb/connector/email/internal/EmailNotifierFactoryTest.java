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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.connector.email.internal.abstraction.MailAbstraction;
import org.openengsb.connector.email.internal.abstraction.MailProperties;

public class EmailNotifierFactoryTest {

    @Test
    public void testCreateEmailNotifier() throws Exception {
        MailAbstraction mailAbstraction = Mockito.mock(MailAbstraction.class);

        MailProperties propertiesMock = Mockito.mock(MailProperties.class);
        Mockito.when(mailAbstraction.createMailProperties()).thenReturn(propertiesMock);

        EmailNotifierFactory factory = new EmailNotifierFactory(mailAbstraction);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("prefix", "pre: ");
        attributes.put("smtpAuth", "true");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");

        EmailNotifier notifier = factory.createServiceInstance("id", attributes);

        Assert.assertNotNull(notifier);
        Assert.assertEquals("id", notifier.getInstanceId());

        Mockito.verify(propertiesMock).setPassword("password");
        Mockito.verify(propertiesMock).setPrefix("pre: ");
        Mockito.verify(propertiesMock).setSmtpAuth(true);
        Mockito.verify(propertiesMock).setSender("smtpSender");
        Mockito.verify(propertiesMock).setSmtpHost("smtpHost");
        Mockito.verify(propertiesMock).setSmtpPort("smtpPort");
        Mockito.verify(propertiesMock).setUser("user");
    }

    @Test
    public void testUpdateEmailNotifier() throws Exception {
        MailAbstraction mailAbstraction = Mockito.mock(MailAbstraction.class);
        EmailNotifierFactory factory = new EmailNotifierFactory(mailAbstraction);
        MailProperties propertiesMock = Mockito.mock(MailProperties.class);
        Mockito.when(mailAbstraction.createMailProperties()).thenReturn(propertiesMock);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("prefix", "pre: ");
        attributes.put("smtpAuth", "true");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");

        EmailNotifier notifier = factory.createServiceInstance("id", attributes);

        attributes.put("user", "otherValue");

        factory.updateServiceInstance(notifier, attributes);

        Mockito.verify(propertiesMock).setUser("otherValue");
    }
}
