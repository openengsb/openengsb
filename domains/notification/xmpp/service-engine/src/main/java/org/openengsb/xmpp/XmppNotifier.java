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
package org.openengsb.xmpp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;

public class XmppNotifier implements NotificationDomain {

    private String user;
    private String password;

    private String server;
    private int port;

    private XMPPConnection connection;

    public XmppNotifier(String server, String port, String user, String password) {
        this.user = user;
        this.password = password;

        if(server != null){
            this.server = server;
        }else{
            this.server = "localhost";
        }
        try {
            this.port = Integer.parseInt(port);
        } catch (Exception e) {
            this.port = 5222;
        }
    }

    public void notify(Notification notification) {
        this.connect(this.server, this.port);

        this.login(this.user, this.password, "/openengsb/xmppNotifer");

        this.sendMessage(notification.getRecipient(), notification.getSubject(), notification.getMessage(),
                notification.getAttachments());

        this.disconnect();
    }

    private void connect(String server, int port) {
        if (this.connection.isConnected()) {
            throw new XMPPException("Already an open connection, disconnect first.");
        }

        ConnectionConfiguration config = new ConnectionConfiguration(server, port);
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(true);

        this.connection = new XMPPConnection(config);

        try {
            connection.connect();
        } catch (Exception e) {
            throw new XMPPException("Connect to server failed", e);
        }
    }

    public void disconnect() {
        if (this.connection.isConnected()) {
            this.connection.disconnect();
        }
    }

    private void login(String username, String password, String resources) {
        if (connection == null) {
            throw new XMPPException("Cannot login, if no connection is opened");
        }
        try {
            connection.login(username, password, resources);
        } catch (Exception e) {
            throw new XMPPException("Login to server failed", e);
        }
    }

    public void sendMessage(String target, String subject, String msg, Attachment[] attach) {
        if (this.connection == null) {
            throw new XMPPException("Not connected to XMPP Server.");
        }
        ChatManager chatmanager = this.connection.getChatManager();

        Chat chat = chatmanager.createChat(target, null);

        Message message = new Message();
        message.setSubject(subject);
        message.setBody(msg);
        try {
            chat.sendMessage(message);
        } catch (Exception e) {
            throw new XMPPException("Message transmission failed", e);
        }

        for(Attachment attachment : attach){
            FileTransferManager transferManager = new FileTransferManager(connection);
            OutgoingFileTransfer transfer = transferManager.createOutgoingFileTransfer(target);
            
            try{
                transfer.sendStream(new ByteArrayInputStream(attachment.getData()), attachment.getName(), attachment.getData().length, attachment.getType());
            } catch(Exception e){ 
                throw new XMPPException("Transmission of Attachment", e);
            }
        }
         
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

}
