package org.openengsb.swingclient;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setValue(String key, String value) {
        List<Segment> list = new ArrayList<Segment>();

        list.add(new TextSegment.Builder(key).text(value).build());

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
