package org.openengsb.core.common.remote;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This filter takes a {@link Document} representing a {@link MethodCallRequest} and deserializes it. The
 * MethodCallRequest is then passed to the next filter. The resulting {@link MethodResultMessage} is then seralized to
 * XML again and returned.
 *
 * This filter is intended for incoming ports.
 *
 * <code>
 * <pre>
 *      [org.w3c.dom.Document] > Filter > [MethodCallRequest]     > ...
 *                                                                   |
 *                                                                   v
 *      [org.w3c.dom.Document] < Filter < [MethodResultMessage]   < ...
 * </pre>
 * </code>
 */
public class XmlMethodCallMarshalFilter extends AbstractFilterChainElement<Document, Document> {

    private FilterAction next;
    private Unmarshaller unmarshaller;

    public XmlMethodCallMarshalFilter() {
        super(Document.class, Document.class);
        try {
            JAXBContext context = JAXBContext.newInstance(MethodCallRequest.class, MethodResultMessage.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Document doFilter(Document input, Map<String, Object> metadata) throws FilterException {
        MethodCallRequest call;
        try {
            call = parseMethodCall(input);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        MethodResultMessage result = (MethodResultMessage) next.filter(call, metadata);
        return serializeResult(result);
    }

    private Document serializeResult(MethodResultMessage result) {
        DOMResult domResult = new DOMResult();
        try {
            JAXBContext jaxbContext =
                JAXBContext.newInstance(MethodResultMessage.class, Class.forName(result.getResult().getClassName()));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(new JAXBElement<MethodResultMessage>(
                new QName(MethodResultMessage.class.getSimpleName()),
                MethodResultMessage.class, result), domResult);
        } catch (JAXBException e) {
            throw new FilterException(e);
        } catch (ClassNotFoundException e) {
            throw new FilterException(e);
        }
        return (Document) domResult.getNode();
    }

    private MethodCallRequest parseMethodCall(Document input) throws JAXBException {
        MethodCallRequest request = unmarshaller.unmarshal(input, MethodCallRequest.class).getValue();
        MethodCall result = request.getMethodCall();
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
        JAXBContext jaxbContext = JAXBContext.newInstance(clazzes);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object[] args = result.getArgs();
        for (int i = 0; i < args.length; i++) {
            args[i] = unmarshaller.unmarshal((Node) args[i], clazzes[i]).getValue();
        }
        return request;
    }

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;

    }

}
