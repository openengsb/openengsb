package org.openengsb.core.methodcalltransformation;

import org.openengsb.core.messaging.Segment;
import org.openengsb.util.serialization.SerializationException;

public class Transformer {
    private Transformer() {
        throw new AssertionError();
    }

    public static Segment toSegment(MethodCall methodCall) {
        return new ToSegmentTransformer().transform(methodCall);
    }

    public static String toXml(MethodCall methodCall) throws SerializationException {
        return toSegment(methodCall).toXML();
    }

    public static Segment toSegment(ReturnValue returnValue) {
        return new ToSegmentTransformer().transform(returnValue);
    }

    public static String toXml(ReturnValue returnValue) throws SerializationException {
        return toSegment(returnValue).toXML();
    }

    public static MethodCall toMethodCall(String xml) throws SerializationException {
        return toMethodCall(Segment.fromXML(xml));
    }

    public static ReturnValue toReturnValue(String xml) throws SerializationException {
        return toReturnValue(Segment.fromXML(xml));
    }

    public static MethodCall toMethodCall(Segment segment) {
        return new FromSegmentTransformer().transform(segment);
    }

    public static ReturnValue toReturnValue(Segment segment) {
        return new FromSegmentTransformer().transformReturnValue(segment);
    }

}
