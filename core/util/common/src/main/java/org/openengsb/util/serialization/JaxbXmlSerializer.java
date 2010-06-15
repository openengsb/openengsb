package org.openengsb.util.serialization;

import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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

    @Override
    public <T> void serialize(T object, Writer writer) throws SerializationException {
        try {
            marshaller.marshal(object, writer);
        } catch (JAXBException e) {
            throw new SerializationException("Jaxb failed at serializing", e);
        }
    }

}
