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

import java.util.Collection;
import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.openengsb.core.console.internal.util.ServiceCommandArguments;
import org.openengsb.core.console.internal.util.ServicesHelper;

/**
 * this completer helps the user to know which input is expected
 */
public class ServiceCompleter implements Completer {

    private ServiceCommandArguments lastCommand;
    private ServicesHelper servicesHelper;

    public ServiceCompleter(ServicesHelper helper) {
        this.servicesHelper = helper;
    }

    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {

        StringsCompleter delegate = new StringsCompleter();
        try {
            if (buffer == null) {
                if (lastCommand == null) {
                    addStandardArguments(delegate);
                } else {
                    addCandidates(delegate, lastCommand);
                }
            } else {
                lastCommand = ServiceCommandArguments.valueOf(buffer.toUpperCase());
                switch (lastCommand) {
                    case LIST:
                        return delegate.complete(buffer, cursor, candidates);
                    case CREATE:
                        lastCommand = CREATE;
                        addDomains(candidates);
                        return new StringsCompleter().complete(buffer, cursor, candidates);
                    case UPDATE:
                        // TODO: see OPENENGSB-2282
                        break;
                    case DELETE:
                        lastCommand = DELETE;
                        addServiceIds(candidates);
                        return new StringsCompleter().complete(buffer, cursor, candidates);
                    default:
                        break;
                }
            }
        } catch (IllegalArgumentException ex) {
            if (lastCommand != null) {
                addCandidates(delegate, lastCommand);
                lastCommand = null;
            } else {
                addStandardArguments(delegate);
            }
        }
        return delegate.complete(buffer, cursor, candidates);
    }

    private void addCandidates(StringsCompleter delegate, ServiceCommandArguments lastCommand) {
        switch (lastCommand) {
            case DELETE:
                addServiceIds(delegate.getStrings());
                break;
            case CREATE:
            case UPDATE:
                addDomains(delegate.getStrings());
                break;
        }
    }

    private void addStandardArguments(StringsCompleter delegate) {
        delegate.getStrings().add(LIST.toString().toLowerCase());
        delegate.getStrings().add(CREATE.toString().toLowerCase());
        delegate.getStrings().add(UPDATE.toString().toLowerCase());
        delegate.getStrings().add(DELETE.toString().toLowerCase());
    }

    private void addDomains(Collection<String> strings) {
        strings.addAll(servicesHelper.getDomainProviderNames());
    }


    private void addServiceIds(Collection<String> strings) {
            strings.addAll(servicesHelper.getRunningServiceIds());
    }
}
