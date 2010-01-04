package org.openengsb.swingclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class OpenEngSBClient extends JFrame {

    private JTextArea textArea = new JTextArea();

    private JButton sendButton = new JButton("Send");

    private JButton resetButton = new JButton("Reset");

    private JButton exitButton = new JButton("Exit");

    private JTextField context = new JTextField(8);

    private JComboBox endpoint;

    private JComboBox operation = new JComboBox(new String[] { "none", "event", "methodcall" });

    private JmsService jmsService;

    public OpenEngSBClient(JmsService jmsService, List<String> services) {
        this.jmsService = jmsService;
        this.endpoint = new JComboBox(services.toArray());
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
        buttonPanel.add(resetButton);
        buttonPanel.add(exitButton);

        sendButton.addActionListener(new SendAction());
        resetButton.addActionListener(new ResetAction());
        exitButton.addActionListener(new ExitAction());

        add(configPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
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
                    final String result = (String) jmsService.doServiceCall(textArea.getText(), context.getText());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(OpenEngSBClient.this, result);
                        }
                    });
                }
            });
            t.start();
        }
    }

    private class ResetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            textArea.setText("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        }
    }

    private class ExitAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

}
