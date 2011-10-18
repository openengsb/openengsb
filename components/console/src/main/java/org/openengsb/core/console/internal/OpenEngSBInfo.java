/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.console.internal;

import java.util.LinkedList;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.commands.info.InfoProvider;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.osgi.framework.Bundle;

@Command(scope = "openengsb", name = "info", description = "Prints out system information.")
public class OpenEngSBInfo extends OsgiCommandSupport {

    private String versionNumber;
    private String droolsVersion;

    private List<InfoProvider> infoProviders = new LinkedList<InfoProvider>();

    @Override
    protected Object doExecute() throws Exception {
        //
        // print OpenEngSB information
        //
        OutputStreamFormater.printValue("OpenEngSB Framework");
        OutputStreamFormater.printValue("OpenEngSB Framework Version", versionNumber);
        // print loaded openengsb-bundles
        Bundle[] bundles = getBundleContext().getBundles();
        Integer count = 0;
        for (Bundle b : bundles) {
            if (b.getSymbolicName().startsWith("org.openengsb.framework")) {
                count++;
            }
        }
        OutputStreamFormater.printValue("OpenEngSB Framework Bundles", count.toString());
        OutputStreamFormater.printValue("\n");

        //
        // Other infos:
        //
        OutputStreamFormater.printValue("Used libraries");
        OutputStreamFormater.printValue("Karaf Version", System.getProperty("karaf.version"));
        OutputStreamFormater.printValue("OSGi Framework",
            bundleContext.getBundle(0).getSymbolicName() + " - " + bundleContext.getBundle(0).getVersion());
        OutputStreamFormater.printValue("Drools version", droolsVersion);
        OutputStreamFormater.printValue("\n");

        return null;
    }

    public List<InfoProvider> getInfoProviders() {
        return infoProviders;
    }

    public void setInfoProviders(List<InfoProvider> infoProviders) {
        this.infoProviders = infoProviders;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setDroolsVersion(String droolsVersion) {
        this.droolsVersion = droolsVersion;
    }
}
