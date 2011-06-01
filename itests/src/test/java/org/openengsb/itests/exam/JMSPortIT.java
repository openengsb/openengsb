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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.security.CipherUtils;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.springframework.jms.core.JmsTemplate;

@RunWith(JUnit4TestRunner.class)
public class JMSPortIT extends AbstractExamTestHelper {

    private static final String METHOD_CALL_STRING = ""
            + "{"
            + "    \"classes\": ["
            + "        \"java.lang.String\","
            + "        \"org.openengsb.core.api.workflow.model.ProcessBag\""
            + "    ],"
            + "    \"methodName\": \"executeWorkflow\","
            + "    \"metaData\": {"
            + "        \"serviceId\": \"workflowService\","
            + "        \"contextId\": \"foo\""
            + "    },"
            + "    \"args\": ["
            + "        \"simpleFlow\","
            + "        {"
            + "        }"
            + "    ]"
            + "}";

    private RuleManager ruleManager;

    @Before
    public void setUp() throws Exception {
        ruleManager = getOsgiService(RuleManager.class);
    }

    @Ignore
    @Test
    public void jmsPort_shouldBeExportedWithCorrectId() throws Exception {
        OutgoingPort serviceWithId =
            OpenEngSBCoreServices.getServiceUtilsService().getServiceWithId(OutgoingPort.class, "jms-json", 60000);
        assertNotNull(serviceWithId);
    }

    @Ignore
    @Test
    public void startSimpleWorkflow_ShouldReturn42() throws Exception {
        addWorkflow("simpleFlow");
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        String secureRequest = prepareRequest(METHOD_CALL_STRING, "admin", "password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        template.convertAndSend("receive", encryptedMessage);
        String result = (String) template.receiveAndConvert("12345");

        byte[] resultData = Base64.decodeBase64(result);
        assertThat(result, not(containsString("The answer to life the universe and everything")));
        byte[] decryptedResultData = CipherUtils.decrypt(resultData, sessionKey);
        result = new String(decryptedResultData);
        assertThat(result, containsString("The answer to life the universe and everything"));
    }

    @Test
    public void testSendMethodCallWithWrongAuthentication_shouldFail() throws Exception {
        addWorkflow("simpleFlow");
        ActiveMQConnectionFactory cf =
            new ActiveMQConnectionFactory("failover:(tcp://localhost:6549)?timeout=60000");
        JmsTemplate template = new JmsTemplate(cf);
        String secureRequest = prepareRequest(METHOD_CALL_STRING, "admin", "wrong-password");
        SecretKey sessionKey = generateSessionKey();
        String encryptedMessage = encryptMessage(secureRequest, sessionKey);

        template.convertAndSend("receive", encryptedMessage);
        String result = (String) template.receiveAndConvert("12345");

        assertThat(result, containsString("Exception"));
        assertThat(result, not(containsString("The answer to life the universe and everything")));
    }

    private String encryptMessage(String secureRequest, SecretKey sessionKey)
        throws EncryptionException, InterruptedException, IOException {
        PublicKey publicKey = getPublicKeyFromConfigFile();
        String encodedMessage = Base64.encodeBase64String(CipherUtils.encrypt(secureRequest.getBytes(), sessionKey));
        String encodedKey = Base64.encodeBase64String(CipherUtils.encrypt(sessionKey.getEncoded(), publicKey));

        String encryptedMessage = ""
                + "{"
                + "  \"encryptedContent\":\"" + encodedMessage + "\","
                + "  \"encryptedKey\":\"" + encodedKey + "\""
                + "}";
        return encryptedMessage;
    }

    private SecretKey generateSessionKey() {
        SecretKey sessionKey =
            CipherUtils.generateKey(CipherUtils.DEFAULT_SYMMETRIC_ALGORITHM, CipherUtils.DEFAULT_SYMMETRIC_KEYSIZE);
        return sessionKey;
    }

    private String prepareRequest(String methodCall, String username, String password) {
        String request = ""
                + "{"
                + "  \"callId\":\"12345\","
                + "  \"answer\":true,"
                + "  \"methodCall\":" + methodCall
                + "}";

        String authInfo = ""
                + "{"
                + "  \"className\":\"org.openengsb.core.api.security.model.UsernamePasswordAuthenticationInfo\","
                + "  \"data\":"
                + "  {"
                + "    \"username\":\"" + username + "\","
                + "    \"password\":\"" + password + "\""
                + "  }"
                + "}";

        String secureRequest = ""
                + "{"
                + "  \"authenticationData\":" + authInfo + ","
                + "  \"timestamp\":" + System.currentTimeMillis() + ","
                + "  \"message\":" + request
                + "}";
        return secureRequest;
    }

    private PublicKey getPublicKeyFromConfigFile() throws InterruptedException, IOException {
        // FIXME do this properly when OPENENGSB-1597 is resolved
        File file = new File(System.getProperty("karaf.home"), "/etc/keys/public.key.data");
        while (!file.exists()) {
            Thread.sleep(1000);
        }
        byte[] keyData;

        keyData = FileUtils.readFileToByteArray(file);
        PublicKey publicKey = CipherUtils.deserializePublicKey(keyData, CipherUtils.DEFAULT_ASYMMETRIC_ALGORITHM);
        return publicKey;
    }

    private void addWorkflow(String workflow) throws IOException, RuleBaseException {
        if (ruleManager.get(new RuleBaseElementId(RuleBaseElementType.Process, workflow)) == null) {
            InputStream is =
                getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/" + workflow + ".rf");
            String testWorkflow = IOUtils.toString(is);
            RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Process, workflow);
            ruleManager.add(id, testWorkflow);
            IOUtils.closeQuietly(is);
        }
    }
}
