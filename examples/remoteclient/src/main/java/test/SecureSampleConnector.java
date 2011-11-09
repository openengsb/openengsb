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

package test;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Setup to run this app:
 *
 * + Start OpenEngSB
 *
 * + install the jms-feature: features:install openengsb-ports-jms
 *
 * + copy example+external-connector-proxy+example-remote.connector to the openengsb/config-directory
 *
 * + copy openengsb/etc/keys/public.key.data to src/main/resources
 */
public final class SecureSampleConnector {

    static final Logger LOGGER = LoggerFactory.getLogger(SecureSampleConnector.class);
    private static final String URL = "tcp://127.0.0.1:6549";
    private static JmsConfig jmsConfig;

    private static void init() throws Exception {
        jmsConfig = new JmsConfig(URL);
        jmsConfig.init();
        jmsConfig.createConsumerForQueue("example-remote", new ConnectorMessageListener(jmsConfig));
    }

    private static void stop() throws JMSException {
        jmsConfig.stop();
    }

    /**
     * Small-test client that can be used for sending jms-messages to a running openengsb
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("initializing");
        init();
        LOGGER.info("initialized");
        System.in.read();
        LOGGER.info("stopping");
        stop();
        LOGGER.info("done");
    }

    private SecureSampleConnector() {
    }
}
