package org.openengsb.config.jbi.test.unit;

import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.openengsb.config.jbi.EndpointInfo;
import org.openengsb.config.jbi.ServiceAssemblyInfo;
import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointNameType;
import org.openengsb.config.jbi.types.EndpointType;
import org.openengsb.config.jbi.types.ServiceType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Fixtures {
    public static ServiceAssemblyInfo createSAI() {
        ServiceAssemblyInfo sa = new ServiceAssemblyInfo("saname");
        sa.addServiceUnit(createSUI());
        return sa;
    }

    public static ServiceUnitInfo createSUI() {
        ComponentType c = new ComponentType("a", "b", "http://a.b.c", true);
        EndpointType e = new EndpointType("a");
        e.addAttribute(new ServiceType("service", false, 0, ""));
        e.addAttribute(new EndpointNameType("endpoint", false, 0, ""));
        HashMap<String, String> values = Maps.<String, String> newHashMap();
        values.put("service", "servicename");
        values.put("endpoint", "endpointname");
        EndpointInfo ei = new EndpointInfo(e, values);
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
