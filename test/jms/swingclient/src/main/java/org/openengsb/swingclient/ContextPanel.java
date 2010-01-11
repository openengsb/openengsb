package org.openengsb.swingclient;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class ContextPanel extends JPanel {

    private JButton refresh = new JButton("Refresh");
    private JButton exit = new JButton("Exit");
    private Model model = new Model();

    JTable table = new JTable(model);
    private JPopupMenu popup;

    private ContextFacade contextFacade = new ContextFacade();

    public ContextPanel() {
        setLayout(new BorderLayout());
        popup = new JPopupMenu();

        JMenuItem newItem = new JMenuItem("New...");
        JMenuItem deleteItem = new JMenuItem("Delete");

        newItem.addActionListener(new NewContextEntryAction(this));
        deleteItem.addActionListener(new DeleteContextEntryAction(this));

        popup.add(newItem);
        popup.add(deleteItem);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refresh);
        buttonPanel.add(exit);

        refresh.addActionListener(new RefreshContextAction(this));
        exit.addActionListener(new ExitAction());

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        table.addMouseListener(new PopupListener());
    }

    private class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
                int row = table.rowAtPoint(e.getPoint());
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }
    }

    public void updateModel(List<ContextEntry> values) {
        model.setValues(values);
    }

    public List<ContextEntry> getModel() {
        return model.getValues();
    }

    private class Model extends AbstractTableModel {

        private List<ContextEntry> values = new ArrayList<ContextEntry>();

        public List<ContextEntry> getValues() {
            return values;
        }

        public void setValues(List<ContextEntry> values) {
            this.values = values;
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return values.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ContextEntry contextEntry = values.get(rowIndex);

            switch (columnIndex) {
            case 0:
                return contextEntry.getName();
            case 1:
                return contextEntry.getValue();
            default:
                throw new AssertionError();
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return "Name";
            case 1:
                return "Value";
            default:
                throw new AssertionError();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String newValue = (String) aValue;
            ContextEntry contextEntry = values.get(rowIndex);

            String name = contextEntry.getName();
            String oldValue = contextEntry.getValue();

            if (contextEntry.getValue().equals(newValue)) {
                return;
            }

            try {
                contextFacade.setValue(name, oldValue, newValue);
                contextEntry.setValue(newValue);
            } catch (ConcurrentModificationException e) {
                String currentValue = contextFacade.getValue(name);

                String message = "The value of this entry changed:\n\n";
                message += "Old value: " + oldValue + "\n";
                message += "Current value: " + currentValue + "\n";
                message += "New value: " + newValue + "\n";
                message += "\nDo you want to overwrite the current value with your new value?";

                int response = JOptionPane.showConfirmDialog(ContextPanel.this, message, "fof",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.NO_OPTION) {
                    contextEntry.setValue(currentValue);
                    return;
                }

                try {
                    contextFacade.setValue(name, currentValue, newValue);
                    contextEntry.setValue(newValue);
                } catch (ConcurrentModificationException ee) {
                    JOptionPane.showMessageDialog(ContextPanel.this, "The value changed again. I'm giving up.",
                            "Error setting value", JOptionPane.ERROR_MESSAGE);
                    contextEntry.setValue(contextFacade.getValue(name));
                }
            }
        }
    }
}
