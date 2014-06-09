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

package org.openengsb.connector.userprojects.ldap.internal;

import java.util.Map;

import org.openengsb.connector.userprojects.ldap.internal.ldap.ServerConfig;
import org.openengsb.core.api.Connector;
import org.openengsb.core.common.AbstractConnectorInstanceFactory;
import org.openengsb.infrastructure.ldap.LdapDao;

public class UserProjectsLdapServiceInstanceFactory extends
        AbstractConnectorInstanceFactory<UserProjectsLdapServiceImpl> {

    private LdapDao ldapDao;
    private SynchronizationService synchronizationService;
    
    public UserProjectsLdapServiceInstanceFactory() {
    }

    public void setSynchronizationService(SynchronizationService service) {
        synchronizationService = service;
    }

    @Override
    public Connector createNewInstance(String id) {
        return new UserProjectsLdapServiceImpl();
    }

    @Override
    public UserProjectsLdapServiceImpl doApplyAttributes(UserProjectsLdapServiceImpl instance,
        Map<String, String> attributes) {
        if (attributes.containsKey("ldapServer.host")) {
            ServerConfig.host = attributes.get("ldapServer.host");
        }
        if (attributes.containsKey("ldapServer.port")) {
            ServerConfig.port = Integer.valueOf(attributes.get("ldapServer.port"));
        }
        if (attributes.containsKey("ldapServer.userDn")) {
            ServerConfig.userDn = attributes.get("ldapServer.userDn");
        }
        if (attributes.containsKey("ldapServer.credentials")) {
            ServerConfig.credentials = attributes.get("ldapServer.credentials");
        }
        if (attributes.containsKey("ldapServer.multipleValueSeparator")) {
            ServerConfig.multipleValueSeparator = attributes.get("ldapServer.multipleValueSeparator");
        }

        setupLdapDao();
        instance.setLdapDao(ldapDao);
        synchronizationService.syncFromLdapServerToOpenEngSB(ldapDao);

        return instance;
    }
    
    private void setupLdapDao() {
        ldapDao = new LdapDao(ServerConfig.host, ServerConfig.port);
        ldapDao.connect(ServerConfig.userDn, ServerConfig.credentials);
    }

    public void destroy() {
        ldapDao.disconnect();
    }
}
