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
package org.openengsb.util.serialization;

import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;

public class JaxbXmlSerializer implements Serializer {

    protected Unmarshaller unmarshaller;
    protected Marshaller marshaller;

    public JaxbXmlSerializer(String packageName) throws SerializationException {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(packageName);
        } catch (JAXBException e) {
            throw new SerializationException("could not initialize " + this.getClass().getName(), e);
        }
        initHandlers(context);
    }

    public JaxbXmlSerializer(Class<?> docClass) throws SerializationException {
        this(docClass.getPackage().getName());
    }

    public JaxbXmlSerializer(Class<?>... docClasses) throws SerializationException {
        JAXBContext context;
        try {
            context = JAXBContext.newInstance(docClasses);
        } catch (JAXBException e) {
            throw new SerializationException("could not initialize " + this.getClass().getName(), e);
        }
        initHandlers(context);
    }

    public JaxbXmlSerializer(JAXBContext context) throws SerializationException {
        initHandlers(context);
    }

    private void initHandlers(JAXBContext context) throws SerializationException {
        try {
            unmarshaller = context.createUnmarshaller();
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            throw new SerializationException("Could not initialize Handlers.", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Class<T> clazz, Reader reader) throws SerializationException {
        try {
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at deserializing", e);
        }
    }

    public <T> T deserialize(Class<T> clazz, Source source) throws SerializationException {
        try {
            return unmarshaller.unmarshal(source, clazz).getValue();
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at deserializing", e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, Node node) throws SerializationException {
        try {
            return unmarshaller.unmarshal(node, clazz).getValue();
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at deserializing", e);
        }
    }

    @Override
    public void serialize(Object object, Writer writer) throws SerializationException {
        try {
            marshaller.marshal(object, writer);
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at serializing", e);
        }
    }

    @Override
    public Node serializeToDOM(Object o) throws SerializationException {
        DOMResult result = new DOMResult();
        try {
            marshaller.marshal(o, result);
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at serializing", e);
        }
        return result.getNode();
    }

    @Override
    public Source serializeToSource(Object o) throws SerializationException {
        return new DOMSource(serializeToDOM(o));
    }
}
