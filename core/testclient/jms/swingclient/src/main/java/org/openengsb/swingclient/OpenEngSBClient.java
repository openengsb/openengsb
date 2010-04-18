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

package org.openengsb.swingclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class OpenEngSBClient extends JFrame {

    private static JmsService jmsService;

    public OpenEngSBClient(JmsService jmsService, List<ClientEndpoint> endpoints) {
        OpenEngSBClient.jmsService = jmsService;
        setTitle("OpenEngSB devtool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(700, 400));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        final JTabbedPane pane = new JTabbedPane();

        MessagePanel messagePanel = new MessagePanel(endpoints);
        final ContextPanel contextPanel = new ContextPanel();

        pane.addTab("Message", messagePanel);
        pane.addTab("Context", contextPanel);

        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (pane.getSelectedIndex() == 1) {
                    new RefreshContextAction(contextPanel).actionPerformed(null);
                    pane.removeChangeListener(this);
                }
            }
        });

        add(pane);
        setVisible(true);
    }

    public static String serviceCall(ClientEndpoint endpoint, String operation, String message, String context,
            String mep) throws JMSException {
        return jmsService.doServiceCall(endpoint, operation, message, context, mep);
    }

    public static String contextCall(String message) throws JMSException {
        ClientEndpoint endpoint = new ClientEndpoint("org.openengsb.test.contextService", "ctx:contextService");
        return serviceCall(endpoint, "methodcall", message, "", "in-out");
    }

    public void setJmsService(JmsService jmsService) {
        OpenEngSBClient.jmsService = jmsService;
    }
}
