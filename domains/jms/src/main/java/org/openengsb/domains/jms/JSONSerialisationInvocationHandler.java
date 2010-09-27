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

package org.openengsb.domains.jms;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

public class JSONSerialisationInvocationHandler implements InvocationHandler {

    private final Sender sender;

    public JSONSerialisationInvocationHandler(Sender sender) {
        super();
        this.sender = sender;
    }

    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2) {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, arg2);
            String send = sender.send(arg1.getName(), writer.toString());
            if (arg1.getReturnType().getName() != "void") {
                return mapper.readValue(send, TypeFactory.type(arg1.getGenericReturnType()));
            } else {
                return null;
            }
        } catch (JsonGenerationException e) {
            throw new JMSConnectorException(e.getMessage());
        } catch (JsonMappingException e) {
            throw new JMSConnectorException(e.getMessage());
        } catch (JsonParseException e) {
            throw new JMSConnectorException(e.getMessage());
        } catch (IOException e) {
            throw new JMSConnectorException(e.getMessage());
        }
    }

}
