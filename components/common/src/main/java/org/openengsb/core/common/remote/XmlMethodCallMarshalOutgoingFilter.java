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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.xml.bind.api.JAXBRIContext;


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

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMethodCallMarshalOutgoingFilter.class);

    private FilterAction next;
    
    private OsgiUtilsService utilsService;

    public XmlMethodCallMarshalOutgoingFilter(OsgiUtilsService utilsService) {
        super(MethodCallRequest.class, MethodResultMessage.class);
        this.utilsService = utilsService;
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
            JAXBContext jaxbContext = JAXBContext.newInstance(); // classes.toArray(new Class<?>[classes.size()])
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(new JAXBElement<MethodCallRequest>(new QName(MethodCallRequest.class.getSimpleName()),
                MethodCallRequest.class, result), domResult);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        return (Document) domResult.getNode();
    }

    private Class<?>[] getAllClasses() {
        Collection<Class<?>> result = Sets.newHashSet();
        for (ClassProvider cl : utilsService.listServices(ClassProvider.class)) {
            Collection<Class<?>> supportedTypes = cl.listClasses();
            result.addAll(supportedTypes);
        }
        result.add(SecureRequest.class);
        result.add(SecureResponse.class);
        result.add(MethodCallRequest.class);
        result.add(MethodResultMessage.class);
        result.add(MethodCall.class);
        result.add(MethodResult.class);
        return result.toArray(new Class<?>[result.size()]);
    }

    private MethodResultMessage parseMethodResult(Document input) throws JAXBException {
        Class<?>[] allClasses = getAllClasses();
        LOGGER.info(Arrays.toString(allClasses));
        JAXBContext jaxbContext =
            JAXBContext.newInstance(allClasses,
                ImmutableMap.of(JAXBRIContext.ANNOTATION_READER, new CustomAnnotationReader()));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(input, MethodResultMessage.class).getValue();
    }

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, Document.class, Document.class);
        this.next = next;

    }

}
