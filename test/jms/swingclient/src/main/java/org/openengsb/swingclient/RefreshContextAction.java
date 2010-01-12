package org.openengsb.swingclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

            panel.tree.updateTree(context);

        } catch (JMSException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
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
