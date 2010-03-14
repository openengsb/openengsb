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

package org.openengsb.core.messaging;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;

public class SegmentsTest {

    private JibxXmlSerializer serializer = new JibxXmlSerializer();

    @Test
    public void testTextSegment() throws SerializationException {
        TextSegment ts = new TextSegment.Builder().name("name").format("format").domainConcept("domainConcept").text(
                "text").build();

        StringWriter writer = new StringWriter();
        serializer.serialize(ts, writer);

        TextSegment out = serializer.deserialize(TextSegment.class, new StringReader(writer.toString()));

        Assert.assertEquals(ts, out);
    }

    @Test
    public void testListSegment() throws SerializationException {
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder().name("foo").text("x").build());
        list.add(new TextSegment.Builder().name("bar").text("y").build());
        ListSegment ls = new ListSegment.Builder().name("list").list(list).build();

        StringWriter writer = new StringWriter();
        serializer.serialize(ls, writer);

        ListSegment out = serializer.deserialize(ListSegment.class, new StringReader(writer.toString()));
        Assert.assertEquals(ls, out);
    }

    @Test
    public void testListInListSegment() throws SerializationException {
        List<Segment> list1 = new ArrayList<Segment>();
        list1.add(new TextSegment.Builder().name("bar").text("y").build());
        list1.add(new TextSegment.Builder().name("foo").text("x").build());
        ListSegment ls1 = new ListSegment.Builder().name("list1").list(list1).build();

        List<Segment> list2 = new ArrayList<Segment>();
        list2.add(new TextSegment.Builder().name("foo").text("x").build());
        list2.add(ls1);

        ListSegment ls2 = new ListSegment.Builder().name("list2").list(list2).build();

        StringWriter writer = new StringWriter();
        serializer.serialize(ls2, writer);

        ListSegment out = serializer.deserialize(ListSegment.class, new StringReader(writer.toString()));

        Assert.assertEquals(ls2, out);
    }
}
