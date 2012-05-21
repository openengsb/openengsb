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
package org.openengsb.infrastructure.jms.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.stomp.StompConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class JmsInfrastructureTest extends AbstractOsgiMockServiceTest {

    private static final int STOMP_PORT = 16550;
    private static final int OPENWIRE_PORT = 16549;
    @Rule
    public TemporaryFolder tempfolder = new TemporaryFolder();
    private Activator activator;

    @Before
    public void setUp() throws Exception {
        System.setProperty("karaf.data", tempfolder.getRoot().getAbsolutePath());

        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        Configuration configuration = mock(Configuration.class);
        when(configAdmin.getConfiguration("org.openengsb.infrastructure.jms")).thenReturn(configuration);
        Properties configProperties = new Properties();
        configProperties.put("openwire", OPENWIRE_PORT);
        configProperties.put("stomp", STOMP_PORT);
        when(configuration.getProperties()).thenReturn(configProperties);
        registerService(configAdmin, new Hashtable<String, Object>(), ConfigurationAdmin.class);

        activator = new Activator();
        activator.start(bundleContext);
        activator.startThread.join();
    }

    @After
    public void tearDown() throws Exception {
        activator.stop(bundleContext);
    }

    @Test
    public void startBroker_shouldCreateDataDirectories() throws Exception {
        assertThat(new File(tempfolder.getRoot(), "activemq/openengsb").exists(), is(true));
        assertThat(new File(tempfolder.getRoot(), "activemq/openengsb/kahadb").exists(), is(true));
    }

    @Test
    public void connectOpenWire_shouldConnect() throws Exception {
        ActiveMQConnectionFactory activeMQConnectionFactory =
            new ActiveMQConnectionFactory("tcp://localhost:" + OPENWIRE_PORT);
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        session.close();
        connection.stop();
        connection.close();
    }

    @Test
    public void connectStomp_shouldConnect() throws Exception {
        StompConnection connection = new StompConnection();
        connection.open("localhost", STOMP_PORT);
        connection.close();
    }
}
