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
package org.openengsb.config.jbi.types;

import java.util.Map;

import org.w3c.dom.Element;

@SuppressWarnings("serial")
public class ServiceEndpointTargetType extends AbstractType {
    private String serviceName;
    private String endpointName;

    public ServiceEndpointTargetType() {
    }

    public ServiceEndpointTargetType(String name, boolean optional, int maxLength, String defaultValue) {
        super(name, optional, maxLength, defaultValue);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    private Object readResolve() {
        if (serviceName == null) {
            serviceName = "targetService";
        }
        if (endpointName == null) {
            endpointName = "targetEndpoint";
        }
        return this;
    }

    @Override
    public void toAttributeOnElement(Map<String, String> context, Element elem) {
        String target = context.get(getName());
        elem.setAttribute(serviceName, target.substring(0, target.indexOf('.')));
        elem.setAttribute(endpointName, target.substring(target.indexOf('.') + 1));
    }

    @Override
    public void toPropertyOnElement(Map<String, String> context, Element elem) {
        throw new UnsupportedOperationException();
    }
}
