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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.JMSException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class SendAction implements ActionListener {

    private MessagePanel panel;

    public SendAction(MessagePanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ClientEndpoint selectedEndpoint = (ClientEndpoint) panel.endpoint.getSelectedItem();
                String text = panel.textArea.getText();
                String ctx = panel.context.getText();
                String op = (String) panel.operation.getSelectedItem();
                String mep = (String) panel.mep.getSelectedItem();

                try {
                    final String result = OpenEngSBClient.serviceCall(selectedEndpoint, op, text, ctx, mep);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new InfoDialog("Result", result);
                        }
                    });
                } catch (final JMSException e) {
                    JOptionPane.showMessageDialog(panel, e.getMessage(), "JMS Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        t.start();
    }
}
