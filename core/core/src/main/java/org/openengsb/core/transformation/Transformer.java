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

import org.openengsb.core.model.Event;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.xmlmapping.XMLEvent;
import org.openengsb.core.xmlmapping.XMLMethodCall;
import org.openengsb.core.xmlmapping.XMLReturnValue;
import org.openengsb.core.xmlmapping.XMLTypedValue;
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
        ToXmlTypesTransformer transformer = new ToXmlTypesTransformer();

        List<XMLTypedValue> arguments = new ArrayList<XMLTypedValue>();
        for (int i = 0; i < methodCall.getArgs().length; i++) {
            Object o = methodCall.getArgs()[i];
            XMLTypedValue arg = new XMLTypedValue();
            arg.setValue(transformer.toMapable(o));
            arg.setType(methodCall.getTypes()[i].getName());
            arguments.add(arg);
        }

        xmc.setArgs(arguments);

        return xml(xmc);
    }

    public static String toXml(ReturnValue returnValue) throws SerializationException {
        ToXmlTypesTransformer transformer = new ToXmlTypesTransformer();
        XMLReturnValue xrv = new XMLReturnValue();

        XMLTypedValue typedValue = new XMLTypedValue();
        typedValue.setType(returnValue.getType().getName());

        typedValue.setValue(transformer.toMapable(returnValue.getValue()));

        xrv.setValue(typedValue);
        return xml(xrv);
    }

    public static String toXml(Event event) throws SerializationException {
        ToXmlTypesTransformer transformer = new ToXmlTypesTransformer();
        XMLEvent xe = transformer.toXmlEvent(event);
        return xml(xe);
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

    public static MethodCall toMethodCall(String xml) throws SerializationException {

        FromXmlTypesTransformer transformer = new FromXmlTypesTransformer();
        XMLMethodCall xmc = serializer.deserialize(XMLMethodCall.class, new StringReader(xml));

        List<Object> args = new ArrayList<Object>();
        List<Class<?>> types = new ArrayList<Class<?>>();

        for (XMLTypedValue arg : xmc.getArgs()) {
            Object o = transformer.toObject(arg.getValue());
            args.add(o);
            types.add(TransformerUtil.simpleGetClass(arg.getType()));
        }

        return new MethodCall(xmc.getMethodName(), args.toArray(), types.toArray(new Class<?>[types.size()]));
    }

    public static ReturnValue toReturnValue(String xml) throws SerializationException {
        FromXmlTypesTransformer transformer = new FromXmlTypesTransformer();
        XMLReturnValue xrv = serializer.deserialize(XMLReturnValue.class, new StringReader(xml));
        XMLTypedValue typedValue = xrv.getValue();
        Object o = transformer.toObject(typedValue.getValue());
        Class<?> simpleGetClass = null;
        if (typedValue.getType().equals("void")) {
            simpleGetClass = void.class;
        } else {
            simpleGetClass = TransformerUtil.simpleGetClass(typedValue.getType());
        }
        return new ReturnValue(o, simpleGetClass);
    }

    public static Event toEvent(String xml) throws SerializationException {
        XMLEvent xrv = serializer.deserialize(XMLEvent.class, new StringReader(xml));
        FromXmlTypesTransformer transformer = new FromXmlTypesTransformer();
        return transformer.toEvent(xrv, "-1");
    }
}
