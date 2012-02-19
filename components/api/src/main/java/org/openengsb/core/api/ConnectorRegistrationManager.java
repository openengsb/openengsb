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

package org.openengsb.core.api;

import java.util.HashMap;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.xlink.XLinkUrlTemplate;

/**
 * Manages the registration of services for connector-instances at runtime.
 *
 * It does not handle persistence.
 *
 * This class is meant to be used with {@link ConnectorManager} which handles the persistence.
 */
public interface ConnectorRegistrationManager extends OpenEngSBService {

    /**
     * updates a the registration service instance. If no service with the given ID is registered a new registration is
     * created.
     *
     * @throws ConnectorValidationFailedException if the validation of the connector-attributes fails.
     */
    void updateRegistration(ConnectorId id, ConnectorDescription connectorDescpription)
        throws ConnectorValidationFailedException;
    
    /**
     * Registers the given Connector for XLinking. The Connector must define the views he offers for XLink.
     * Returns an appropriate XLinkUrlTemplate for the Connector.
     * @param id
     * @param connectorDescpription 
     * @param availableViews HashMap containing ViewId, Viewdescription combinations
     * @return an appropriate XLinkUrlTemplate for the Connector.
     */
    public XLinkUrlTemplate registerForXLink(ConnectorId id, ConnectorDescription connectorDescpription, HashMap<String,String> availableViews);

    /**
     * updates a the registration service instance. If no service with the given ID is registered a new registration is
     * created.
     *
     * As opposed to {@link ConnectorRegistrationManager#updateRegistration} this method skips the validation-step.
     */
    void forceUpdateRegistration(ConnectorId id, ConnectorDescription connectorDescpription);

    /**
     * Removes the connector instance with the given {@code id} from the service registry.
     *
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void remove(ConnectorId id);

}
