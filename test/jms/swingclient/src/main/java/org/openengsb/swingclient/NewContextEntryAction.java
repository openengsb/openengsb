package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

public class NewContextEntryAction implements ActionListener {

    private ContextPanel panel;

    private ContextFacade contextFacade = new ContextFacade();

    public NewContextEntryAction(ContextPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String key = JOptionPane.showInputDialog(panel, "Enter key", "Create new entry", JOptionPane.PLAIN_MESSAGE);

        if (key == null || key.equals("")) {
            return;
        }

        String value = JOptionPane.showInputDialog(panel, "Enter value", "Create new entry", JOptionPane.PLAIN_MESSAGE);

        if (value == null || value.equals("")) {
            return;
        }

        contextFacade.setValue(key, null, value);
        List<ContextEntry> model = panel.getModel();
        model.add(new ContextEntry("", key, value));
        ((AbstractTableModel) panel.table.getModel()).fireTableDataChanged();
    }

}
