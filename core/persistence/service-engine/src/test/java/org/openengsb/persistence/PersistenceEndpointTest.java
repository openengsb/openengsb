/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.persistence;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

@RunWith(Parameterized.class)
public class PersistenceEndpointTest extends AbstractPersistenceEndpointTest {

    protected Class<?> objectClass;
    protected Object o1;
    protected Object sample1;
    protected Object updated1;

    public PersistenceEndpointTest(Class<?> objectClass, Object o1, Object sample1, Object udpated1) {
        this.objectClass = objectClass;
        this.o1 = o1;
        this.sample1 = sample1;
        this.updated1 = udpated1;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return PersistenceTest.data();
    }

    @Test
    public void createAndQuery() throws Exception {
        PersistenceObject po = PersistenceTest.makePersistenceObject(o1);
        Source content = makeMethodCall("create", po);
        InOut io = sendInOutMessage(new QName("methodcall"), content);
        if (ExchangeStatus.ERROR.equals(io.getStatus())) {
            throw io.getError();
        }
        po = PersistenceTest.makePersistenceObject(o1);
        content = makeMethodCall("query", po);
        io = sendInOutMessage(new QName("methodcall"), content);
        if (ExchangeStatus.ERROR.equals(io.getStatus())) {
            throw io.getError();
        }
        Source resultSource = io.getOutMessage().getContent();
        assertNotNull(resultSource);
        PersistenceObject resultPo = (PersistenceObject) getReturnValue(resultSource);
        Object resultObject = PersistenceTest.parsePersistenceObject(resultPo);
        assertEquals(o1, resultObject);
    }

    protected Object getReturnValue(Source xmlSource) throws TransformerConfigurationException, TransformerException,
            SerializationException {
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
        ReturnValue value = Transformer.toReturnValue(sw.toString());
        List<Object> valueList = (List) value.getValue();
        return valueList.get(0);
    }

    protected Source makeMethodCall(String methodName, Object... args) throws SerializationException {
        Class<?>[] classes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        MethodCall mc = new MethodCall(methodName, args, classes);
        String xml = Transformer.toXml(mc);
        return new StreamSource(new StringReader(xml));
    }

    protected InOnly sendInOnlyMessage(QName operation, Source content) throws MessagingException, JAXBException {
        InOnly inonly = client.createInOnlyExchange();
        inonly.setService(new QName("urn:test", "persistence"));
        inonly.setOperation(operation);
        inonly.getInMessage().setContent(content);
        client.sendSync(inonly);
        return inonly;
    }

    protected InOut sendInOutMessage(QName operation, Source content) throws MessagingException, JAXBException {
        InOut inout = client.createInOutExchange();
        inout.setService(new QName("urn:test", "persistence"));
        inout.setOperation(operation);
        inout.getInMessage().setContent(content);
        client.sendSync(inout);
        return inout;
    }
}
