/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.ui.web.ArgumentModel;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
    private ServiceId service;
    private MethodId method;
    private List<ArgumentModel> arguments = new ArrayList<ArgumentModel>();

    public ServiceId getService() {
        return this.service;
    }

    public void setService(ServiceId service) {
        this.service = service;
    }

    public MethodId getMethod() {
        return this.method;
    }

    public void setMethod(MethodId method) {
        this.method = method;
    }

    public List<ArgumentModel> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<ArgumentModel> arguments) {
        this.arguments = arguments;
    }

    public Object[] getArgumentsAsArray() {
        Object[] result = new Object[arguments.size()];

        for(int i = 0; i < arguments.size(); i++){
            result[i] = arguments.get(i).getValue();
        }
        return result;
    }

}
