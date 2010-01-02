/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.core.transformation;

import org.openengsb.core.messaging.Segment;
import org.openengsb.core.model.Event;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.util.serialization.SerializationException;

public class Transformer {
    private Transformer() {
        throw new AssertionError();
    }

    public static Segment toSegment(MethodCall methodCall) {
        return new ToSegmentTransformer().transform(methodCall);
    }

    public static Segment toSegment(ReturnValue returnValue) {
        return new ToSegmentTransformer().transform(returnValue);
    }

    public static Segment toSegment(Event event) {
        return new ToSegmentTransformer().transform(event);
    }

    public static String toXml(MethodCall methodCall) throws SerializationException {
        return toSegment(methodCall).toXML();
    }

    public static String toXml(ReturnValue returnValue) throws SerializationException {
        return toSegment(returnValue).toXML();
    }

    public static String toXml(Event event) throws SerializationException {
        return toSegment(event).toXML();
    }

    public static MethodCall toMethodCall(String xml) throws SerializationException {
        return toMethodCall(Segment.fromXML(xml));
    }

    public static ReturnValue toReturnValue(String xml) throws SerializationException {
        return toReturnValue(Segment.fromXML(xml));
    }

    public static Event toEvent(String xml) throws SerializationException {
        return toEvent(Segment.fromXML(xml));
    }

    public static MethodCall toMethodCall(Segment segment) {
        return new FromSegmentTransformer().transform(segment);
    }

    public static ReturnValue toReturnValue(Segment segment) {
        return new FromSegmentTransformer().transformReturnValue(segment);
    }

    public static Event toEvent(Segment segment) {
        return new FromSegmentTransformer().transformEvent(segment);
    }

}
