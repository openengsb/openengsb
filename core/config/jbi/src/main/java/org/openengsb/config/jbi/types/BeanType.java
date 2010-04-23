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
public class BeanType implements Serializable {
    private String clazz;
    private List<AbstractType> properties;
    private ComponentType parent;

    public BeanType() {
        readResolve();
    }

    public BeanType(String clazz) {
        this.clazz = clazz;
        readResolve();
    }

    private Object readResolve() {
        if (properties == null) {
            properties = new ArrayList<AbstractType>();
        }
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String theClass) {
        this.clazz = theClass;
    }

    public List<AbstractType> getProperties() {
        return properties;
    }

    public void setProperties(List<AbstractType> properties) {
        this.properties = properties;
    }

    public void addProperty(AbstractType p) {
        properties.add(p);
    }

    public ComponentType getParent() {
        return parent;
    }

    public void setParent(ComponentType parent) {
        this.parent = parent;
    }
}
