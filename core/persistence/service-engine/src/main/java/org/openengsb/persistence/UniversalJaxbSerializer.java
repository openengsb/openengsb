package org.openengsb.persistence;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class UniversalJaxbSerializer {

    public String serialize(Object o) throws JAXBException {
        Class<?> objectClass = o.getClass();
        JAXBContext context = JAXBContext.newInstance(objectClass);
        Marshaller m = context.createMarshaller();
        StringWriter resultWriter = new StringWriter();
        StreamResult result = new StreamResult(resultWriter);
        JAXBElement<?> jaxbElement = new JAXBElement(new QName(objectClass.getSimpleName()), objectClass, o);
        m.marshal(jaxbElement, result);
        // m.marshal(jaxbElement, System.out);
        // return (Document) result.getNode();
        return resultWriter.toString();
    }

    public <T> T deserialize(Class<T> clazz, String doc) throws JAXBException {
        Source source = new StreamSource(new StringReader(doc));
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller u = context.createUnmarshaller();
        return u.unmarshal(source, clazz).getValue();
    }
}
