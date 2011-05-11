package org.openengsb.core.common.remote;

import java.util.List;

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
import org.openengsb.core.api.remote.MethodReturn;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlMethodCallMarshalFilter extends AbstractFilterChainElement<Document, Document> {

    private FilterAction next;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    public XmlMethodCallMarshalFilter() {
        super(Document.class, Document.class);
        try {
            JAXBContext context = JAXBContext.newInstance(MethodCall.class, MethodReturn.class);
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Document doFilter(Document input) throws FilterException {
        MethodCall call;
        try {
            call = parseMethodCall(input);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        MethodReturn result = (MethodReturn) next.filter(call);
        return serializeResult(result);
    }

    private Document serializeResult(MethodReturn result) {
        DOMResult domResult = new DOMResult();
        try {
            marshaller.marshal(new JAXBElement<MethodReturn>(new QName(MethodReturn.class.getSimpleName()),
                MethodReturn.class, result), domResult);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return (Document) domResult.getNode();
    }

    private MethodCall parseMethodCall(Document input) throws JAXBException {
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
    }

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, MethodCall.class, MethodReturn.class);
        this.next = next;

    }

}
