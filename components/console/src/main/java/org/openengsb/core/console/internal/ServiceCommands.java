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

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Command(scope = "openengsb", name = "service", description = "Prints out the created OpenEngSB services.")
public class ServiceCommands extends OsgiCommandSupport {

    @Argument(index = 0, name = "command", description = "The service command argument (CREATE, UPDATE, DELETE)",
            required = true, multiValued = false)
    String arg = null;

    @Argument(index = 1, name = "serviceId", description = "The domain id to be instantiated", required = false,
            multiValued = false)
    String serviceId = null;

    @Argument(index = 2, name = "serviceAttributes", description = "The service attributes (alphabetic order)",
            required = false, multiValued = true)
    List<String> attributes = null;

    private OsgiUtilsService osgiUtilsService;
    private ServicesHelper serviceHelper;

    protected Object doExecute() throws Exception {
        ServiceReference sr = getBundleContext().getServiceReference("org.openengsb.core.api.OsgiUtilsService");
        ServiceReference commandSessionReference =
                getBundleContext().getServiceReference("org.apache.felix.service.command.CommandProcessor");

        osgiUtilsService = getService(OsgiUtilsService.class, sr);

        CommandProcessor commandProcessor = getService(CommandProcessor.class, commandSessionReference);
        CommandSession commandSession = commandProcessor.createSession(System.in, System.err, System.out);
        InputStream keyboard = commandSession.getKeyboard();

        try {
            ServiceCommandArguments arguments = ServiceCommandArguments.valueOf(arg.toUpperCase());
            switch (arguments) {
                case LIST:
                    serviceHelper.listCreatableServices();
                    break;
                case CREATE:
                    //TODO implement one line create
                    serviceHelper.createService(keyboard);
                    break;
                case UPDATE:
                    //TODO
                    break;
                case DELETE:
                    //TODO
                    break;
                default:
                    System.err.println("Invalid Argument");
                    break;
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid Argument");
        }
        return null;
    }

    private void createService(List<DomainProvider> serviceList, InputStream keyboard) {
        boolean readInput = true;
        try {
            while (readInput) {
                char read = (char) keyboard.read();

            }
        } catch (IOException e) {
            //TODO error handling
            e.printStackTrace();
        }

    }

    public ServicesHelper getServiceHelper() {
        return serviceHelper;
    }

    public void setServiceHelper(ServicesHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }
}
