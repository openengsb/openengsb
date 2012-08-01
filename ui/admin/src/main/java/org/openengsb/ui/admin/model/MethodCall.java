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

package org.openengsb.ui.admin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
    private ServiceId service;
    private MethodId method;
    private List<Argument> arguments = new ArrayList<Argument>();

    public MethodId getMethod() {
        return this.method;
    }

    public void setMethod(MethodId method) {
        this.method = method;
    }

    public List<Argument> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public ServiceId getService() {
        return this.service;
    }

    public void setService(ServiceId service) {
        this.service = service;
    }

    public Object[] getArgumentsAsArray() throws ArgumentConversionException {
        Object[] result = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            try {
                result[i] = arguments.get(i).toValue();
            } catch (IllegalArgumentException e) {
                Argument arg = arguments.get(i);
                throw new ArgumentConversionException("Error during conversion of an argument number", e, arg);
            }
        }
        return result;
    }
}
