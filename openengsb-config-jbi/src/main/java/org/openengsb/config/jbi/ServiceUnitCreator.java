package org.openengsb.config.jbi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;

public class ServiceUnitCreator {
    public static void createServiceUnit(OutputStream os, ComponentType component, EndpointType endpoint,
            Map<String, String> map) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(os);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeXBeanXmlFile(bout, component, endpoint, map);

        zip.putNextEntry(new ZipEntry("xbean.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();

        bout = new ByteArrayOutputStream();
        writeJbiXmlFile(bout, component);

        zip.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();
        zip.close();
    }

    public static void writeXBeanXmlFile(OutputStream out, ComponentType component, EndpointType endpoint,
            Map<String, String> map) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("<beans xmlns:" + component.getNsname() + "=\"" + component.getNamespace() + "\"\n");
        writer.write("       xmlns:app=\"urn:openengsb:application\">\n");
        writer.write("  <" + component.getNsname() + ':' + endpoint.getName() + "\n");
        for (Map.Entry<String, String> e : map.entrySet()) {
            writer.write("    " + e.getKey() + "=\"" + e.getValue() + "\"\n");
        }
        writer.write("  />");
        writer.write("</beans>");
        writer.flush();
    }

    public static void writeJbiXmlFile(OutputStream out, ComponentType component) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<jbi xmlns=\"http://java.sun.com/xml/ns/jbi\" version=\"1.0\">\n");
        writer.write("  <services binding-component=\"");
        writer.write("" + component.isBindingComponent());
        writer.write("\"/>\n");
        writer.write("</jbi>\n");
        writer.flush();
    }
}
