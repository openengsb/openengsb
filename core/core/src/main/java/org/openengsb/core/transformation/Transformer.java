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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openengsb.core.model.Event;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.xmlmapping.XMLEvent;
import org.openengsb.core.xmlmapping.XMLMapEntry;
import org.openengsb.core.xmlmapping.XMLMapable;
import org.openengsb.core.xmlmapping.XMLMethodCall;
import org.openengsb.core.xmlmapping.XMLPrimitive;
import org.openengsb.core.xmlmapping.XMLReturnValue;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;

public class Transformer {

    private final static JibxXmlSerializer serializer = new JibxXmlSerializer();

    private Transformer() {
        throw new AssertionError();
    }

    public static String toXml(MethodCall methodCall) throws SerializationException {
        XMLMethodCall xmc = new XMLMethodCall();
        xmc.setMethodName(methodCall.getMethodName());

        List<XMLMapable> mapables = new ArrayList<XMLMapable>();
        for (Object o : methodCall.getArgs()) {
            mapables.add(toMapable(o));
        }

        xmc.setArgs(mapables);

        return xml(xmc);
    }

    private static String xml(Object o) {
        try {
            StringWriter writer = new StringWriter();
            serializer.serialize(o, writer);
            return writer.toString();
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private static XMLMapable toMapable(Object o) {
        XMLMapable m = new XMLMapable();

        if (o instanceof Map<?, ?>) {

        } else if (o instanceof Event) {

        } else if (o instanceof List<?>) {

        } else {
            XMLPrimitive primitive = toPrimitive(o);
            m.setValue(primitive);
        }

        return m;
    }

    private static XMLPrimitive toPrimitive(Object o) {
        XMLPrimitive p = new XMLPrimitive();

        if (o instanceof String) {
            p.setString((String) o);
            return p;
        }

        if (o instanceof Integer) {
            p.setInt((Integer) o);
            return p;
        }

        if (o instanceof Byte) {
            p.setByte((Byte) o);
            return p;
        }

        if (o instanceof Boolean) {
            p.setBoolean((Boolean) o);
            return p;
        }

        if (o instanceof Double) {
            p.setDouble((Double) o);
            return p;
        }

        if (o instanceof Float) {
            p.setFloat((Float) o);
            return p;
        }

        if (o instanceof Short) {
            p.setShort((Short) o);
            return p;
        }

        throw new IllegalStateException("Type not supported: " + o.getClass());
    }

    public static String toXml(ReturnValue returnValue) throws SerializationException {
        XMLReturnValue xrv = new XMLReturnValue();
        xrv.setValue(toMapable(returnValue.getValue()));
        return xml(xrv);
    }

    public static String toXml(Event event) throws SerializationException {
        XMLEvent xe = new XMLEvent();
        xe.setDomain(event.getDomain());
        xe.setName(event.getName());
        xe.setToolConnector(event.getToolConnector());

        ArrayList<XMLMapEntry> list = new ArrayList<XMLMapEntry>();
        for (String key : event.getKeys()) {
            XMLMapEntry entry = new XMLMapEntry();
            entry.setKey(key);
            entry.setValue(toMapable(event.getValue(key)));
            list.add(entry);
        }

        xe.setElements(list);

        return xml(xe);
    }

    public static MethodCall toMethodCall(String xml) throws SerializationException {
        XMLMethodCall xmc = serializer.deserialize(XMLMethodCall.class, new StringReader(xml));

        List<Object> args = new ArrayList<Object>();
        List<Class<?>> types = new ArrayList<Class<?>>();

        for (XMLMapable mapable : xmc.getArgs()) {
            Object o = toObject(mapable);
            args.add(o);
            types.add(o.getClass());
        }

        return new MethodCall(xmc.getMethodName(), args.toArray(), types.toArray(new Class<?>[types.size()]));
    }

    private static Object xmlArrayToObject(List<XMLMapable> array) {
        return null; // TODO
    }

    private static Object xmlMapToObject(List<XMLMapEntry> map) {
        return null; // TODO
    }

    private static Object toObject(XMLEvent event) {
        return null; // TODO
    }

    private static Object toObject(XMLPrimitive primitive) {
        if (primitive.ifBoolean()) {
            return primitive.isBoolean();
        }
        if (primitive.ifByte()) {
            return primitive.getByte();
        }
        if (primitive.ifDouble()) {
            return primitive.getDouble();
        }
        if (primitive.ifFloat()) {
            return primitive.getFloat();
        }
        if (primitive.ifInt()) {
            return primitive.getInt();
        }
        if (primitive.ifShort()) {
            return primitive.getShort();
        }
        if (primitive.ifString()) {
            return primitive.getString();
        }

        throw new IllegalStateException();
    }

    private static Object toObject(XMLMapable mapable) {
        if (mapable.ifValue()) {
            return toObject(mapable.getValue());
        }
        if (mapable.ifArray()) {
            return xmlArrayToObject(mapable.getArraies()); // ...
        }
        if (mapable.ifMap()) {
            return xmlMapToObject(mapable.getMaps());
        }
        if (mapable.ifEvent()) {
            return toObject(mapable.getEvent());
        }

        throw new IllegalStateException();
    }

    public static ReturnValue toReturnValue(String xml) throws SerializationException {
        XMLReturnValue xrv = serializer.deserialize(XMLReturnValue.class, new StringReader(xml));
        Object o = toObject(xrv.getValue());
        return new ReturnValue(o, o.getClass());
    }

    public static Event toEvent(String xml) throws SerializationException {
        XMLEvent xrv = serializer.deserialize(XMLEvent.class, new StringReader(xml));
        Event e = new Event(xrv.getDomain(), xrv.getName());
        e.setToolConnector(xrv.getToolConnector());
        // e.setValue(key, value) TODO
        return e;
    }
}
