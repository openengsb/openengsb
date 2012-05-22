/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This filter takes a {@link MethodCallRequest} and serializes it into a {@link Document}. The document is then passed
 * to the next filter. The resulting document is then deseralized and returned.
 *
 * This filter is intended for outgoing ports.
 *
 * <code>
 * <pre>
 *      [MethodCallRequest] > Filter > [org.w3c.dom.Document]     > ...
 *                                                                   |
 *                                                                   v
 *      [MethodResultMessage] < Filter < [org.w3c.dom.Document]   < ...
 * </pre>
 * </code>
 */
public class XmlMethodCallMarshalOutgoingFilter extends
        AbstractFilterChainElement<MethodCallRequest, MethodResultMessage> {

    private FilterAction next;
    private Unmarshaller unmarshaller;

    public XmlMethodCallMarshalOutgoingFilter() {
        try {
            JAXBContext context = JAXBContext.newInstance(MethodCallRequest.class, MethodResultMessage.class);
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metaData)
        throws FilterException {
        Document serializeRequest = serializeRequest(input);
        Document resultDocument = (Document) next.filter(serializeRequest, metaData);
        try {
            return parseMethodResult(resultDocument);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
    }

    private Document serializeRequest(MethodCallRequest result) {
        DOMResult domResult = new DOMResult();
        try {
            @SuppressWarnings("unchecked")
            List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(result.getMethodCall().getClasses());
            if (classes.contains(null)) {
                throw new FilterException("Could not load all required classes. Require: "
                        + result.getMethodCall().getClasses() + " got: " + classes);
            }
            classes.add(MethodCallRequest.class);
            JAXBContext jaxbContext =
                JAXBContext.newInstance(classes.toArray(new Class<?>[classes.size()]));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(new JAXBElement<MethodCallRequest>(new QName(MethodCallRequest.class.getSimpleName()),
                MethodCallRequest.class, result), domResult);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        return (Document) domResult.getNode();
    }

    private MethodResultMessage parseMethodResult(Document input) throws JAXBException {
        MethodResultMessage request = unmarshaller.unmarshal(input, MethodResultMessage.class).getValue();
        MethodResult result = request.getResult();
        Class<?> resultClass;
        try {
            resultClass = Class.forName(result.getClassName());
        } catch (ClassNotFoundException e) {
            throw new FilterException(e);
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(MethodResult.class, resultClass);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Object parsedArg = unmarshaller.unmarshal((Node) result.getArg(), resultClass).getValue();
        result.setArg(parsedArg);
        return request;
    }

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, Document.class, Document.class);
        this.next = next;

    }

}
