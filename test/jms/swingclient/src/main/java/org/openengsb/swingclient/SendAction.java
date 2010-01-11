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
                            JOptionPane.showMessageDialog(panel, result);
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
