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
package org.openengsb.persistence;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.DirectMessageHandlingEndpoint;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.core.xmlmapping.XMLEvent;
import org.openengsb.core.xmlmapping.XMLMappable;
import org.openengsb.core.xmlmapping.XMLMappableList;
import org.openengsb.core.xmlmapping.XMLMethodCall;
import org.openengsb.core.xmlmapping.XMLReturnValue;
import org.openengsb.core.xmlmapping.XMLTypedValue;
import org.openengsb.util.serialization.JaxbXmlSerializer;
import org.openengsb.util.serialization.SerializationException;
import org.openengsb.util.serialization.Serializer;

/**
 * @org.apache.xbean.XBean element="persistenceEndpoint"
 *                         description="Persistence Component"
 */
public class PersistenceEndpoint extends DirectMessageHandlingEndpoint<Persistence> {

    private static Serializer serializer = null;

    private PersistenceInternal persistence;

    private static Serializer getSerializer() throws SerializationException {
        if (serializer == null) {
            serializer = new JaxbXmlSerializer(XMLEvent.class.getPackage().getName());
        }
        return serializer;
    }

    @Override
    protected void handleMethodCallManually(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper, MessageProperties msgProperties) throws Exception {
        XMLMethodCall xmlCall = getXmlMethodCall(in);

        List<Object> args = new ArrayList<Object>();
        List<Class<?>> types = new ArrayList<Class<?>>();

        for (XMLTypedValue arg : xmlCall.getArgs()) {
            XMLMappable value = arg.getValue();
            if (value.ifList()) {
                List<PersistenceObject> list = transformList(value);
                args.add(list);
                types.add(List.class);
            } else if (value.ifBean()) {
                args.add(new PersistenceObject(toXml(value), arg.getType()));
                types.add(PersistenceObject.class);
            } else {
                throw new IllegalStateException("Only java.util.List and beans are supported.");
            }
        }

        MethodCall methodCall = new MethodCall(xmlCall.getMethodName(), args.toArray(), types
                .toArray(new Class<?>[types.size()]));
        ReturnValue returnValue = methodCall.invoke(persistence);

        String transformed = transformReturnValue(returnValue);

        out.setContent(new StringSource(transformed));
    }

    @SuppressWarnings("unchecked")
    private String transformReturnValue(ReturnValue returnValue) throws SerializationException {
        if (returnValue.getValue() == null) {
            return Transformer.toXml(returnValue);
        }
        List<PersistenceObject> result = (List<PersistenceObject>) returnValue.getValue();
        List<XMLMappable> transformed = new ArrayList<XMLMappable>(result.size());
        for (PersistenceObject po : result) {
            transformed.add(toMappable(po.getXml()));
        }

        XMLMappableList list = new XMLMappableList();
        list.setMappables(transformed);

        XMLMappable mappable = new XMLMappable();
        mappable.setList(list);

        XMLTypedValue typedValue = new XMLTypedValue();
        typedValue.setType(List.class.getName());
        typedValue.setValue(mappable);

        XMLReturnValue xmlReturnValue = new XMLReturnValue();
        xmlReturnValue.setValue(typedValue);

        return toXml(xmlReturnValue);
    }

    private XMLMappable toMappable(String xml) throws SerializationException {
        return getSerializer().deserialize(XMLMappable.class, new StringReader(xml));
    }

    private List<PersistenceObject> transformList(XMLMappable value) throws SerializationException {
        List<PersistenceObject> list = new ArrayList<PersistenceObject>();
        XMLMappableList mappableList = value.getList();
        for (XMLMappable mappable : mappableList.getMappables()) {
            if (!value.ifBean()) {
                throw new IllegalStateException("Only beans are supported as part of the list.");
            }
            list.add(new PersistenceObject(toXml(mappable), mappable.getBean().getClassName()));
        }
        return list;
    }

    private String toXml(Object value) throws SerializationException {
        StringWriter writer = new StringWriter();
        getSerializer().serialize(value, writer);
        return writer.toString();
    }

    private XMLMethodCall getXmlMethodCall(NormalizedMessage in) throws TransformerException, SerializationException {
        String xml = new SourceTransformer().toString(in.getContent());
        XMLMethodCall xmc = getSerializer().deserialize(XMLMethodCall.class, new StringReader(xml));
        return xmc;
    }

    public void setPersistenceImpl(PersistenceInternal persistence) {
        this.persistence = persistence;
    }

}
