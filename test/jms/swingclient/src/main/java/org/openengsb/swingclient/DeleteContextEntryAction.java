package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class DeleteContextEntryAction implements ActionListener {

    private ContextPanel panel;
    
    private ContextFacade contextFacade = new ContextFacade();

    public DeleteContextEntryAction(ContextPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        List<ContextEntry> model = panel.getModel();
        int row = panel.table.getSelectedRow();

        ContextEntry removedEntry = model.remove(row);
        ((AbstractTableModel) panel.table.getModel()).fireTableDataChanged();
        
        contextFacade.remove(removedEntry.getName());
    }
}
