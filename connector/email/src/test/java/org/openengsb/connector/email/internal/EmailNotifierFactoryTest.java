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

package org.openengsb.connector.email.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openengsb.connector.email.internal.abstraction.MailAbstraction;
import org.openengsb.connector.email.internal.abstraction.MailProperties;
import org.openengsb.core.common.AliveState;

public class EmailNotifierFactoryTest {

    public static class MailAbstractionImp implements MailAbstraction {
        MailProperties props = mock(MailProperties.class);
        
        @Override
        public void send(MailProperties properties, String subject, String textContet, String receiver) { }

        @Override
        public void connect(MailProperties properties) { }

        @Override
        public MailProperties createMailProperties() {
            return props;
        }

        @Override
        public AliveState getAliveState() {
            return null;
        }
    }
    
    @Test
    public void testCreateEmailNotifier() throws Exception {
        EmailNotifierFactory factory = new EmailNotifierFactory(MailAbstractionImp.class);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("prefix", "pre: ");
        attributes.put("smtpAuth", "true");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");
        attributes.put("secureMode", "SSL");

        EmailNotifier notifier = factory.createServiceInstance("id", attributes);
        MailProperties propertiesMock = notifier.getProperties();
        
        assertNotNull(notifier);
        assertEquals("id", notifier.getInstanceId());

        verify(propertiesMock).setPassword("password");
        verify(propertiesMock).setPrefix("pre: ");
        verify(propertiesMock).setSmtpAuth(true);
        verify(propertiesMock).setSender("smtpSender");
        verify(propertiesMock).setSmtpHost("smtpHost");
        verify(propertiesMock).setSmtpPort("smtpPort");
        verify(propertiesMock).setUser("user");
        verify(propertiesMock).setSecureMode("SSL");
    }
    
    @Test
    public void testUpdateEmailNotifier() throws Exception {
        EmailNotifierFactory factory = new EmailNotifierFactory(MailAbstractionImp.class);
    
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("user", "user");
        attributes.put("password", "password");
        attributes.put("prefix", "pre: ");
        attributes.put("smtpAuth", "true");
        attributes.put("smtpSender", "smtpSender");
        attributes.put("smtpHost", "smtpHost");
        attributes.put("smtpPort", "smtpPort");
    
        EmailNotifier notifier = factory.createServiceInstance("id", attributes);
        MailProperties propertiesMock = notifier.getProperties();
    
        attributes.put("user", "otherValue");
    
        factory.updateServiceInstance(notifier, attributes);
    
        verify(propertiesMock).setUser("otherValue");
    }
}
