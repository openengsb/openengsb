package org.openengsb.util.serialization;

import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbHelper {
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Class<T> docClass, Reader reader) throws JAXBException {
        String packageName = docClass.getPackage().getName();
        JAXBContext jc = JAXBContext.newInstance(packageName);
        Unmarshaller u = jc.createUnmarshaller();
        return (T) u.unmarshal(reader);
    }

    public static void marshal(Object doc, Writer writer) throws JAXBException {
        JAXBContext context1 = JAXBContext.newInstance(doc.getClass());
        Marshaller m = context1.createMarshaller();
        m.marshal(doc, writer);
    }
}
