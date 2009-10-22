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
package org.openengsb.config.jbi.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.openengsb.config.jbi.ComponentsRetriever;
import org.openengsb.config.jbi.ParseException;
import org.openengsb.config.jbi.component.ComponentDescriptor;
import org.openengsb.config.jbi.component.ComponentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class DirectoryComponentsRetriever implements ComponentsRetriever {
    private static Logger log = LoggerFactory.getLogger(DirectoryComponentsRetriever.class);
    private String componentsDirectory;
    private List<ComponentDescriptor> components;

    @Override
    public List<ComponentDescriptor> lookupComponents() {
        if (components != null) {
            return components;
        }
        components = new ArrayList<ComponentDescriptor>();
        File path = new File(componentsDirectory);

        if (!path.exists() || !path.isDirectory()) {
            return components;
        }

        for (File file : path.listFiles()) {
            if (!file.getAbsolutePath().endsWith("-installer.zip")) {
                continue;
            }
            try {
                components.add(readInstallerZip(file));
                log.info("Parsed jbi component file " + file);
            } catch (Throwable e) {
                log.warn("Error while reading jbi component file", e);
            }
        }

        return components;
    }

    private ComponentDescriptor readInstallerZip(File file) throws ZipException, IOException, ParseException {
        ComponentParser parser = new ComponentParser();
        String jarname = "lib/" + file.getName().substring(0, file.getName().indexOf("-installer.zip")) + ".jar";
        ZipFile zip = new ZipFile(file);

        ZipEntry jbiEntry = zip.getEntry("META-INF/jbi.xml");
        ComponentDescriptor jbiDescriptor = parser.parseJbi(new InputSource(zip.getInputStream(jbiEntry)));

        ZipEntry jarEntry = zip.getEntry(jarname);
        ZipInputStream jar = new ZipInputStream(zip.getInputStream(jarEntry));

        ZipEntry entry = null;
        while ((entry = jar.getNextEntry()) != null) {
            if (entry.getName().endsWith(".xsd")) {
                break;
            }
            entry = null;
        }

        if (entry == null) {
            throw new ParseException("jbi component schema not found");
        }

        ComponentDescriptor schemaDescriptor = parser.parseSchema(new InputSource(jar));

        return new ComponentDescriptor(jbiDescriptor.getType(), jbiDescriptor.getName(),
                jbiDescriptor.getDescription(), schemaDescriptor.getTargetNamespace(), schemaDescriptor.getEndpoints());
    }

    public void setComponentsDirectory(String componentsDirectory) {
        this.componentsDirectory = componentsDirectory;
    }
}
