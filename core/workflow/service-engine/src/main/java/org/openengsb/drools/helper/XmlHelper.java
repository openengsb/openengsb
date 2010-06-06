package org.openengsb.drools.helper;

import java.io.InputStream;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

public class XmlHelper {

    public static <T> T unmarshal(Class<T> docClass, Source source) throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        return (T) u.unmarshal(source);
    }

    public static <T> T unmarshal(Class<T> docClass, InputStream inputStream) throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        return (T) u.unmarshal(inputStream);
    }

    public static void marshal(Object doc, Writer writer) throws JAXBException {
        JAXBContext context1 = JAXBContext.newInstance(doc.getClass());
        Marshaller m = context1.createMarshaller();
        m.marshal(doc, writer);
    }

}
