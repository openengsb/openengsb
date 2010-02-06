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

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.openengsb.swingclient.ContextTreePanel.ContextTreeNode;

public class NewContextEntryAction implements ActionListener {

    private ContextPanel panel;

    private ContextFacade contextFacade = new ContextFacade();

    public NewContextEntryAction(ContextPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ContextTreeNode selectedNode = panel.tree.getSelectedNode();
        if (selectedNode == null) {
            return;
        }

        String path = selectedNode.getPath();

        String key = JOptionPane.showInputDialog(panel, "Enter key for value in subtree '/" + path + "'",
                "Create new entry", JOptionPane.PLAIN_MESSAGE);

        if (key == null || key.equals("")) {
            return;
        }

        String value = JOptionPane.showInputDialog(panel, "Enter value", "Create new entry", JOptionPane.PLAIN_MESSAGE);

        if (value == null || value.equals("")) {
            return;
        }

        // TODO: add check if value key already exists
        contextFacade.setValue(path + key, null, value);
        selectedNode.getValues().add(new ContextEntry(path, key, value));
        ((AbstractTableModel) panel.table.table.getModel()).fireTableDataChanged();
    }

}
