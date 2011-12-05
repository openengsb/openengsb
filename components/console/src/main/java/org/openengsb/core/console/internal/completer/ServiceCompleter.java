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

import static org.openengsb.core.console.internal.util.ServiceCommandArguments.CREATE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.DELETE;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.LIST;
import static org.openengsb.core.console.internal.util.ServiceCommandArguments.UPDATE;

import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;

/**
 * this completer helps the user to know which input is expected
 */
public class ServiceCompleter implements Completer {

    private ServicesHelper servicesHelper;

    public ServiceCompleter(ServicesHelper helper) {
        this.servicesHelper = helper;
    }

    /**
     * @param buffer     it's the beginning string typed by the user
     * @param cursor     it's the position of the cursor
     * @param candidates the list of completions proposed to the user
     */
    public int complete(String buffer, int cursor, List<String> candidates) {

        StringsCompleter delegate = new StringsCompleter();
        if (buffer == null) {
            delegate = printStandardCommands(delegate);
        } else {
            try {
                ServiceCommandArguments arguments = ServiceCommandArguments.valueOf(buffer.toUpperCase());
                switch (arguments) {
                    case LIST:
                        return delegate.complete(buffer, cursor, candidates);
                    case CREATE:
                        // TODO: see OPENENGSB-2280
                        break;
                    case UPDATE:
                        // TODO: see OPENENGSB-2282
                        break;
                    case DELETE:
                        //TODO : see OPENENGSB-2281
                        break;
                    default:
                        break;
                }
            } catch (IllegalArgumentException ex) {
                delegate = printStandardCommands(delegate);
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }

    private StringsCompleter printStandardCommands(StringsCompleter delegate) {
        delegate.getStrings().add(LIST.toString().toLowerCase());
        delegate.getStrings().add(CREATE.toString().toLowerCase());
        delegate.getStrings().add(UPDATE.toString().toLowerCase());
        delegate.getStrings().add(DELETE.toString().toLowerCase());
        return delegate;
    }

}
