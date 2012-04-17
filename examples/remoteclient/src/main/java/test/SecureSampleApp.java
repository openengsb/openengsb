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

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.DecryptionException;
import org.openengsb.core.api.security.EncryptionException;
import org.openengsb.core.api.security.model.EncryptedMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.util.CipherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import com.google.common.collect.ImmutableMap;

/**
 * Setup to run this app: + Start OpenEngSB + install the jms-feature: features:install openengsb-ports-jms + copy
 * example+example+testlog.connector to the openengsb/config-directory + copy openengsb/etc/keys/public.key.data to
 * src/main/resources
 */
public final class SecureSampleApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureSampleApp.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String URL = "tcp://127.0.0.1:6549";

    private static JmsTemplate template;

    private static void init() throws JMSException {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(URL);
        template = new JmsTemplate(cf);

    }

    private static MethodResult call(MethodCall call, String username, Credentials credentails) throws IOException,
        JMSException, InterruptedException, ClassNotFoundException, EncryptionException, DecryptionException {
        MethodCallRequest methodCallRequest = new MethodCallRequest(call);
        SecretKey sessionKey = CipherUtils.generateKey("AES", 128);
        String requestString = marshalRequest(methodCallRequest, sessionKey, username, credentails);
        String resultString = sendMessage(requestString);
        return convertStringToResult(resultString, sessionKey);
    }

    private static String sendMessage(final String encryptedMessage) {
        return template.execute(new SessionCallback<String>() {
            @Override
            public String doInJms(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(encryptedMessage);
                Queue outQueue = session.createQueue("receive");
                Destination inDest = session.createTemporaryQueue();
                String correlationID = UUID.randomUUID().toString();
                message.setJMSReplyTo(inDest);
                message.setJMSCorrelationID(correlationID);
                MessageProducer producer = session.createProducer(outQueue);
                producer.send(outQueue, message);
                return ((TextMessage) session.createConsumer(inDest).receive(10000)).getText();
            }
        }, true);
    }

    private static String marshalRequest(MethodCallRequest methodCallRequest, SecretKey sessionKey,
            String username, Credentials credentials) throws IOException, EncryptionException {
        byte[] requestString = marshalSecureRequest(methodCallRequest, username, credentials);
        EncryptedMessage encryptedMessage = encryptMessage(sessionKey, requestString);
        return MAPPER.writeValueAsString(encryptedMessage);
    }

    private static EncryptedMessage encryptMessage(SecretKey sessionKey, byte[] requestString) throws IOException,
        EncryptionException {
        PublicKey publicKey = readPublicKey();
        byte[] encryptedContent = CipherUtils.encrypt(requestString, sessionKey);
        byte[] encryptedKey = CipherUtils.encrypt(sessionKey.getEncoded(), publicKey);
        EncryptedMessage encryptedMessage = new EncryptedMessage(encryptedContent, encryptedKey);
        return encryptedMessage;
    }

    private static PublicKey readPublicKey() throws IOException {
        InputStream publicKeyResource = ClassLoader.getSystemResourceAsStream("public.key.data");
        byte[] publicKeyData = IOUtils.toByteArray(publicKeyResource);
        PublicKey publicKey = CipherUtils.deserializePublicKey(publicKeyData, "RSA");
        return publicKey;
    }

    private static byte[] marshalSecureRequest(MethodCallRequest methodCallRequest,
            String username, Credentials credentials) throws IOException {
        SecureRequest secureRequest = SecureRequest.create(methodCallRequest, username, credentials);
        return MAPPER.writeValueAsBytes(secureRequest);
    }

    private static MethodResult convertStringToResult(String resultString, SecretKey sessionKey) throws IOException,
        ClassNotFoundException, DecryptionException {
        SecureResponse resultMessage = decryptResponse(resultString, sessionKey);
        return convertResult(resultMessage);
    }

    private static MethodResult convertResult(SecureResponse resultMessage) throws ClassNotFoundException {
        MethodResult result = resultMessage.getMessage().getResult();
        return result;
    }

    private static SecureResponse decryptResponse(String resultString, SecretKey sessionKey)
        throws DecryptionException, IOException {
        byte[] decryptedContent;
        try {
            decryptedContent = CipherUtils.decrypt(Base64.decodeBase64(resultString), sessionKey);
        } catch (DecryptionException e) {
            System.err.println(resultString);
            throw e;
        }
        SecureResponse resultMessage = MAPPER.readValue(decryptedContent, SecureResponse.class);
        return resultMessage;
    }

    /**
     * Small-test client that can be used for sending jms-messages to a running openengsb
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("initializing");
        init();
        LOGGER.info("initialized");
        MethodCall methodCall = new MethodCall("doSomething", new Object[]{ "Hello World!" }, ImmutableMap.of(
            "serviceId", "example+example+testlog", "contextId", "foo"));
        LOGGER.info("calling method");
        MethodResult methodResult = call(methodCall, "admin", new Password("password"));
        System.out.println(methodResult);
    }

    private SecureSampleApp() {
    }
}
