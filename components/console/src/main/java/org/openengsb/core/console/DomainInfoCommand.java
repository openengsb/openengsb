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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.fusesource.jansi.Ansi;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.util.Comparators;
import org.osgi.framework.ServiceReference;

/**
 * this class should only be used INSIDE the console package and NOT OUTSIDE because in a later
 * release it will be moved to the internal package
 */
@Deprecated
@Command(scope = "openengsb", name = "domains", description = "Prints out the available OpenEngSB domains.")
public class DomainInfoCommand extends OsgiCommandSupport {

    protected Object doExecute() throws Exception {
        int maxNameLen = 25;
        ServiceReference sr = getBundleContext().getServiceReference("org.openengsb.core.api.OsgiUtilsService");
        OsgiUtilsService service = getService(OsgiUtilsService.class, sr);
        List<DomainProvider> serviceList = service.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());
        System.out.println("Services");
        for (DomainProvider dp : serviceList) {
            printValue(dp.getName().getString(Locale.getDefault()), maxNameLen,
                dp.getDescription().getString(Locale.getDefault()));
        }

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

}
