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

package org.openengsb.core.console;

import java.util.LinkedList;
import java.util.List;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.commands.info.InfoProvider;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.fusesource.jansi.Ansi;
import org.osgi.framework.Bundle;

/**
 * this class should only be used INSIDE the console package and NOT OUTSIDE because in a later release it will be moved
 * to the internal package
 *
 * @deprecated This class was mistakely took into the public packages and will be removed without any replacement in the
 *             upcomming openengsb-framework 3.0.0 release. If you need any specific functionality from it please
 *             contact openengsb-user@googlegroups.com and let's check where we can move this functionality. In
 *             addidition this class had been depricated because it is a duplicate of the {@link OpenEngSBInfo} command.
 *
 */
@Deprecated
@Command(scope = "openengsb", name = "info", description = "Prints out system information.")
public class ServicesInfo extends OsgiCommandSupport {

    private String versionNumber;
    private String nameAdjective;
    private String nameNoun;
    private String droolsVersion;

    private List<InfoProvider> infoProviders = new LinkedList<InfoProvider>();

    @Override
    protected Object doExecute() throws Exception {
        int maxNameLen;

        maxNameLen = 25;
        //
        // print OpenEngSB information
        //
        System.out.println("OpenEngSB");
        printValue("OpenEngSB version", maxNameLen, versionNumber);
        printValue("OpenEngSB codename", maxNameLen, String.format("%s %s", nameAdjective, nameNoun));
        // print loaded openengsb-bundles
        Bundle[] bundles = getBundleContext().getBundles();
        Integer count = 0;
        for (Bundle b : bundles) {
            if (b.getSymbolicName().startsWith("org.openengsb.")) {
                count++;
            }
        }
        printValue("OpenEngSB bundles", maxNameLen, count.toString());
        System.out.println();

        //
        // Other infos:
        //
        System.out.println("Used libraries");
        printValue("Karaf version", maxNameLen, System.getProperty("karaf.version"));
        printValue("OSGi Framework", maxNameLen,
            bundleContext.getBundle(0).getSymbolicName() + " - " + bundleContext.getBundle(0).getVersion());
        printValue("Drools version", maxNameLen, droolsVersion);
        System.out.println();

        return null;
    }

    private void printValue(String name, int pad, String value) {
        System.out.println(Ansi.ansi().a("  ").a(Ansi.Attribute.INTENSITY_BOLD).a(name).a(spaces(pad - name.length()))
            .a(Ansi.Attribute.RESET).a("   ").a(value).toString());
    }

    private String spaces(int nb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(' ');
        }
        return sb.toString();
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

    public void setNameAdjective(String nameAdjective) {
        this.nameAdjective = nameAdjective;
    }

    public void setNameNoun(String nameNoun) {
        this.nameNoun = nameNoun;
    }

    public void setDroolsVersion(String droolsVersion) {
        this.droolsVersion = droolsVersion;
    }
}
