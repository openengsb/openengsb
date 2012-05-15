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


import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.api.xlink.model.XLinkToolView;

/**
 * Manages connector instances.
 * 
 * This class is responsible for creating new connector-instances as well as updating an deleting them. All instances
 * created with this service are persisted using {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
 */
public interface ConnectorManager {

    /**
     * creates a new connector instance with the given id and description. The new connectorDescription is validated
     * before the connector instance is created. In this validation-step only the combination of the attributes is
     * validated. Each valid is assumed to be valid by itself (e.g. number-attributes)
     * 
     * The connector instance is then registered in the OSGi-registry and persisted using
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     * 
     * @throws ConnectorValidationFailedException if the attributes supplied with the connectorDescription are invalid
     */
    void create(ConnectorId id, ConnectorDescription connectorDescription) throws ConnectorValidationFailedException;

    /**
     * creates a new connector instance with the given id and description. It works similar to
     * {@link ConnectorManager#createService} but skips the validation step. However this method still assumes that each
     * attribute is valid by itself (e.g. number-attributes)
     * 
     * The connector instance is then registered in the OSGi-registry and persisted using
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     * 
     * @throws ConnectorValidationFailedException if the attributes supplied with the connectorDescription are invalid
     */
    void forceCreate(ConnectorId id, ConnectorDescription connectorDescription);

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     * 
     * If the attributes are invalid, the connector instance remains unchanged.
     * 
     * @throws ConnectorValidationFailedException if the combination of the new attributes are not valid
     * @throws IllegalArgumentException if no connector instance with the given id is available
     */
    void update(ConnectorId id, ConnectorDescription connectorDescription) throws ConnectorValidationFailedException,
        IllegalArgumentException;

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     * 
     * Unlike {@link ConnectorManager#update} this method skips the attribute validation before the update.
     * 
     * @throws IllegalArgumentException if no connector instancewith the given id is available
     */
    void forceUpdate(ConnectorId id, ConnectorDescription connectorDescription) throws IllegalArgumentException;

    /**
     * Deletes the connector instance with the given {@code id}.
     * 
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void delete(ConnectorId id) throws PersistenceException;

    /**
     * Returns the description for the specified connector instance.
     */
    ConnectorDescription getAttributeValues(ConnectorId id);

    // @extract-start ConnectorManager
    
    /**
     * Registers the given Connector for XLinking. 
     * The Connector must provide the models it accepts for XLink, represented as a Map with the modelClass 
     * information as key and for each key a list of views which are available for the model. 
     * A Toolname must be provided to display a human readable Name of the Tool in the XLink http-servlet.
     * The parameter named hostId must containing the Host-IP. This Id is used to identify the Host when 
     * the user calls the XLink HTTP-Servlet. Therefore the Host must not reach the HTTP-Servlet via a proxy. 
     * A XLinkTemplate is returned, it contains information about which model is to be used for which view.
     * Together with the baseUrl, the tool can now construct valid XLink-URLs with the identifier 
     * of one of the defined model. 
     * 
     * The classes 'XLinkUtils' and 'XLinkUtilsTest' in the commons package also provides examples of how to 
     * create XLink-URLs. 
     * 
     * Note that this function does not create a Connector, 
     * it must be called with an already registered Connector. 
     * 
     * @see org.openengsb.core.api.xlink.XLinkTemplate
     */    
    XLinkTemplate connectToXLink(ConnectorId id, String hostId, 
            String toolName, 
            Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews);
    
    /**
     * Unregisters the given Connector from XLink.
     */
    void disconnectFromXLink(ConnectorId id, String hostId);

    // @extract-end
    
    /**
     * Returns a list of ToolRegistrations to a given hostId. 
     * If the hostId is unknown, returns an emty list.
     */
    List<XLinkToolRegistration> getXLinkRegistration(String hostId);
}
