package org.openengsb.swingclient;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.jms.JMSException;

import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextSegmentTransformer;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class ContextFacade {

    public void remove(String name) {
        List<Segment> list = new ArrayList<Segment>();

        list.add(new TextSegment.Builder(name).text("").build());

        ListSegment listSegment = new ListSegment.Builder("/").list(list).build();

        try {
            String xml = listSegment.toXML();
            OpenEngSBClient.contextCall("remove", xml);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(String key, String oldValue, String newValue) {
        if (oldValue != null) {
            String value = getValue(key);
            
            if (!value.equals(oldValue)) {
                throw new ConcurrentModificationException();
            }
        }
        
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder(key).text(newValue).build());
        ListSegment listSegment = new ListSegment.Builder("/").list(list).build();

        try {
            String xml = listSegment.toXML();
            OpenEngSBClient.contextCall("store", xml);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(String pathAndKey) {
        try {
            String path = pathAndKey.substring(0, pathAndKey.lastIndexOf('/'));
            String key = pathAndKey.substring(pathAndKey.lastIndexOf('/') + 1);
            TextSegment text = new TextSegment.Builder("path").text(path).build();
            String xml = text.toXML();
            String response = OpenEngSBClient.contextCall("request", xml);
            Segment segment = Segment.fromXML(response);

            Context context = ContextSegmentTransformer.toContext(segment);
            String value = context.get(key);

            return value;
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}
