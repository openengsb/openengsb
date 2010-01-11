package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextSegmentTransformer;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class RefreshContextAction implements ActionListener {

    private ContextPanel panel;

    public RefreshContextAction(ContextPanel panel) {
        this.panel = panel;

    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        try {
            String result = OpenEngSBClient.contextCall("request", getMessage());

            Segment segment = Segment.fromXML(result);
            Context context = ContextSegmentTransformer.toContext(segment);

            List<ContextEntry> values = flatten(context);

            panel.updateModel(values);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ContextEntry> flatten(Context ctx) {
        ArrayList<ContextEntry> list = new ArrayList<ContextEntry>();
        flatten(ctx, list, "");
        return list;
    }

    private void flatten(Context ctx, List<ContextEntry> list, String prefix) {
        for (String key : ctx.getKeys()) {
            list.add(new ContextEntry(prefix + key, ctx.get(key)));
        }
        for (String child : ctx.getChildrenNames()) {
            flatten(ctx.getChild(child), list, prefix + child + "/");
        }
    }

    private String getMessage() {
        try {
            TextSegment text = new TextSegment.Builder("path").text("/").build();
            String xml = text.toXML();
            return xml;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
