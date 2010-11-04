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

package org.openengsb.xmpp;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;

public class XmppNotifier implements NotificationDomain {
    private Log log = LogFactory.getLog(getClass());

    private XMPPConnection connection;
    private String password;
    private String resources;

    private FileTransferManager transferManager;

    private String user;

    private void connect() {
        if (this.connection.isConnected()) {
            throw new XMPPNotifierException("Connection already open, disconnect first.");
        }

        try {
            connection.connect();
        } catch (XMPPException e) {
            throw new XMPPNotifierException("Connecting to server failed.", e);
        }
    }

    private Chat createChat(String target) {
        ChatManager chatmanager = this.connection.getChatManager();
        Chat chat = chatmanager.createChat(target, null);
        return chat;
    }

    private void disconnect() {
        if (this.connection.isConnected()) {
            this.connection.disconnect();
        }
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    private void login() {
        if (connection == null) {
            throw new XMPPNotifierException("Cannot login because connection is not open");
        }
        try {
            connection.login(this.user, this.password, this.resources);
        } catch (XMPPException e) {
            throw new XMPPNotifierException("Login to server failed (" +
                    user + "/" + password + "/" + resources + ")", e);
        }
    }

    public void notify(Notification notification) {
        this.connect();

        try {
            this.login();
            this.sendMessage(notification.getRecipient(), notification.getSubject(), notification.getMessage());
            this.sendAttachments(notification.getRecipient(), notification.getAttachments());
        } finally {
            this.disconnect();
        }
    }

    private void sendAttachments(String target, List<Attachment> attach) {
        if (attach.size() < 1)
            return;

        for (Attachment attachment : attach) {
            OutgoingFileTransfer transfer;

            // FIXME needed to mock the transferManager
            // transferManager needs to be renewed at each transfer to enable
            // FileTransfer for the connection
            if (transferManager != null) {
                transfer = transferManager.createOutgoingFileTransfer(target);
            } else {
                FileTransferNegotiator.setServiceEnabled(connection, true);
                ServiceDiscoveryManager.getInstanceFor(connection);
                transfer = new FileTransferManager(connection).createOutgoingFileTransfer(target);
            }
            try {
                transfer.sendStream(new ByteArrayInputStream(attachment.getData()), attachment.getName(), attachment
                        .getData().length, attachment.getType());
            } catch (Exception e1) {
                log.error("Sending File failed. Reason: " + e1.getMessage());
            }
            waitForTransfer(transfer);
            log.debug("Status :: " + transfer.getStatus() + " Error :: " + transfer.getError() + " Exception :: "
                    + transfer.getException());
            log.debug("Is it done? " + transfer.isDone());
        }
    }

    private void waitForTransfer(OutgoingFileTransfer transfer) {
        while (!transfer.isDone()) {
            log.debug("STATE: " + transfer.getStatus() + transfer.getException());
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Waiting for Transfer failed. Reason: " + e.getMessage());
            }
        }
    }

    private void sendMessage(String target, String subject, String msg) {
        if (this.connection == null) {
            throw new XMPPNotifierException("Not connected to XMPP Server.");
        }
        Chat chat = this.createChat(target);

        Message message = this.setupMessage(subject, msg);

        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            throw new XMPPNotifierException("Message transmission failed", e);
        }
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setTransferManager(FileTransferManager transferManager) {
        this.transferManager = transferManager;
    }

    private Message setupMessage(String subject, String body) {
        Message message = new Message();
        message.setSubject(subject);
        message.setBody(body);
        return message;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
