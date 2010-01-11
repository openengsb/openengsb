package org.openengsb.swingclient;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class ContextPanel extends JPanel {

    private JButton refresh = new JButton("Refresh");
    private JButton exit = new JButton("Exit");
    private Model model = new Model();

    JTable table = new JTable(model);
    private JPopupMenu popup;

    public ContextPanel() {
        setLayout(new BorderLayout());
        popup = new JPopupMenu();

        JMenuItem newItem = new JMenuItem("New...");
        JMenuItem deleteItem = new JMenuItem("Delete");

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
            String s = (String) aValue;
            ContextEntry contextEntry = values.get(rowIndex);

            if (contextEntry.getValue().equals(s)) {
                return;
            }

            contextEntry.setValue(s);

            List<Segment> list = new ArrayList<Segment>();

            String name = contextEntry.getName();
            String value = contextEntry.getValue();
            list.add(new TextSegment.Builder(name).text(value).build());

            ListSegment listSegment = new ListSegment.Builder("/").list(list).build();

            try {
                String xml = listSegment.toXML();
                OpenEngSBClient.contextCall("store", xml);
            } catch (SerializationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
