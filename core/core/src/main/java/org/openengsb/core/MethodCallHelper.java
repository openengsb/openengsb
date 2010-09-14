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

package org.openengsb.core;

import java.lang.reflect.Method;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;

public class MethodCallHelper {

    public static Object sendMethodCall(OpenEngSBEndpoint endpoint, QName service, Method method, Object[] args,
            MessageProperties msgProperties) {
        Object[] arguments = checkArgs(args);
        try {
            InOut inout = endpoint.getExchangeFactory().createInOutExchange();
            inout.setService(service);
            inout.setOperation(new QName("methodcall"));

            NormalizedMessage msg = inout.createMessage();
            inout.setInMessage(msg);

            msgProperties.applyToMessage(msg);
            MethodCall call = new MethodCall(method, arguments);

            String xml = Transformer.toXml(call);

            msg.setContent(new StringSource(xml));

            endpoint.sendSync(inout);

            checkFailure(inout, method);

            NormalizedMessage outMessage = inout.getOutMessage();
            String outXml = new SourceTransformer().toString(outMessage.getContent());

            ReturnValue returnValue = Transformer.toReturnValue(outXml);

            return returnValue.getValue();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkFailure(InOut inout, Method method) {
        if (inout.getStatus() != ExchangeStatus.ERROR) {
            return;
        }
        Exception e = inout.getError();
        if (e != null) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Failure invoking method " + method + ". No result delivered.");
    }

    private static Object[] checkArgs(Object[] args) {
        if (args != null) {
            return args;
        }
        return new Object[0];
    }

}
