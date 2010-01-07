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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ServiceAssemblyCreator {
    public static void createServiceAssembly(OutputStream os, ServiceAssemblyInfo sa) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(os);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        writeJbiXmlFile(bout, sa);

        zip.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        zip.write(bout.toByteArray());
        zip.closeEntry();

        for (ServiceUnitInfo su : sa.getServiceUnits()) {
            bout = new ByteArrayOutputStream();
            ServiceUnitCreator.createServiceUnit(bout, su);
            zip.putNextEntry(new ZipEntry(su.getIdentifier()+".zip"));
            zip.write(bout.toByteArray());
            zip.closeEntry();
        }

        zip.close();
    }

    public static void writeJbiXmlFile(OutputStream os, ServiceAssemblyInfo sa) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<jbi xmlns=\"http://java.sun.com/xml/ns/jbi\" version=\"1.0\">\n");
        writer.write("  <service-assembly>\n");
        writeIdentification(writer, "    ", sa.getName(), "");
        for (ServiceUnitInfo su : sa.getServiceUnits()) {
            writeServiceUnit(writer, su);
        }
        writer.write("  </service-assembly>\n");
        writer.write("</jbi>");
        writer.flush();
    }

    private static void writeServiceUnit(OutputStreamWriter writer, ServiceUnitInfo su) throws IOException {
        writer.write("    <service-unit>\n");
        writeIdentification(writer, "      ", su.getIdentifier(), "");
        writer.write("      <target>\n");
        writer.write("        <artifacts-zip>" + su.getIdentifier() + ".zip</artifacts-zip>\n");
        writer.write("        <component-name>" + su.getComponent().getName() + "</component-name>");
        writer.write("      </target>\n");
        writer.write("    </service-unit>\n");
    }

    private static void writeIdentification(OutputStreamWriter writer, String indent, String name, String description) throws IOException {
        writer.write(indent + "<identification>\n");
        writer.write(indent + "  <name>" + name + "</name>\n");
        writer.write(indent + "  <description>" + description + "</description>\n");
        writer.write(indent + "</identification>\n");
    }
}
