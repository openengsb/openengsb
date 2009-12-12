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

package org.openengsb.core.methodcalltransformation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;

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
            Integer id = systemIdTointernalId.get(System.identityHashCode(obj));
            if (id != null) {
                segments.add(new TextSegment.Builder("reference").text(String.valueOf(id)).build());
            } else {

                id = counter++;
                systemIdTointernalId.put(System.identityHashCode(obj), id);
                segments.add(new TextSegment.Builder("id").text(String.valueOf(id)).build());
                segments.add(buildBean(type, obj));
            }
        }

        return segments;
    }

    private Segment buildBean(Class<?> type, Object o) {
        List<Segment> fields = new ArrayList<Segment>();

        try {

            for (Field field : type.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
                    continue;
                }
                Class<?> fieldType = field.getType();
                String fieldName = field.getName();
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object fieldValue = field.get(o);
                field.setAccessible(accessible);

                List<Segment> segment = valueToSegment(fieldName, fieldType, fieldValue);

                fields.add(new ListSegment.Builder("field").list(segment).build());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new ListSegment.Builder("bean").list(fields).build();
    }
}
