/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.openengsb.xmpp.test;

import java.io.ByteArrayInputStream;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openengsb.xmpp.XmppNotifier;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class XmppNotifierIT extends SpringTestSupport {
    private static ServiceMixClient client;

    private final String TEST_EVENT = getTestEvent();
    private XmppNotifier xmppNotifier;

    private XMPPConnection conn;
    private ChatManager chatManager;
    private Chat chat;
    private FileTransferManager transferManager;
    private OutgoingFileTransfer transfer;

    private final String namespace = "urn:test";
    private final String name = "xmpp";
    private final String testMsg = "Testmessage";
    private final String testRecipient = "test@satellite";
    private final Integer numAttachments = 1;

    private String user;
    private String password;
    private String resources;

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("testXBean.xml");
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        xmppNotifier = (XmppNotifier) context.getBean("xmppNotifier");
        user = (String) context.getBean("user");
        password = (String) context.getBean("password");
        resources = (String) context.getBean("resources");

        client = new DefaultServiceMixClient(this.jbi);

        initializeMockedObjects();

        xmppNotifier.setConnection(conn);
        xmppNotifier.setTransferManager(transferManager);

        setupMocking();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testNotifyConnect() throws Exception {
        sendMessage();
        Mockito.verify(conn, Mockito.times(1)).connect();
    }

    @Test
    public void testNotifyLogin() throws XMPPException, MessagingException {
        sendMessage();
        Mockito.verify(conn, Mockito.times(1)).login(Mockito.eq(user), Mockito.eq(password), Mockito.eq(resources));
    }

    @Test
    public void testNotifyGetChatManager() throws XMPPException, MessagingException {
        sendMessage();
        Mockito.verify(conn, Mockito.times(1)).getChatManager();
    }

    @Test
    public void testNotifyCreateChat() throws XMPPException, MessagingException {
        sendMessage();
        Mockito.verify(chatManager, Mockito.times(1)).createChat(Mockito.eq(testRecipient),
                (MessageListener) Mockito.anyObject());
    }

    @Test
    public void testNotifySendMessage() throws XMPPException, MessagingException {
        sendMessage();
        Message message = new Message();
        message.setBody(testMsg);
        Mockito.verify(chat, Mockito.times(1)).sendMessage(Mockito.eq(message));
    }

    @Test
    public void testNotifyCreateOutgoingFileTransfer() throws XMPPException, MessagingException {
        sendMessage();
        Mockito.verify(transferManager, Mockito.times(numAttachments)).createOutgoingFileTransfer(testRecipient);
    }

    @Test
    public void testNotifySendStream() throws XMPPException, MessagingException {
        sendMessage();
        Mockito.verify(transfer, Mockito.times(numAttachments)).sendStream((ByteArrayInputStream) Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    private void sendMessage() throws MessagingException {
        InOut me = client.createInOutExchange();
        me.setService(new QName(namespace, name));
        me.getInMessage().setContent(new StringSource(TEST_EVENT));
        me.setOperation(new QName("methodcall"));
        me.getInMessage().setProperty("operation", Boolean.TRUE);
        client.sendSync(me);
        assertNotSame("Exchange was not processed correctly", ExchangeStatus.ERROR, me.getStatus());
    }

    private String getTestEvent() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><XMLMethodCall><methodName>notify</methodName><args>"
                + "<type>org.openengsb.drools.model.Notification</type><value><bean>"
                + "<className>org.openengsb.drools.model.Notification</className>"
                + "<fields><fieldName>subject</fieldName><value><null>null</null></value></fields>"
                + "<fields><fieldName>message</fieldName><value><primitive><string>" + testMsg
                + "</string></primitive><id>1</id></value></fields><fields>"
                + "<fieldName>recipient</fieldName><value><primitive><string>" + testRecipient
                + "</string></primitive><id>2</id></value></fields><fields>"
                + "<fieldName>attachments</fieldName><value><list><mappable><bean>"
                + "<className>org.openengsb.drools.model.Attachment</className><fields>"
                + "<fieldName>data</fieldName><value><primitive><base64Binary>dGVzdGZpbGU=</base64Binary>"
                + "</primitive><id>5</id></value></fields><fields><fieldName>type</fieldName>"
                + "<value><primitive><string>txt</string></primitive><id>6</id></value></fields>"
                + "<fields><fieldName>name</fieldName><value><primitive><string>file1.txt</string></primitive>"
                + "<id>7</id></value></fields></bean><id>4</id></mappable></list><id>3</id></value></fields>"
                + "</bean><id>0</id></value></args></XMLMethodCall>";
    }

    private void initializeMockedObjects() {
        conn = Mockito.mock(XMPPConnection.class);
        chatManager = Mockito.mock(ChatManager.class);
        chat = Mockito.mock(Chat.class);
        transferManager = Mockito.mock(FileTransferManager.class);
        transfer = Mockito.mock(OutgoingFileTransfer.class);
    }

    private void setupMocking() {
        Mockito.when(conn.getChatManager()).thenReturn(chatManager);
        Mockito.when(chatManager.createChat(Mockito.anyString(), (MessageListener) Mockito.anyObject())).thenReturn(
                chat);
        Mockito.when(transferManager.createOutgoingFileTransfer(Mockito.anyString())).thenReturn(transfer);
        Mockito.when(conn.isConnected()).thenReturn(false);
        Mockito.when(transfer.isDone()).thenReturn(true);
    }
}
