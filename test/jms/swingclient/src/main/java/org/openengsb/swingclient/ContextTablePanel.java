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
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

public class ContextTablePanel extends JPanel {

    private Model model = new Model();

    JTable table = new JTable(model);

    ContextPanel contextPanel;

    JButton newEntry = new JButton("New Entry");

    private ContextFacade contextFacade = new ContextFacade();

    public ContextTablePanel(ContextPanel contextPanel) {
        this.contextPanel = contextPanel;

        setLayout(new BorderLayout());

        JPopupMenu popup = new JPopupMenu();

        JMenuItem newItem = new JMenuItem("New Entry");
        JMenuItem deleteItem = new JMenuItem("Delete");

        newItem.addActionListener(new NewContextEntryAction(contextPanel));
        deleteItem.addActionListener(new DeleteContextEntryAction(this));

        popup.add(newItem);
        popup.add(deleteItem);

        table.addKeyListener(new KeyAdapter() {
            private DeleteContextEntryAction deleteAction = new DeleteContextEntryAction(ContextTablePanel.this);

            @Override
            public void keyPressed(KeyEvent e) {
                deleteAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "delete"));
            }
        });
        table.addMouseListener(new PopupListener(popup));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        newEntry.setEnabled(false);
        newEntry.addActionListener(new NewContextEntryAction(contextPanel));
        buttonPanel.add(newEntry);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void updateModel(List<ContextEntry> values) {
        model.setValues(values);
    }

    public List<ContextEntry> getModel() {
        return model.getValues();
    }

    private class PopupListener extends MouseAdapter {
        private JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

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

            String path = contextEntry.getPath() + "/" + contextEntry.getName();
            String oldValue = contextEntry.getValue();

            if (contextEntry.getValue().equals(newValue)) {
                return;
            }

            try {
                contextFacade.setValue(path, oldValue, newValue);
                contextEntry.setValue(newValue);
            } catch (ConcurrentModificationException e) {
                String currentValue = contextFacade.getValue(path);

                String message = "The value of this entry changed:\n\n";
                message += "Old value: " + oldValue + "\n";
                message += "Current value: " + currentValue + "\n";
                message += "New value: " + newValue + "\n";
                message += "\nDo you want to overwrite the current value with your new value?";

                int response = JOptionPane.showConfirmDialog(ContextTablePanel.this, message, "Edit Conflict",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.NO_OPTION) {
                    contextEntry.setValue(currentValue);
                    return;
                }

                try {
                    contextFacade.setValue(path, currentValue, newValue);
                    contextEntry.setValue(newValue);
                } catch (ConcurrentModificationException ee) {
                    JOptionPane.showMessageDialog(ContextTablePanel.this, "The value changed again. I'm giving up.",
                            "Error setting value", JOptionPane.ERROR_MESSAGE);
                    contextEntry.setValue(contextFacade.getValue(path));
                }
            }
        }
    }

}
