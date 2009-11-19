package org.openengsb.context;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.util.serialization.SerializationException;

public class TestSegmentContextTransformer {

    @Test
    @Ignore
    public void testText() throws SerializationException {
        List<Segment> list = new ArrayList<Segment>();
        Segment text = new TextSegment.Builder("a").text("x").build();
        list.add(text);

        ListSegment listSegment = new ListSegment.Builder("/").list(list).build();
        transformAndVerify(listSegment);
    }

    @Test
    public void testTransformer() throws SerializationException {
        List<Segment> l1 = new ArrayList<Segment>();
        List<Segment> l2 = new ArrayList<Segment>();

        l2.add(new TextSegment.Builder("a").text("x").build());
        l2.add(new TextSegment.Builder("b").text("y").build());
        l2.add(new TextSegment.Builder("c").text("z").build());

        l1.add(new TextSegment.Builder("1foo").text("1").build());
        l1.add(new TextSegment.Builder("2bar").text("2").build());
        l1.add(new TextSegment.Builder("3buz").text("3").build());
        l1.add(new ListSegment.Builder("4list").list(l2).build());

        Segment listSegment = new ListSegment.Builder("/").list(l1).build();

        transformAndVerify(listSegment);
    }

    private void transformAndVerify(Segment segment) throws SerializationException {
        Context context = ContextSegmentTransformer.toContext(segment);
        Segment out = ContextSegmentTransformer.toSegment(context);
        Assert.assertEquals(segment, out);
    }
}
