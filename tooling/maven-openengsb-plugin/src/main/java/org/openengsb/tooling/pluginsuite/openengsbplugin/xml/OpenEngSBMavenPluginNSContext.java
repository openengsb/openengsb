package org.openengsb.tooling.pluginsuite.openengsbplugin.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class OpenEngSBMavenPluginNSContext implements NamespaceContext {

    private HashMap<String, String> prefixToURI = new HashMap<String, String>();
    private HashMap<String, String> URIToPrefix = new HashMap<String, String>();

    public OpenEngSBMavenPluginNSContext() {
        buildNamingContext();
    }

    private void buildNamingContext() {
        String[][] data =
            new String[][]{ { "lc", "http://www.openengsb.org/tooling/maven-openengsb-plugin/licenseCheckMojo" },
                { "pom", "http://maven.apache.org/POM/4.0.0" } };

        for (String[] strArr : data) {
            prefixToURI.put(strArr[0], strArr[1]);
            URIToPrefix.put(strArr[1], strArr[0]);
        }
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return prefixToURI.get(prefix);
    }

    @Override
    public String getPrefix(String uri) {
        return URIToPrefix.get(uri);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(String arg0) {
        HashSet<String> h = new HashSet<String>();
        h.addAll(prefixToURI.keySet());
        return h.iterator();
    }

}
