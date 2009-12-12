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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.messaging.ListSegment;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.messaging.TextSegment;

class FromSegmentTransformer {

    private Map<Integer, Object> idToObject = new HashMap<Integer, Object>();

    private Map<Integer, Integer> placeholderInstances = new HashMap<Integer, Integer>();

    MethodCall transform(Segment segment) {
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

            String type = ((TextSegment) l.get(1)).getText();

            args[i] = segmentToValue(type, l.subList(2, l.size()));
            types[i] = getClassForType(type);
        }

        replacePlaceholders(args, types);

        return new MethodCall(methodName, args, types);
    }

    private void replacePlaceholders(Object[] args, Class<?>[] types) {
        for (int i = 0; i < args.length; i++) {
            args[i] = replace(args[i], types[i]);
        }
    }

    private Object replace(Object obj, Class<?> type) {
        if (obj == null) {
            return null;
        }

        if (PrimitiveTypes.contains(type)) {
            return obj;
        }

        Integer id = placeholderInstances.get(System.identityHashCode(obj));
        if (id == null) {
            for (Field field : type.getDeclaredFields()) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    Object currentValue = field.get(obj);
                    Object newValue = replace(currentValue, field.getType());
                    field.set(obj, newValue);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                field.setAccessible(accessible);
            }

            return obj;
        }

        Object realInstance = idToObject.get(id);
        if (realInstance == null) {
            throw new IllegalStateException("No object with id " + id);
        }

        return realInstance;
    }

    private Class<?> getClassForType(String type) {
        Class<?> clazz = PrimitiveTypes.get(type);

        if (clazz != null) {
            return clazz;
        }

        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Object segmentToValue(String type, List<Segment> segments) {
        Object obj;
        Segment firstSegment = segments.get(0);
        if (firstSegment.getName().equals("value")) {
            String value = ((TextSegment) firstSegment).getText();
            obj = create(type, value);
        } else if (firstSegment.getName().equals("null")) {
            obj = null;
        } else if (firstSegment.getName().equals("reference")) {
            int id = Integer.valueOf(((TextSegment) firstSegment).getText());

            obj = createPlaceholderInstance(type, id);

        } else {
            int id = Integer.valueOf(((TextSegment) firstSegment).getText());
            obj = segmentToBean(type, (ListSegment) segments.get(1));
            idToObject.put(id, obj);
        }
        return obj;
    }

    private Object createPlaceholderInstance(String type, Integer id) {
        try {
            Object obj = Class.forName(type).newInstance();
            placeholderInstances.put(System.identityHashCode(obj), id);
            return obj;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private Object segmentToBean(String beanType, ListSegment beanFields) {
        try {
            Class<?> beanClass = Class.forName(beanType);
            Object bean = beanClass.newInstance();

            for (Segment segment : beanFields.getList()) {
                ListSegment ls = (ListSegment) segment;
                List<Segment> list = ls.getList();

                String fieldName = ((TextSegment) list.get(0)).getText();
                String fieldType = ((TextSegment) list.get(1)).getText();

                Field field = beanClass.getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();

                field.setAccessible(true);

                field.set(bean, segmentToValue(fieldType, list.subList(2, list.size())));

                field.setAccessible(accessible);
            }
            return bean;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(e);
        }
    }

    private Object create(String type, String value) {
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
