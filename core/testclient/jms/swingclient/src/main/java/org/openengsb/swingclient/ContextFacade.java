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

    public void createContext(String key) {
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder(key).text("").build());
        ListSegment listSegment = new ListSegment.Builder("/").list(list).build();

        try {
            String xml = listSegment.toXML();
            OpenEngSBClient.contextCall("addContext", xml);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(String pathAndKey) {
        try {
            if (pathAndKey.lastIndexOf('/') == -1) {
                pathAndKey = "/" + pathAndKey;
            }

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
