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
package org.openengsb.xmpp.test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.mockito.Mockito;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;
import org.openengsb.xmpp.XmppNotifier;

public class XmppNotifierTest {

    private XmppNotifier target;
    private XMPPConnection conn;
    private ChatManager chatManager;
    private Chat chat;
    private FileTransferManager transferManager;
    private OutgoingFileTransfer transfer;

    private String username = "test";
    private String password = "test";
    private String resources = "/openengsb/XmppNotifier";
    private Notification notification;

    private Notification createNotification() {
        Notification notification = new Notification();
        notification.setRecipient("testReceipient");
        notification.setMessage("TestMessage");
        notification.setSubject("TestSubject");
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(new Attachment(new byte[3], "someType", "someName"));
        attachments.add(new Attachment(new byte[7], "someOtherType", "someOtherName"));
        notification.setAttachments(attachments);
        return notification;
    }

    @Before
    public void setUp() {
        target = new XmppNotifier();
        target.setUser(username);
        target.setPassword(password);
        target.setResources(resources);
        notification = createNotification();

        conn = Mockito.mock(XMPPConnection.class);
        chatManager = Mockito.mock(ChatManager.class);
        chat = Mockito.mock(Chat.class);
        transferManager = Mockito.mock(FileTransferManager.class);
        transfer = Mockito.mock(OutgoingFileTransfer.class);

        target.setConnection(conn);
        target.setTransferManager(transferManager);

        setupMocking();
    }

    private void setupMocking() {
        Mockito.when(conn.getChatManager()).thenReturn(chatManager);
        Mockito.when(chatManager.createChat(Mockito.anyString(), (MessageListener) Mockito.anyObject())).thenReturn(
                chat);
        Mockito.when(transferManager.createOutgoingFileTransfer(notification.getRecipient())).thenReturn(transfer);
        Mockito.when(conn.isConnected()).thenReturn(false);
        Mockito.when(transfer.isDone()).thenReturn(true);
    }

    @After
    public void tearDown() {
        target = null;
        notification = null;
        conn = null;
        chatManager = null;
        chat = null;
        transferManager = null;
        transfer = null;
    }

    @Test
    public void testNotifyConnect() throws XMPPException {
        target.notify(notification);
        Mockito.verify(conn, Mockito.times(1)).connect();
    }

    @Test
    public void testNotifyLogin() throws XMPPException {
        target.notify(notification);
        Mockito.verify(conn, Mockito.times(1)).login(Mockito.eq(username), Mockito.eq(password), Mockito.eq(resources));
    }

    @Test
    public void testNotifyGetChatManager() throws XMPPException {
        target.notify(notification);
        Mockito.verify(conn, Mockito.times(1)).getChatManager();
    }

    @Test
    public void testNotifyCreateChat() throws XMPPException {
        target.notify(notification);
        Mockito.verify(chatManager, Mockito.times(1)).createChat(Mockito.eq(notification.getRecipient()),
                (MessageListener) Mockito.anyObject());
    }

    @Test
    public void testNotifySendMessage() throws XMPPException {
        target.notify(notification);
        Message message = new Message();
        message.setSubject(notification.getSubject());
        message.setBody(notification.getMessage());
        Mockito.verify(chat, Mockito.times(1)).sendMessage(Mockito.eq(message));
    }

    @Test
    public void testNotifyCreateOutgoingFileTransfer() throws XMPPException {
        target.notify(notification);
        Mockito.verify(transferManager, Mockito.times(notification.getAttachments().size()))
                .createOutgoingFileTransfer(notification.getRecipient());
    }

    @Test
    public void testNotifySendStream() throws XMPPException {
        target.notify(notification);
        Mockito.verify(transfer, Mockito.times(notification.getAttachments().size())).sendStream(
                (ByteArrayInputStream) Mockito.anyObject(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }
}