package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResetAction implements ActionListener {

    private MessagePanel panel;

    public ResetAction(MessagePanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        panel.textArea.setText("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        panel.textArea.setCaretPosition(panel.textArea.getText().length());
        panel.textArea.requestFocus();
    }
}
