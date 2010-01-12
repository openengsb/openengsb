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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ContextPanel extends JPanel {

    private JButton refresh = new JButton("Refresh");
    private JButton exit = new JButton("Exit");

    ContextTreePanel tree = new ContextTreePanel(this);

    ContextTablePanel table = new ContextTablePanel(this);

    public ContextPanel() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refresh);
        buttonPanel.add(exit);

        refresh.addActionListener(new RefreshContextAction(this));
        exit.addActionListener(new ExitAction());

        JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel.setTopComponent(new JScrollPane(tree));
        centerPanel.setBottomComponent(table);

        add(centerPanel);
        add(buttonPanel, BorderLayout.SOUTH);

    }

}
