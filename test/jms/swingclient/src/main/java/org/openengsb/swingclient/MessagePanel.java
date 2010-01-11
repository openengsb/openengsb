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

    JComboBox mep = new JComboBox(new String[] { "in-out", "robust-in-only" });

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
