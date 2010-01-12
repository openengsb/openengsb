package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.openengsb.swingclient.ContextTreePanel.ContextTreeNode;

public class DeleteContextSubTreeAction implements ActionListener {

    private ContextTreePanel panel;
    private ContextFacade contextFacade = new ContextFacade();
    private RefreshContextAction refresh;

    public DeleteContextSubTreeAction(ContextTreePanel panel) {
        this.panel = panel;
        refresh = new RefreshContextAction(panel.contextPanel);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        TreePath selectionPath = panel.tree.getSelectionPath();
        if (selectionPath == null) {
            return;
        }

        ContextTreeNode selected = (ContextTreeNode) selectionPath.getLastPathComponent();
        String path = selected.getPath();

        if (selected.getParent() == null) {
            JOptionPane.showMessageDialog(panel, "Cannot delete root node", "Operation not allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(panel, "Do you really want to delete the whole subtree?",
                "Delete Subtree", JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        ContextTreeNode parent = (ContextTreeNode) selected.getParent();
        parent.removeChild(selected);

        contextFacade.remove(path);
        refresh.actionPerformed(evt);
    }
}
