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

package org.openengsb.core.transformation;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;
import org.openengsb.core.model.Event;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;

class ToSegmentTransformer {

    private int counter = 0;

    private Map<Integer, Integer> systemIdTointernalId = new HashMap<Integer, Integer>();

    Segment transform(MethodCall methodCall) {
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder("method").text(methodCall.getMethodName()).build());

        for (int i = 0; i < methodCall.getArgs().length; i++) {
            Object obj = methodCall.getArgs()[i];
            Class<?> type = methodCall.getTypes()[i];

            list.add(new ListSegment.Builder("argument").list(valueToSegment("arg" + i, type, obj)).build());
        }

        ListSegment listSegment = new ListSegment.Builder("methodcall").list(list).build();

        return listSegment;
    }

    Segment transform(ReturnValue returnValue) {
        List<Segment> list = valueToSegment("value", returnValue.getType(), returnValue.getValue());
        return new ListSegment.Builder("returnValue").list(list).build();
    }

    Segment transform(Event event) {
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder("event").text(event.getClass().getName()).build());
        list.add(new TextSegment.Builder("name").text(event.getName()).build());
        list.add(new TextSegment.Builder("domain").text(event.getDomain()).build());

        if (event.getToolConnector() != null) {
            list.add(new TextSegment.Builder("toolConnector").text(event.getToolConnector()).build());
        }

        for (String key : event.getKeys()) {
            Object obj = event.getValue(key);
            Class<?> type = obj.getClass();
            list.add(new ListSegment.Builder("element").list(valueToSegment(key, type, obj)).build());
        }

        ListSegment listSegment = new ListSegment.Builder("event").list(list).build();

        return listSegment;
    }

    private List<Segment> valueToSegment(String name, Class<?> type, Object obj) {
        List<Segment> segments = new ArrayList<Segment>();
        segments.add(new TextSegment.Builder("name").text(name).build());
        segments.add(new TextSegment.Builder("type").text(type.getName()).build());

        if (obj == null) {
            segments.add(new TextSegment.Builder("null").text("").build());
            return segments;
        }

        if (PrimitiveTypes.contains(type)) {
            segments.add(new TextSegment.Builder("value").text(String.valueOf(obj)).build());
        } else {
            handleComplexType(type, obj, segments);
        }

        return segments;
    }

    private void handleComplexType(Class<?> type, Object obj, List<Segment> segments) {
        Integer id = systemIdTointernalId.get(System.identityHashCode(obj));
        if (id != null) {
            segments.add(new TextSegment.Builder("reference").text(String.valueOf(id)).build());
        } else {
            id = counter++;
            systemIdTointernalId.put(System.identityHashCode(obj), id);
            segments.add(new TextSegment.Builder("id").text(String.valueOf(id)).build());
            if (type.isArray()) {
                segments.add(buildArray(type, obj));
            } else {
                segments.add(buildBean(type, obj));
            }
        }
    }

    private Segment buildArray(Class<?> type, Object obj) {
        List<Segment> elements = new ArrayList<Segment>();
        Class<?> elementType = type.getComponentType();

        for (int i = 0; i < Array.getLength(obj); i++) {
            Object elementValue = Array.get(obj, i);
            List<Segment> segment = valueToSegment("arrayElement" + i, elementType, elementValue);
            elements.add(new ListSegment.Builder("arrayElement").list(segment).build());
        }

        return new ListSegment.Builder("array").list(elements).build();
    }

    private Segment buildBean(Class<?> type, Object o) {
        List<Segment> fields = new ArrayList<Segment>();

        for (Field field : type.getDeclaredFields()) {
            if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
                continue;
            }
            Class<?> fieldType = field.getType();
            String fieldName = field.getName();
            Object fieldValue = FieldAccessorUtil.getValue(field, o);

            List<Segment> segment = valueToSegment(fieldName, fieldType, fieldValue);

            fields.add(new ListSegment.Builder("field").list(segment).build());
        }

        return new ListSegment.Builder("bean").list(fields).build();
    }

}
