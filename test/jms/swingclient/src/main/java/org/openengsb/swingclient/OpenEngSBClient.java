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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.springframework.jms.JmsException;

public class OpenEngSBClient extends JFrame {

    private JTextArea textArea = new JTextArea();

    private JButton loadButton = new JButton("Load...");

    private JButton sendButton = new JButton("Send");

    private JButton resetButton = new JButton("Reset");

    private JButton exitButton = new JButton("Exit");

    private JTextField context = new JTextField(8);

    private JComboBox endpoint;

    private JComboBox operation = new JComboBox(new String[] { "none", "event", "methodcall" });

    private JmsService jmsService;

    public OpenEngSBClient(JmsService jmsService, List<ClientEndpoint> endpoints) {
        this.jmsService = jmsService;
        this.endpoint = new JComboBox(endpoints.toArray());
        setTitle("OpenEngSB Testclient");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(650, 400));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        configPanel.add(new JLabel("Context:"));
        configPanel.add(context);
        configPanel.add(new JLabel("Endpoint:"));
        configPanel.add(endpoint);
        configPanel.add(new JLabel("Operation:"));
        configPanel.add(operation);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(exitButton);

        getRootPane().setDefaultButton(sendButton);

        loadButton.setMnemonic('l');
        sendButton.setMnemonic('s');
        resetButton.setMnemonic('r');
        exitButton.setMnemonic('x');

        sendButton.setPreferredSize(loadButton.getPreferredSize());
        resetButton.setPreferredSize(loadButton.getPreferredSize());
        exitButton.setPreferredSize(loadButton.getPreferredSize());

        sendButton.addActionListener(new SendAction());
        resetButton.addActionListener(new ResetAction());
        exitButton.addActionListener(new ExitAction());
        loadButton.addActionListener(new LoadAction());
        
        operation.setEditable(true);

        add(configPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);

        new ResetAction().actionPerformed(null);
    }

    public JmsService getJmsService() {
        return jmsService;
    }

    public void setJmsService(JmsService jmsService) {
        this.jmsService = jmsService;
    }

    private class SendAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    ClientEndpoint selectedEndpoint = (ClientEndpoint) endpoint.getSelectedItem();
                    String text = textArea.getText();
                    String ctx = context.getText();
                    String op = (String) operation.getSelectedItem();

                    try {
                        final String result = (String) jmsService.doServiceCall(selectedEndpoint, op, text, ctx);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(OpenEngSBClient.this, result);
                            }
                        });
                    } catch (JMSException e) {
                        JOptionPane.showMessageDialog(OpenEngSBClient.this, e.getMessage(), "JMS Error",
                                JOptionPane.ERROR_MESSAGE);
                    } catch (JmsException e) {
                        JOptionPane.showMessageDialog(OpenEngSBClient.this, e.getCause().getMessage(), "JMS Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
            t.start();
        }
    }

    private class ResetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setText("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            textArea.setCaretPosition(textArea.getText().length());
            textArea.requestFocus();
        }
    }

    private class LoadAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(OpenEngSBClient.this);
            File file = fileChooser.getSelectedFile();
            if (file == null) {
                return;
            }

            try {
                textArea.setText(IOUtils.toString(new FileReader(file)));
                textArea.setCaretPosition(0);
                textArea.requestFocus();
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(OpenEngSBClient.this, "File could not be found", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(OpenEngSBClient.this, "Error reading file", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ExitAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

}
