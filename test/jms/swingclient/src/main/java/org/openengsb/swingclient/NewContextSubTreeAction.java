package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.openengsb.swingclient.ContextTreePanel.ContextTreeNode;

public class NewContextSubTreeAction implements ActionListener {

    private ContextTreePanel panel;
    private ContextFacade contextFacade = new ContextFacade();
    private RefreshContextAction refresh;

    public NewContextSubTreeAction(ContextTreePanel panel) {
        this.panel = panel;
        refresh = new RefreshContextAction(panel.contextPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TreePath selectionPath = panel.tree.getSelectionPath();
        if (selectionPath == null) {
            return;
        }

        ContextTreeNode selected = (ContextTreeNode) selectionPath.getLastPathComponent();
        String path = selected.getPath();

        String name = JOptionPane.showInputDialog(panel, "Enter name for subtree", "Create new subtree",
                JOptionPane.PLAIN_MESSAGE);

        if (name == null || name.isEmpty()) {
            return;
        }

        contextFacade.createContext(path + "/" + name);
        refresh.actionPerformed(e);
    }
}
