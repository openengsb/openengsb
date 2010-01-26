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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

class FromSegmentTransformer {

    private Map<Integer, Object> idToObject = new HashMap<Integer, Object>();

    private Map<Integer, Integer> placeholderInstances = new HashMap<Integer, Integer>();

    MethodCall transform(Segment segment) {
        ListSegment ls = (ListSegment) segment;
        List<Segment> list = new ArrayList<Segment>(ls.getList());

        Segment method = list.remove(0);
        String methodName = extractMethodName(method);

        Object[] args = new Object[list.size()];
        Class<?>[] types = new Class<?>[list.size()];
        transformArguments(list, args, types);

        replacePlaceholders(args, types);

        return new MethodCall(methodName, args, types);
    }

    ReturnValue transformReturnValue(Segment segment) {
        List<Segment> l = ((ListSegment) segment).getList();

        String typeName = ((TextSegment) l.get(1)).getText();
        Object value = segmentToValue(typeName, l.subList(2, l.size()));
        Class<?> type = getClassForType(typeName);

        value = replace(value, type);

        return new ReturnValue(value, type);
    }

    Event transformEvent(Segment segment) {
        ListSegment ls = (ListSegment) segment;
        List<Segment> list = new ArrayList<Segment>(ls.getList());

        String eventType = ((TextSegment) list.remove(0)).getText();
        ListSegment superClasses = (ListSegment) list.remove(0);
        String eventName = ((TextSegment) list.remove(0)).getText();
        String domain = ((TextSegment) list.remove(0)).getText();
        String toolConnector = null;
        if (!list.isEmpty() && list.get(0).getName().equals("toolConnector")) {
            toolConnector = ((TextSegment) list.remove(0)).getText();
        }

        Class<?> eventClass = getEventClass(eventType, superClasses.getList());
        Event event = createEvent(eventClass, eventName, domain, toolConnector);
        transformEventElements(list, event);

        for (String key : event.getKeys()) {
            Object oldValue = event.getValue(key);
            event.setValue(key, replace(oldValue, oldValue.getClass()));
        }

        return event;
    }

    private void transformEventElements(List<Segment> list, Event event) {
        for (Segment elementList : list) {
            if (!elementList.getName().equals("element")) {
                throw new IllegalArgumentException("Error parsing element of event");
            }

            List<Segment> l = ((ListSegment) elementList).getList();

            String key = ((TextSegment) l.get(0)).getText();
            String type = ((TextSegment) l.get(1)).getText();

            event.setValue(key, segmentToValue(type, l.subList(2, l.size())));
        }
    }

    private Event createEvent(Class<?> eventClass, String name, String domain, String toolConnector) {
        try {

            Constructor<?> noArgConstructor = eventClass.getDeclaredConstructor();
            boolean accessible = noArgConstructor.isAccessible();
            noArgConstructor.setAccessible(true);
            Event e = (Event) noArgConstructor.newInstance();
            noArgConstructor.setAccessible(accessible);

            e.setDomain(domain);
            e.setName(name);
            e.setToolConnector(toolConnector);
            return e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Class<?> getEventClass(String type, List<Segment> superclasses) {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            if (superclasses.isEmpty()) {
                throw new RuntimeException(e);
            }
            String superType = ((TextSegment) superclasses.get(0)).getText();
            return getEventClass(superType, superclasses.subList(1, superclasses.size()));
        }
    }

    private String extractMethodName(Segment method) {
        TextSegment textMethod = (TextSegment) method;
        if (!textMethod.getName().equals("method")) {
            throw new IllegalArgumentException("Error parsing method call");
        }

        return textMethod.getText();
    }

    private void transformArguments(List<Segment> segmentList, Object[] args, Class<?>[] types) {
        for (int i = 0; i < segmentList.size(); i++) {
            Segment argumentList = segmentList.get(i);
            if (!argumentList.getName().equals("argument")) {
                throw new IllegalArgumentException("Error parsing argument");
            }

            List<Segment> l = ((ListSegment) argumentList).getList();

            String type = ((TextSegment) l.get(1)).getText();

            args[i] = segmentToValue(type, l.subList(2, l.size()));
            types[i] = getClassForType(type);
        }
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
            return doRecursiveReplace(obj, type);
        }

        Object realInstance = idToObject.get(id);
        if (realInstance == null) {
            throw new IllegalStateException("No object with id " + id);
        }

        return realInstance;
    }

    private Object doRecursiveReplace(Object obj, Class<?> type) {
        if (type.isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                Object currentValue = Array.get(obj, i);
                Object newValue = replace(currentValue, type.getComponentType());
                Array.set(obj, i, newValue);
            }
        } else {
            for (Field field : type.getDeclaredFields()) {
                Object currentValue = FieldAccessorUtil.getValue(field, obj);
                Object newValue = replace(currentValue, field.getType());
                FieldAccessorUtil.setValue(field, obj, newValue);
            }
        }
        return obj;
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
        String firstSegmentName = firstSegment.getName();
        if (firstSegmentName.equals("value")) {
            String value = ((TextSegment) firstSegment).getText();
            obj = PrimitiveTypes.create(type, value);
        } else if (firstSegmentName.equals("null")) {
            obj = null;
        } else if (firstSegmentName.equals("reference")) {
            int id = Integer.valueOf(((TextSegment) firstSegment).getText());
            obj = createPlaceholderInstance(type, id);
        } else {
            obj = transformComplexType(type, segments, firstSegment);
        }
        return obj;
    }

    private Object transformComplexType(String type, List<Segment> segments, Segment firstSegment) {
        Object obj;
        int id = Integer.valueOf(((TextSegment) firstSegment).getText());
        ListSegment secondSegment = (ListSegment) segments.get(1);
        if (secondSegment.getName().equals("array")) {
            obj = segmentToArray(type, (ListSegment) segments.get(1));
        } else if (secondSegment.getName().equals("event")) {
            obj = transformEvent((ListSegment) segments.get(1));
        } else {
            obj = segmentToBean(type, (ListSegment) segments.get(1));
        }
        idToObject.put(id, obj);
        return obj;
    }

    private Object createPlaceholderInstance(String type, Integer id) {
        try {

            Class<?> clazz = Class.forName(type);
            Object obj;
            if (clazz.isArray()) {
                Object referencedArray = idToObject.get(id);
                obj = Array.newInstance(clazz.getComponentType(), Array.getLength(referencedArray));
            } else {
                obj = clazz.newInstance();
            }
            placeholderInstances.put(System.identityHashCode(obj), id);
            return obj;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private Object segmentToArray(String type, ListSegment arrayElements) {
        List<Segment> arrayElementList = arrayElements.getList();

        Class<?> arrayClass = getClassForType(type);
        Object array = Array.newInstance(arrayClass.getComponentType(), arrayElementList.size());

        for (int i = 0; i < arrayElementList.size(); i++) {
            ListSegment ls = (ListSegment) arrayElementList.get(i);
            List<Segment> list = ls.getList();

            String componentType = ((TextSegment) list.get(1)).getText();

            Array.set(array, i, segmentToValue(componentType, list.subList(2, list.size())));
        }
        return array;
    }

    private Object segmentToBean(String beanType, ListSegment beanFields) {
        try {
            Class<?> beanClass = getClassForType(beanType);

            Constructor<?> noArgConstructor = beanClass.getDeclaredConstructor();
            boolean accessible = noArgConstructor.isAccessible();
            noArgConstructor.setAccessible(true);
            Object bean = noArgConstructor.newInstance();
            noArgConstructor.setAccessible(accessible);

            for (Segment segment : beanFields.getList()) {
                ListSegment ls = (ListSegment) segment;
                List<Segment> list = ls.getList();

                String fieldName = ((TextSegment) list.get(0)).getText();
                String fieldType = ((TextSegment) list.get(1)).getText();

                Field field = beanClass.getDeclaredField(fieldName);

                FieldAccessorUtil.setValue(field, bean, segmentToValue(fieldType, list.subList(2, list.size())));
            }
            return bean;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }

            throw new RuntimeException(e);
        }
    }

}
