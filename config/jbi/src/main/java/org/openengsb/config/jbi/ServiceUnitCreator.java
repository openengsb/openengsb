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
package org.openengsb.config.jbi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openengsb.config.jbi.types.ComponentType;

public class ServiceUnitCreator {
    public static void createServiceUnit(OutputStream os, ServiceUnitInfo su) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(os);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeXBeanXmlFile(bout, su);

        zip.putNextEntry(new ZipEntry("xbean.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();

        bout = new ByteArrayOutputStream();
        writeJbiXmlFile(bout, su.getComponent());

        zip.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();
        zip.close();
    }

    public static void writeXBeanXmlFile(OutputStream out, ServiceUnitInfo su) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("<beans xmlns:" + su.getComponent().getNsname() + "=\"" + su.getComponent().getNamespace() + "\"\n");
        writer.write("       xmlns:app=\"urn:openengsb:application\">\n");
        writer.write("  <" + su.getComponent().getNsname() + ':' + su.getEndpoint().getName() + "\n");
        for (Map.Entry<String, String> e : su.getMap().entrySet()) {
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
