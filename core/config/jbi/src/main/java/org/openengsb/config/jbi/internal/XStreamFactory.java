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
package org.openengsb.config.jbi.internal;

import org.openengsb.config.jbi.types.AbstractType;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.BoolType;
import org.openengsb.config.jbi.types.ChoiceType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointNameType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.IntType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.jbi.types.ServiceEndpointTargetType;
import org.openengsb.config.jbi.types.ServiceNameType;
import org.openengsb.config.jbi.types.StringType;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory {
    public static XStream createXStream() {
        XStream x = new XStream();
        configureSimpleTypes(x);
        configureBeanType(x);
        configureEndpointType(x);
        configureComponentType(x);
        return x;
    }

    private static void configureSimpleTypes(XStream x) {
        x.useAttributeFor(AbstractType.class, "name");
        x.useAttributeFor(AbstractType.class, "optional");
        x.useAttributeFor(AbstractType.class, "maxLength");
        x.useAttributeFor(AbstractType.class, "defaultValue");
        x.alias("int", IntType.class);
        x.useAttributeFor(IntType.class, "min");
        x.useAttributeFor(IntType.class, "max");
        x.alias("string", StringType.class);
        x.alias("serviceName", ServiceNameType.class);
        x.useAttributeFor(ServiceNameType.class, "target");
        x.alias("endpointName", EndpointNameType.class);
        x.useAttributeFor(EndpointNameType.class, "target");
        x.alias("bool", BoolType.class);
        x.alias("choice", ChoiceType.class);
        x.useAttributeFor(ChoiceType.class, "values");
        x.alias("ref", RefType.class);
        x.useAttributeFor(RefType.class, "theClass");
        x.aliasAttribute(RefType.class, "theClass", "clazz");
        x.alias("endpointTarget", ServiceEndpointTargetType.class);
        x.useAttributeFor(ServiceEndpointTargetType.class, "serviceName");
        x.useAttributeFor(ServiceEndpointTargetType.class, "endpointName");
    }

    private static void configureBeanType(XStream x) {
        x.alias("bean", BeanType.class);
        x.useAttributeFor(BeanType.class, "clazz");
    }

    private static void configureEndpointType(XStream x) {
        x.alias("endpoint", EndpointType.class);
        x.useAttributeFor(EndpointType.class, "name");
    }

    private static void configureComponentType(XStream x) {
        x.alias("component", ComponentType.class);
        x.useAttributeFor(ComponentType.class, "name");
        x.useAttributeFor(ComponentType.class, "bindingComponent");
        x.useAttributeFor(ComponentType.class, "namespace");
        x.useAttributeFor(ComponentType.class, "nsname");
    }
}
