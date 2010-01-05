/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.jbi.test.unit;

import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.openengsb.config.jbi.BeanInfo;
import org.openengsb.config.jbi.EndpointInfo;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.BeanType;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointNameType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.RefType;
import org.openengsb.config.jbi.types.ServiceType;
import org.openengsb.config.jbi.types.StringType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Fixtures {
    public static ServiceAssemblyInfo createSAI() {
        ServiceAssemblyInfo sa = new ServiceAssemblyInfo("saname");
        sa.addServiceUnit(createSUI());
        HashMap<String, String> props = Maps.newHashMap();
        props.put("id", "thebean");
        BeanInfo bi = new BeanInfo(sa.getServiceUnits().get(0).getComponent().getBeans().get(0), props);
        sa.addBean(bi);
        return sa;
    }

    public static ServiceUnitInfo createSUI() {
        ComponentType c = new ComponentType("a", "b", "http://a.b.c", true);
        BeanType b = new BeanType("a.b.C");
        b.addProperty(new StringType("stringProperty", true, 0, ""));
        EndpointType e = new EndpointType("a");
        e.addAttribute(new ServiceType("service", false, 0, ""));
        e.addAttribute(new EndpointNameType("endpoint", false, 0, ""));
        RefType ref = new RefType("beanRef", true, 0, "");
        ref.setTheClass(b.getClazz());
        e.addAttribute(ref);
        HashMap<String, String> attrs = Maps.<String, String> newHashMap();
        attrs.put("service", "servicename");
        attrs.put("endpoint", "endpointname");
        attrs.put("beanRef", "thebean");
        EndpointInfo ei = new EndpointInfo(e, attrs);
        c.addEndpoint(e);
        c.addBean(b);
        return new ServiceUnitInfo(c, Lists.newArrayList(ei));
    }

    public static XPath newXPath() {
        MapNamespaceContext ctx = new MapNamespaceContext();
        ctx.addNamespace("ns", "http://a.b.c");
        return newXPath(ctx);
    }

    public static XPath newXPath(NamespaceContext ns) {
        XPathFactory fac = XPathFactory.newInstance();
        XPath xpath = fac.newXPath();
        xpath.setNamespaceContext(ns);
        return xpath;
    }
}
