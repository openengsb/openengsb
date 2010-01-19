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
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class InfoDialog extends JDialog {

    public InfoDialog(String title, Object message) {
        super((Frame) null, true);
        super.setTitle(title);

        initBody(message);

        setSize(400, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initBody(Object message) {
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(String.valueOf(message));
        textArea.setEditable(false);

        add(new JScrollPane(textArea), BorderLayout.CENTER);

        add(createButtons(), BorderLayout.SOUTH);
    }

    private Component createButtons() {
        JPanel buttonPanel = new JPanel();

        JButton ok = new JButton("ok");
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                InfoDialog.this.setVisible(false);
                InfoDialog.this.dispose();
            }
        });
        buttonPanel.add(ok);

        return buttonPanel;
    }
}
