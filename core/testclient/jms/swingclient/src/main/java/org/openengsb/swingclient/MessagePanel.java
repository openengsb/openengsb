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
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MessagePanel extends JPanel {

    private JButton loadButton = new JButton("Load...");

    private JButton sendButton = new JButton("Send");

    private JButton resetButton = new JButton("Reset");

    private JButton exitButton = new JButton("Exit");

    JTextArea textArea = new JTextArea();

    JComboBox operation = new JComboBox(new String[] { "none", "event", "methodcall" });

    JComboBox mep = new JComboBox(new String[] { "in-out", "in-only" });

    JTextField context = new JTextField(8);

    JComboBox endpoint;

    public MessagePanel(List<ClientEndpoint> endpoints) {
        this.endpoint = new JComboBox(endpoints.toArray(new ClientEndpoint[] {}));
        setLayout(new BorderLayout());

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        configPanel.add(new JLabel("Context:"));
        configPanel.add(context);
        configPanel.add(new JLabel("Endpoint:"));
        configPanel.add(endpoint);
        configPanel.add(new JLabel("Operation:"));
        configPanel.add(operation);
        configPanel.add(new JLabel("MEP:"));
        configPanel.add(mep);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(exitButton);

        // panel.setDefaultButton(sendButton);

        loadButton.setMnemonic('l');
        sendButton.setMnemonic('s');
        resetButton.setMnemonic('r');
        exitButton.setMnemonic('x');

        sendButton.setPreferredSize(loadButton.getPreferredSize());
        resetButton.setPreferredSize(loadButton.getPreferredSize());
        exitButton.setPreferredSize(loadButton.getPreferredSize());

        sendButton.addActionListener(new SendAction(this));
        resetButton.addActionListener(new ResetAction(this));
        exitButton.addActionListener(new ExitAction());
        loadButton.addActionListener(new LoadAction(this));

        operation.setEditable(true);

        add(configPanel, BorderLayout.NORTH);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
