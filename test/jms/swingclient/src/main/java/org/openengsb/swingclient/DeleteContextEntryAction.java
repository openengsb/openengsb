package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class DeleteContextEntryAction implements ActionListener {

    private ContextTablePanel panel;
    private ContextFacade contextFacade = new ContextFacade();

    public DeleteContextEntryAction(ContextTablePanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        List<ContextEntry> model = panel.getModel();
        int row = panel.table.getSelectedRow();

        int result = JOptionPane.showConfirmDialog(panel, "Do you really want to delete this entry?", "Delete Entry",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        ContextEntry removedEntry = model.remove(row);

        ((AbstractTableModel) panel.table.getModel()).fireTableDataChanged();
        contextFacade.remove(removedEntry.getPath() + removedEntry.getName());
    }
}
