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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;

@Command(scope = "openengsb", name = "service", description = "Prints out the created OpenEngSB services.")
public class ServiceCommands extends OsgiCommandSupport {

    public static final String VALUE_SEPARATOR = ":";
    public static final String CONNECTOR_TYPE = "type";

    @Option(name = "-f", aliases = {"--force"}, description = "Force the command (true or false) ", required = false,
        multiValued = false)
    private String force = "false";

    @Argument(index = 0, name = "command", description = "The service command argument (CREATE, UPDATE, DELETE)",
        required = false, multiValued = false)
    private String arg = null;

    @Argument(index = 1, name = "id", required = false, multiValued = false)
    private String id = null;

    //attributes for creating a service: has the format:
    // <CONNECTOR_TYPE> VALUE_SEPARATOR <value> <field1> VALUE_SEPARATOR <value1>
    // ...without the "<" sign and any whitespace
    @Argument(index = 2, name = "attributes", required = false, multiValued = true)
    private List<String> attributes = new ArrayList<String>();

    private ServicesHelper serviceHelper;

    protected Object doExecute() throws Exception {
        try {
            ServiceCommandArguments arguments = retrieveArgumentOrDefault();
            boolean option = retrieveOption();
            switch (arguments) {
                case LIST:
                    serviceHelper.listRunningServices();
                    break;
                case CREATE:
                    serviceHelper.createService(id, option, retrieveAttributes(attributes));
                    break;
                case UPDATE:
                    // TODO: see OPENENGSB-2282
                    break;
                case DELETE:
                    serviceHelper.deleteService(id, option);
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

    private ServiceCommandArguments retrieveArgumentOrDefault() {
        if (arg == null) {
            return ServiceCommandArguments.LIST;
        }
        return ServiceCommandArguments.valueOf(arg.toUpperCase());
    }

    private boolean retrieveOption() {
        return Boolean.parseBoolean(this.force);
    }

    private Map<String, String> retrieveAttributes(List<String> attributes) {
        Map<String, String> resultMap = new HashMap<String, String>();

        for (String valueAndFields : attributes) {
            String[] valueAndField = valueAndFields.split(ServiceCommands.VALUE_SEPARATOR);
            if (valueAndField.length != 2) {
                throw new IllegalArgumentException();
            } else {
                resultMap.put(valueAndField[0], valueAndField[1]);
            }
        }
        return resultMap;
    }

    public ServicesHelper getServiceHelper() {
        return serviceHelper;
    }

    public void setServiceHelper(ServicesHelper serviceHelper) {
        this.serviceHelper = serviceHelper;
    }


}
