/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.openengsb.core;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;

public class MethodCallTransformer {

    private MethodCallTransformer() {
        throw new AssertionError();
    }

    public static Segment transform(MethodCall methodCall) {
        List<Segment> list = new ArrayList<Segment>();
        list.add(new TextSegment.Builder("method").text(methodCall.getMethodName()).build());

        for (int i = 0; i < methodCall.getArgs().length; i++) {
            Object o = methodCall.getArgs()[i];
            Class<?> type = methodCall.getTypes()[i];

            List<Segment> argument = new ArrayList<Segment>();
            argument.add(new TextSegment.Builder("type").text(type.getName()).build());
            argument.add(new TextSegment.Builder("value").text(String.valueOf(o)).build());
            list.add(new ListSegment.Builder("argument").list(argument).build());

        }

        ListSegment listSegment = new ListSegment.Builder("methodcall").list(list).build();

        return listSegment;
    }

    public static MethodCall transform(Segment segment) {
        if (!(segment instanceof ListSegment)) {
            throw new IllegalArgumentException("Segment is not a ListSegment");
        }

        ListSegment ls = (ListSegment) segment;

        List<Segment> list = new ArrayList<Segment>(ls.getList());

        Segment method = list.remove(0);
        if (!(method instanceof TextSegment)) {
            throw new IllegalArgumentException("Segment is not a TextSegment");
        }

        TextSegment textMethod = (TextSegment) method;
        if (!textMethod.getName().equals("method")) {
            throw new IllegalArgumentException("Error parsing method call");
        }

        String methodName = textMethod.getText();

        Object[] args = new Object[list.size()];
        Class<?>[] types = new Class<?>[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Segment argumentList = list.get(i);
            if (!argumentList.getName().equals("argument")) {
                throw new IllegalArgumentException("Error parsing argument");
            }

            List<Segment> l = ((ListSegment) argumentList).getList();

            String type = ((TextSegment) l.get(0)).getText();
            String value = ((TextSegment) l.get(1)).getText();

            Object obj = create(type, value);

            args[i] = obj;
            types[i] = getClassForType(type);
        }

        return new MethodCall(methodName, args, types);
    }

    private static Class<?> getClassForType(String type) {
        if (type.equals("byte")) {
            return byte.class;
        }

        if (type.equals("short")) {
            return short.class;
        }

        if (type.equals("int")) {
            return int.class;
        }

        if (type.equals("long")) {
            return long.class;
        }

        if (type.equals("char")) {
            return char.class;
        }

        if (type.equals("float")) {
            return float.class;
        }

        if (type.equals("double")) {
            return double.class;
        }

        if (type.equals("boolean")) {
            return boolean.class;
        }

        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object create(String type, String value) {
        if (type.equals("java.lang.String")) {
            return value;
        }

        if (type.equals("byte")) {
            return Byte.valueOf(value);
        }

        if (type.equals("short")) {
            return Short.valueOf(value);
        }

        if (type.equals("int")) {
            return Integer.valueOf(value);
        }

        if (type.equals("long")) {
            return Long.valueOf(value);
        }

        if (type.equals("char")) {
            if (value.length() > 1) {
                throw new IllegalArgumentException("Can't parse char");
            }

            return value.charAt(0);
        }

        if (type.equals("float")) {
            return Float.valueOf(value);
        }

        if (type.equals("double")) {
            return Double.valueOf(value);
        }

        if (type.equals("boolean")) {
            return Boolean.valueOf(value);
        }

        throw new IllegalStateException(String.format("Type '%s' is not supported", type));
    }
}
