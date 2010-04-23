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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ComponentType implements Serializable {
    private String name;
    private boolean bindingComponent;
    private String namespace;
    private String nsname;
    private List<EndpointType> endpoints;
    private List<BeanType> beans;

    public ComponentType() {
        readResolve();
    }

    public ComponentType(String name, String nsname, String namespace, boolean bindingComponent) {
        this.name = name;
        this.nsname = nsname;
        this.namespace = namespace;
        this.bindingComponent = bindingComponent;
        readResolve();
    }

    private Object readResolve() {
        if (endpoints == null) {
            endpoints = new ArrayList<EndpointType>();
        }
        if (beans == null) {
            beans = new ArrayList<BeanType>();
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isBindingComponent() {
        return bindingComponent;
    }

    public void setBindingComponent(boolean bindingComponent) {
        this.bindingComponent = bindingComponent;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNsname() {
        return nsname;
    }

    public void setNsname(String nsname) {
        this.nsname = nsname;
    }

    public List<EndpointType> getEndpoints() {
        return endpoints;
    }

    public EndpointType getEndpoint(String name) {
        for (EndpointType e : endpoints) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public void setEndpoints(List<EndpointType> endpoints) {
        this.endpoints = endpoints;
    }

    public List<BeanType> getBeans() {
        return beans;
    }

    public BeanType getBean(String clazz) {
        for (BeanType b : beans) {
            if (b.getClazz().equals(clazz)) {
                return b;
            }
        }
        return null;
    }

    public void setBeans(List<BeanType> beans) {
        System.out.println(beans);
        this.beans = beans;
    }

    public void addEndpoint(EndpointType e) {
        endpoints.add(e);
    }

    public void addBean(BeanType b) {
        beans.add(b);
    }
}
