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

package org.openengsb.core.console.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.openengsb.core.common.util.SecurityUtils;
import org.openengsb.core.console.internal.ServiceCommands;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class ServicesHelper {


    private DefaultOsgiUtilsService osgiUtilsService;
    private ConnectorManager serviceManager;
    private InputStream keyboard;
    private BundleContext bundleContext;


    public ServicesHelper() {
    }

    public void init() {
        this.osgiUtilsService = new DefaultOsgiUtilsService();
        this.osgiUtilsService.setBundleContext(bundleContext);
        serviceManager = osgiUtilsService.getService(ConnectorManager.class);
        CommandProcessor commandProcessor = osgiUtilsService
            .getService(org.apache.felix.service.command.CommandProcessor.class);
        CommandSession commandSession = commandProcessor.createSession(System.in, System.err, System.out);
        keyboard = commandSession.getKeyboard();
    }

    /**
     * prints out all available services and their alive state
     */
    public void listRunningServices() {
        try {
            final List<String> formatedOutput =
                SecurityUtils.executeWithSystemPermissions(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        List<String> tmp = new ArrayList<String>();

                        List<ServiceReference> listServiceReferences =
                            osgiUtilsService.listServiceReferences(Domain.class);
                        for (ServiceReference ref : listServiceReferences) {
                            Domain service = osgiUtilsService.getService(Domain.class, ref);
                            tmp.add(String.format("%s %s", ref.getProperty("id"), service.getAliveState().toString()));
                        }
                        return tmp;
                    }
                });
            for (String s : formatedOutput) {
                OutputStreamFormater.printValue(s);
            }
        } catch (ExecutionException ex) {
            ex.printStackTrace();
            System.err.println("Could not get services");
        }
    }

    public List<DomainProvider> getDomainProvider() {
        List<DomainProvider> domainProvider = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(domainProvider, Comparators.forDomainProvider());
        return domainProvider;
    }

    public List<String> getDomainProviderNames() {
        List<String> names = new ArrayList<String>();
        List<DomainProvider> domainProvider = getDomainProvider();
        for (DomainProvider provider : domainProvider) {
            String name = provider.getName().getString(Locale.getDefault());
            names.add(name);
        }
        return names;
    }

    /**
     * delete a service identified by its id, if force is true, the user does not have to confirm
     */
    public void deleteService(final String id, boolean force) {
        try {
            int input = 'Y';
            if (!force) {
                OutputStreamFormater
                    .printValue(String.format("Do you really want to delete the connector: %s (Y/n): ", id));
                input = keyboard.read();
            }
            if ('n' != (char) input && 'N' != (char) input) {
                SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        ConnectorId fullId = ConnectorId.fromFullId(id);
                        serviceManager.delete(fullId);
                        return null;
                    }
                });
                OutputStreamFormater.printValue(String.format("Service: %s successfully deleted", id));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.err.println("Could not delete service");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unexpected Error");
        }
    }

    /**
     * returns a list of all service ids
     */
    public List<String> getRunningServiceIds() {
        List<ServiceReference> serviceReferences = osgiUtilsService.listServiceReferences(Domain.class);
        List<String> result = new ArrayList<String>();
        for (ServiceReference ref : serviceReferences) {
            result.add((String) ref.getProperty("id"));
        }
        return result;
    }

    /**
     * crate a service for the given domain, if force is true, input is not verified
     */
    public void createService(String domainProviderName, boolean force, Map<String, String> attributes) {
        // check if a domain has been chosen
        if (domainProviderName == null || domainProviderName.isEmpty()) {
            domainProviderName = selectDomainProvider();
        }

        // get domain provider Id
        String domainProviderId = "";
        List<DomainProvider> domainProvider = getDomainProvider();
        for (DomainProvider provider : domainProvider) {
            if (provider.getName().getString(Locale.getDefault()).equals(domainProviderName)) {
                domainProviderId = provider.getId();
            }
        }
        // get the connector which should be created
        ConnectorProvider connectorProvider =
            getConnectorToCreate(domainProviderId, attributes.get(ServiceCommands.CONNECTOR_TYPE));

        String id;
        if (attributes.isEmpty() || !attributes.containsKey("id")) {
            OutputStreamFormater.printValue("Please enter an ID");
            id = readUserInput();
        } else {
            id = attributes.get("id");
        }

        ServiceDescriptor descriptor = connectorProvider.getDescriptor();
        OutputStreamFormater.printValue(String.format("Please enter the attributes for %s, keep empty for default",
            descriptor.getName().getString(Locale.getDefault())));

        //get attributes for connector
        Map<String, String> attributeMap = getConnectorAttributes(descriptor.getAttributes(), attributes);
        Map<String, Object> properties = new HashMap<String, Object>();

        ConnectorDescription connectorDescription = new ConnectorDescription(attributeMap, properties);
        ConnectorId idProvider = new ConnectorId(domainProviderId, connectorProvider.getId(), id);
        if (force) {
            serviceManager.forceCreate(idProvider, connectorDescription);
            OutputStreamFormater.printValue("Connector successfully created");
        } else {
            OutputStreamFormater.printValue("Do you want to create the connector with the following attributes:", "");
            OutputStreamFormater.printValue("Connector ID", id);
            for (String key : attributeMap.keySet()) {
                OutputStreamFormater.printValue(key, attributeMap.get(key));
            }
            OutputStreamFormater.printValue("Create connector: (Y/n)");
            if (!readUserInput().equalsIgnoreCase("n")) {
                try {
                    serviceManager.create(idProvider, connectorDescription);
                    OutputStreamFormater.printValue("Connector successfully created");
                } catch (ConnectorValidationFailedException e) {
                    e.printStackTrace();
                    OutputStreamFormater.printValue("Connector validation failed, creation aborted");
                }
            } else {
                OutputStreamFormater.printValue("Creation aborted");
            }
        }
    }

    private String selectDomainProvider() {
        String selectedProvider = "";
        List<String> domainProviderNames = getDomainProviderNames();
        for (int i = 0; i < domainProviderNames.size(); i++) {
            String provider = domainProviderNames.get(i);
            OutputStreamFormater.printTabbedValues(
                9, String.format("[%s]", i), String.format("%s", provider));
        }
        String s = readUserInput();
        int pos;
        try {
            pos = Integer.parseInt(s);
            selectedProvider = domainProviderNames.get(pos);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Invalid Input: %s", s));
        }
        return selectedProvider;
    }

    protected Map<String, String> getConnectorAttributes(List<AttributeDefinition> attributeDefinitions,
                                                         Map<String, String> attributesFromInput) {
        HashMap<String, String> attributeMap = new HashMap<String, String>();
        for (AttributeDefinition attributeDefinition : attributeDefinitions) {
            String fieldName = attributeDefinition.getName().getString(Locale.getDefault());
            String description = attributeDefinition.getDescription().getString(Locale.getDefault());
            String defaultValue = attributeDefinition.getDefaultValue().getString(Locale.getDefault());

            String userValue;
            if (attributesFromInput.containsKey(attributeDefinition.getId())) {
                userValue = attributesFromInput.get(attributeDefinition.getId());
            } else {
                OutputStreamFormater.printTabbedValues(9, String.format("\n%s", fieldName), String.format("%s (%s)",
                    description, defaultValue));
                if (!attributeDefinition.getOptions().isEmpty()) {
                    userValue = letUserChooseFromOption(attributeDefinition.getOptions());
                } else {
                    userValue = readUserInput();
                }
                if ("".equals(userValue) || "\n".equals(userValue)) {
                    userValue = defaultValue;
                }
            }
            attributeMap.put(fieldName, userValue);
        }
        return attributeMap;
    }

    private String letUserChooseFromOption(List<AttributeDefinition.Option> options) {
        for (int i = 0; i < options.size(); i++) {
            AttributeDefinition.Option option = options.get(i);
            OutputStreamFormater
                .printTabbedValues(9, String.format("[%s]", i), String.format("%s (%s)", option.getLabel().getString
                    (Locale.getDefault()), option.getValue()));
        }
        String s = readUserInput();
        int pos;
        try {
            pos = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Invalid input %s", s));
        }
        return options.get(pos).getValue();
    }

    private ConnectorProvider getConnectorToCreate(String domainProviderId, String connector) {
        List<ConnectorProvider> connectorProviders = osgiUtilsService.listServices(ConnectorProvider.class,
            String.format("(%s=%s)", Constants.DOMAIN_KEY, domainProviderId));

        for (ConnectorProvider connectorProvider : connectorProviders) {
            if (connector != null && connector.equals(connectorProvider.getId())) {
                return connectorProvider;
            }
        }

        OutputStreamFormater.printValue("Please select the connector you want to create: ");
        Collections.sort(connectorProviders, Comparators.forConnectorProvider());
        for (int i = 0; i < connectorProviders.size(); i++) {
            ConnectorProvider connectorProvider = connectorProviders.get(i);
            ServiceDescriptor descriptor = connectorProvider.getDescriptor();
            OutputStreamFormater
                .printTabbedValues(9, String.format("[%s] %s", i, descriptor.getName().getString(Locale
                    .getDefault())), descriptor.getDescription().getString(Locale.getDefault()));
        }
        String positionString = readUserInput();
        int pos;
        try {
            pos = Integer.parseInt(positionString);
        } catch (NumberFormatException e) {
            System.err.println("Invalid Input");
            return null;
        }
        return connectorProviders.get(pos);
    }

    private String readUserInput() {
        String positionString = "";
        try {
            int read = keyboard.read();
            while (read != '\n') {
                if (read == 127) { // backspace
                    int lastPos = positionString.length() - 1;
                    positionString = positionString.substring(0, lastPos >= 0 ? lastPos : 0);
                    System.out.println("\n" + positionString);
                    System.out.flush();
                } else {
                    char read1 = (char) read;
                    System.out.print(read1);
                    System.out.flush();
                    positionString += read1;
                }
                read = keyboard.read();
            }
            OutputStreamFormater.printValue("\n");
            System.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return positionString;
    }


    public void setOsgiUtilsService(DefaultOsgiUtilsService osgiUtilsService) {
        this.osgiUtilsService = osgiUtilsService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
