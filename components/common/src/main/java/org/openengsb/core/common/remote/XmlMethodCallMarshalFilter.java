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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.xml.bind.api.JAXBRIContext;

/**
 * This filter takes a {@link org.w3c.dom.Document} representing a {@link MethodCallRequest} and deserializes it. The
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
public class XmlMethodCallMarshalFilter extends AbstractFilterChainElement<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlMethodCallMarshalFilter.class);

    private FilterAction next;
    private OsgiUtilsService utilsService;
    
    public XmlMethodCallMarshalFilter(OsgiUtilsService utilsService) {
        super(String.class, String.class);
        this.utilsService = utilsService;
    }

    @Override
    public String doFilter(String input, Map<String, Object> metadata) throws FilterException {
        MethodCallRequest call;
        try {
            call = parseMethodCall(input);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        MethodResultMessage result = (MethodResultMessage) next.filter(call, metadata);
        return serializeResult(result);
    }

    private String serializeResult(MethodResultMessage result) {
        Class<?>[] allClasses = getAllClasses();
        LOGGER.info(Arrays.toString(allClasses));
        StringWriter resultStringWriter;
        try {
            JAXBContext jaxbContext =
                    JAXBContext.newInstance(allClasses,
                        ImmutableMap.of(JAXBRIContext.ANNOTATION_READER, new CustomAnnotationReader()));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            resultStringWriter = new StringWriter();
            marshaller.marshal(result, resultStringWriter);
        } catch (JAXBException e) {
            throw new FilterException(e);
        }
        return resultStringWriter.toString();
    }

    private MethodCallRequest parseMethodCall(String input) throws JAXBException {
        Class<?>[] allClasses = getAllClasses();
        LOGGER.info(Arrays.toString(allClasses));
        JAXBContext jaxbContext =
                JAXBContext.newInstance(allClasses,
                    ImmutableMap.of(JAXBRIContext.ANNOTATION_READER, new CustomAnnotationReader()));
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        MethodCallRequest request = (MethodCallRequest) unmarshaller.unmarshal(new StringReader(input));
        return request;
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

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;

    }

}
