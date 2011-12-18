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

package org.openengsb.core.console.internal.completer;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;

import static org.openengsb.core.console.internal.util.ServiceCommandArguments.CREATE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.DELETE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.LIST;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.UPDATE;

/**
 * this completer helps the user to know which input is expected
 */
public class ServiceCompleter implements Completer {

    private ServiceCommandArguments lastCommand;
    private ServicesHelper servicesHelper;
    private List<String> params;

    public ServiceCompleter(ServicesHelper helper) {
        this.servicesHelper = helper;
        this.params = createPossibilities();
    }

    @Override
    public int complete(String buffer, int cursor, List candidates) {

        StringsCompleter delegate = new StringsCompleter(createPossibilities(), false);
        if (buffer == null || delegate == null && lastCommand == null) {
            printStandardCommands(delegate);
        } else {
            try {
                ServiceCommandArguments argument = ServiceCommandArguments.valueOf(buffer.toUpperCase());
                lastCommand = argument;
                switch (argument) {
                    case LIST:
                        return delegate.complete(buffer, cursor, candidates);
                    case CREATE:
                        // TODO: see OPENENGSB-2280
                        break;
                    case UPDATE:
                        // TODO: see OPENENGSB-2282
                        break;
                    case DELETE:
                        List<String> runningServiceIds = servicesHelper.getRunningServiceIds();
                        //delegate = new StringsCompleter(runningServiceIds);
                        return delegate.complete(buffer, cursor, candidates);
                    default:
                        break;
                }
            } catch (IllegalArgumentException ex) {
                if (lastCommand != null) {
                    List<String> runningServiceIds = servicesHelper.getRunningServiceIds();
                    delegate = new StringsCompleter(runningServiceIds);
                    lastCommand = null;
                    delegate.complete(buffer, cursor, candidates);
                } else {
                    printStandardCommands(delegate);
                }
            }
        }
        servicesHelper.getKeyboard();
        return delegate.complete(buffer, cursor, params);
    }

    private List<String> createPossibilities() {
        List<String> runningServiceIds = servicesHelper.getRunningServiceIds();
        List<String> possabilities = new ArrayList<String>();
        List<ServiceCommandArguments> arguments = new ArrayList<ServiceCommandArguments>();
        arguments.add(LIST);
        arguments.add(DELETE);
        arguments.add(UPDATE);
        arguments.add(CREATE);
        for (ServiceCommandArguments argument : arguments) {
            switch (argument) {
                case CREATE:
                    break;
                case UPDATE:
                    for (String serviceID : runningServiceIds) {
                        possabilities.add(String.format("%s %s", UPDATE.toString(), serviceID));
                    }
                    break;
                case DELETE:
                    for (String serviceID : runningServiceIds) {
                        possabilities.add(String.format("%s %s", DELETE.toString(), serviceID));
                    }
                    break;
                default:
                    possabilities.add(String.format("%s", LIST.toString()));
                    break;
            }
        }

        return possabilities;
    }

    private void printStandardCommands(StringsCompleter delegate) {
        delegate.getStrings().add(LIST.toString().toLowerCase());
        delegate.getStrings().add(CREATE.toString().toLowerCase());
        delegate.getStrings().add(UPDATE.toString().toLowerCase());
        delegate.getStrings().add(DELETE.toString().toLowerCase());
    }

    public List<String> getParams() {
        return params;
    }
}
