package org.openengsb.core.console.internal.util;

import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.util.Comparators;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.util.OutputStreamFormater;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class ServicesHelper {


    private DefaultOsgiUtilsService osgiUtilsService;


    public ServicesHelper(BundleContext bundleContext) {
        this.osgiUtilsService = new DefaultOsgiUtilsService();
        this.osgiUtilsService.setBundleContext(bundleContext);
    }

    /**
     * this method prints out services which can be created
     */
    public void listCreatableServices() {
        List<DomainProvider> serviceList = osgiUtilsService.listServices(DomainProvider.class);
        Collections.sort(serviceList, Comparators.forDomainProvider());

        OutputStreamFormater.printValue("Services");
        Locale defaultLocale = Locale.getDefault();

        for (DomainProvider dp : serviceList) {
            OutputStreamFormater
                    .printValue(dp.getName().getString(defaultLocale), dp.getDescription().getString(defaultLocale));
            printConnectorProvidersByDomain(dp.getId());
        }
    }

    /**
     * prints out all available connectors for a given domain provider
     */
    private void printConnectorProvidersByDomain(String domainType) {
        Locale defaultLocale = Locale.getDefault();
        List<ConnectorProvider> connectorProviders = osgiUtilsService.listServices(
                ConnectorProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domainType));
        if (connectorProviders == null || connectorProviders.size() == 0) {
            OutputStreamFormater.printValue(String.format("No connectors found for domain: %s", domainType));
        } else {
            for (ConnectorProvider connectorProvider : connectorProviders) {
                String serviceId = connectorProvider.getId();
                String serviceName = connectorProvider.getDescriptor().getName().getString(defaultLocale);
                String serviceDescription = connectorProvider.getDescriptor().getDescription().getString(defaultLocale);
                if (serviceId != null && serviceName != null && serviceDescription != null) {
                    OutputStreamFormater.printValuesWithPrefix(serviceId, serviceName, serviceDescription);
                }
            }
        }
    }

    public void createService(InputStream keyboard) {
        OutputStreamFormater.printValue("Enter Service ID");

    }
}
