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
