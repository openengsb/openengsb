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

package org.openengsb.core.common;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.osgi.framework.Filter;

/**
 * Static helper methods providing proxies which access the OSGi service registry for each request to retrieve the
 * correct service implementation. Please keep in mind that each service implementation can change any moment and be
 * either null and/or replaced by a different service.
 */
public final class OpenEngSBCoreServices {

    private static OsgiUtilsService serviceUtils;

    /**
     * This class should not be created
     */
    private OpenEngSBCoreServices() {
    }

    /**
     * Wiring is one of the core concepts in the OpenEngSB. The service retrieved by this method is used to get the
     * endpoints which can be reached within the OpenEngSB.
     */
    public static WiringService getWiringService() throws OsgiServiceNotAvailableException {
        return serviceUtils.getOsgiServiceProxy(WiringService.class);
    }

    /**
     * OpenEngSB Core persistence service specialized to retrieve and store ConfigItem. Those are basically simple
     * persistence items which can be stored at various endpoints like file, the "regular" persistence service or
     * anywhere else. The type to request the config persistence service is stored directly at the Configuration objects
     * in a string constant such as RuleConfiguration#TYPE_ID or ContextConfiguration#TYPE_ID
     */
    public static ConfigPersistenceService getConfigPersistenceService(String type)
        throws OsgiServiceNotAvailableException {
        Filter configPersistenceFilter;
        configPersistenceFilter = serviceUtils.makeFilter(ConfigPersistenceService.class,
                String.format("(%s=%s)", Constants.CONFIGURATION_ID, type));
        return serviceUtils.getOsgiServiceProxy(configPersistenceFilter, ConfigPersistenceService.class);
    }

    /**
     * Returns the {@link OsgiUtilsService} from the OSGi registry. This service helps to retrieve services from the
     * OSGi registry directly from the code.
     */
    public static OsgiUtilsService getServiceUtilsService() throws OsgiServiceNotAvailableException {
        return serviceUtils.getOsgiServiceProxy(OsgiUtilsService.class);
    }

    /**
     * Sets the internal osgiServiceUtils class. This class does not have to be that one exported via
     * {@link #getServiceUtilsService()} since a user can export an implementation of this service with a higher
     * priority (for any reason).
     */
    public static void setOsgiServiceUtils(OsgiUtilsService serviceUtils) throws OsgiServiceNotAvailableException {
        OpenEngSBCoreServices.serviceUtils = serviceUtils.getOsgiServiceProxy(OsgiUtilsService.class);
    }

    public static OutgoingPortUtilService getOutgoingPortUtilService() throws OsgiServiceNotAvailableException {
        return serviceUtils.getOsgiServiceProxy(OutgoingPortUtilService.class);
    }

}
