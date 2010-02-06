/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools.helper;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.drools.Domain;
import org.openengsb.drools.DomainConfiguration;

public class DomainConfigurationImpl implements DomainConfiguration {
    private Map<Integer, DomainInfo> domains;

    private ContextHelper contextHelper;

    public DomainConfigurationImpl(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
        this.domains = new HashMap<Integer, DomainInfo>();
    }

    public void addDomain(Domain domain, String name) {
        this.domains.put(System.identityHashCode(domain), new DomainInfo(name, null));
    }

    public QName getFullServiceName(Domain domain) {
        DomainInfo domainInfo = domains.get(System.identityHashCode(domain));
        String domainName = domainInfo.name;
        String connectorName = domainInfo.connectorName;

        String path = domainName;
        if (connectorName != null) {
            path += "/" + connectorName;
        }

        String namespaceURI = contextHelper.getValue(path + "/namespace");
        String serviceName = contextHelper.getValue(path + "/servicename");
        return new QName(namespaceURI, serviceName);
    }

    @Override
    public void setToConnector(Domain domain, String connectorName) {
        DomainInfo oldInfo = this.domains.get(System.identityHashCode(domain));
        this.domains.put(System.identityHashCode(domain), new DomainInfo(oldInfo.name, connectorName));
    }

    @Override
    public void setToDomain(Domain domain) {
        DomainInfo oldInfo = this.domains.get(System.identityHashCode(domain));
        this.domains.put(System.identityHashCode(domain), new DomainInfo(oldInfo.name, null));
    }

    @Override
    public String getNamespace(Domain domain) {
        DomainInfo domainInfo = domains.get(System.identityHashCode(domain));
        String domainName = domainInfo.name;
        return contextHelper.getValue(domainName + "/namespace");
    }

    @Override
    public String getServiceName(Domain domain) {
        DomainInfo domainInfo = domains.get(System.identityHashCode(domain));
        String domainName = domainInfo.name;
        return contextHelper.getValue(domainName + "/servicename");
    }

    @Override
    public String getEventServiceName(Domain domain) {
        DomainInfo domainInfo = domains.get(System.identityHashCode(domain));
        String domainName = domainInfo.name;
        return contextHelper.getValue(domainName + "/event/servicename");
    }

    private class DomainInfo {
        private final String name;
        private final String connectorName;

        public DomainInfo(String name, String connectorName) {
            this.name = name;
            this.connectorName = connectorName;
        }
    }

    public void setContextHelper(ContextHelper contextHelper) {
        this.contextHelper = contextHelper;
    }

}
