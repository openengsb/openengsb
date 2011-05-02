package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfig;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.MethodReturn.ReturnType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PipelineTest {

    private final class XmlDecoder implements FilterAction<String, Document> {
        @Override
        public Document apply(String input) {
            return parseDocument(input);
        }
    }

    private final class XmlEncoder implements FilterAction<Document, String> {
        @Override
        public String apply(Document input) {
            try {
                return docToXml(input);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class XmlUnmarshaller implements FilterAction<Document, MethodCall> {
        private final Unmarshaller unmarshaller;

        private XmlUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        @Override
        public MethodCall apply(Document input) {
            try {
                MethodCall result = unmarshaller.unmarshal(input, MethodCall.class).getValue();
                List<String> classNames = result.getClasses();
                Class<?>[] clazzes = new Class<?>[classNames.size()];
                ClassLoader cl = this.getClass().getClassLoader();
                for (int i = 0; i < classNames.size(); i++) {
                    try {
                        clazzes[i] = cl.loadClass(classNames.get(i));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                JAXBContext jaxbContext2 = JAXBContext.newInstance(clazzes);
                Unmarshaller unmarshaller = jaxbContext2.createUnmarshaller();
                Object[] args = result.getArgs();
                for (int i = 0; i < args.length; i++) {
                    args[i] = unmarshaller.unmarshal((Node) args[i], clazzes[i]).getValue();
                }
                return result;
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class XmlMarshaller implements FilterAction<MethodReturn, Document> {
        private final Marshaller marshaller;

        private XmlMarshaller(Marshaller marshaller) {
            this.marshaller = marshaller;
        }

        @Override
        public Document apply(MethodReturn input) {
            DOMResult result = new DOMResult();
            try {
                marshaller.marshal(new JAXBElement<MethodReturn>(new QName(MethodReturn.class.getSimpleName()),
                    MethodReturn.class, input), result);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            return (Document) result.getNode();
        }
    }

    private final class JsonMarshaller implements FilterAction<MethodReturn, String> {
        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public String apply(MethodReturn input) {
            try {
                return mapper.writeValueAsString(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class JsonUnmarshaller implements FilterAction<String, MethodCall> {
        private ObjectMapper mapper = new ObjectMapper();

        @Override
        public MethodCall apply(String input) {
            try {
                return mapper.readValue(input, MethodCall.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final class RequestHandlerFunction implements FilterAction<MethodCall, MethodReturn> {
        @Override
        public MethodReturn apply(MethodCall input) {
            return new MethodReturn(ReturnType.Object, input.getArgs()[0], new HashMap<String, String>(), input
                .getCallId());
        }
    }

    @Test
    public void testArchWithJson() throws Exception {
        MarshallingPortFactory<String> marshallingPort = new MarshallingPortFactory<String>();
        marshallingPort.setRequestUnmarshaller(new JsonUnmarshaller());
        marshallingPort.setResponseMarshaller(new JsonMarshaller());
        marshallingPort.setExecutionHandler(new RequestHandlerFunction());

        FilterConfig<String, String> jsonMarshallingPort = marshallingPort.createNewInstance();

        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall methodCall = new MethodCall();
        methodCall.setArgs(new Object[] { "foo" });
        methodCall.setCallId("bar");
        String input = objectMapper.writeValueAsString(methodCall);
        String result = jsonMarshallingPort.apply(input);
        MethodReturn returnValue = objectMapper.readValue(result, MethodReturn.class);
        assertThat((String) returnValue.getArg(), is("foo"));
        assertThat(returnValue.getCallId(), is("bar"));
    }

    @Test
    public void testArchWithXml() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(MethodCall.class, MethodReturn.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final Marshaller marshaller = jaxbContext.createMarshaller();

        MarshallingPortFactory<Document> marshallingPort = new MarshallingPortFactory<Document>();
        marshallingPort.setRequestUnmarshaller(new XmlUnmarshaller(unmarshaller));
        marshallingPort.setExecutionHandler(new RequestHandlerFunction());
        marshallingPort.setResponseMarshaller(new XmlMarshaller(marshaller));

        EncodingPortFactory<String, Document> port = new EncodingPortFactory<String, Document>();
        port.setDecoder(new XmlDecoder());
        port.setEncoder(new XmlEncoder());
        port.setMarshallingPort(marshallingPort.createNewInstance());

        FilterConfig<String, String> fullPort = port.createNewInstance();
        MethodCall call = new MethodCall();
        call.setArgs(new Object[] { "foo" });
        call.setClasses(Arrays.asList(String.class.getName()));
        call.setCallId("bar");

        DOMResult domResult = new DOMResult();
        marshaller.marshal(new JAXBElement<MethodCall>(new QName(MethodCall.class.getSimpleName()), MethodCall.class,
                call), domResult);
        String input = docToXml(domResult.getNode());
        String result = fullPort.apply(input);

        Document parseDocument = parseDocument(result);
        MethodReturn value = unmarshaller.unmarshal(parseDocument, MethodReturn.class).getValue();
        String value2 = unmarshaller.unmarshal((Node) value.getArg(), String.class).getValue();
        value.setArg(value2);
        assertThat((String) value.getArg(), is("foo"));
        assertThat(value.getCallId(), is("bar"));
    }

    private static Document parseDocument(String input) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(input)));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String docToXml(Node input) throws TransformerFactoryConfigurationError, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(input), new StreamResult(sw));
        return sw.toString();
    }
}
