package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.table.AbstractTableModel;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class DeleteContextEntryAction implements ActionListener {

    private ContextPanel panel;

    public DeleteContextEntryAction(ContextPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<ContextEntry> model = panel.getModel();
        int row = panel.table.getSelectedRow();

        ContextEntry removedEntry = model.remove(row);
        ((AbstractTableModel) panel.table.getModel()).fireTableDataChanged();
        
        List<Segment> list = new ArrayList<Segment>();

        String name = removedEntry.getName();
        list.add(new TextSegment.Builder(name).text("").build());

        ListSegment listSegment = new ListSegment.Builder("/").list(list).build();

        try {
            String xml = listSegment.toXML();
            OpenEngSBClient.contextCall("remove", xml);
        } catch (SerializationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (JMSException jmse) {
            // TODO Auto-generated catch block
            jmse.printStackTrace();
        }
        
    }
}
