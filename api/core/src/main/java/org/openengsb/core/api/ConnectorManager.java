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

import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.model.ModelToViewsTuple;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;

/**
 * Manages connector instances.
 * 
 * This class is responsible for creating new connector-instances as well as updating an deleting them. All instances
 * created with this service are persisted using {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
 */
public interface ConnectorManager {

    /**
     * creates a new connector instance with the given description and returns the id of the newly created service. The
     * new connectorDescription is validated before the connector instance is created. In this validation-step only the
     * combination of the attributes is validated. Each valid is assumed to be valid by itself (e.g. number-attributes)
     * 
     * The connector instance is then registered in the OSGi-registry and persisted using
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     * 
     * 
     * @throws ConnectorValidationFailedException if the attributes supplied with the connectorDescription are invalid
     */
    String create(ConnectorDescription connectorDescription) throws ConnectorValidationFailedException;

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
    void createWithId(String id, ConnectorDescription connectorDescription) throws ConnectorValidationFailedException;

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
    String forceCreate(ConnectorDescription connectorDescription);

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     * 
     * If the attributes are invalid, the connector instance remains unchanged.
     * 
     * @throws ConnectorValidationFailedException if the combination of the new attributes are not valid
     * @throws IllegalArgumentException if no connector instance with the given id is available
     */
    void update(String connectorId, ConnectorDescription connectorDescription)
        throws ConnectorValidationFailedException, IllegalArgumentException;

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     * 
     * Unlike {@link ConnectorManager#update} this method skips the attribute validation before the update.
     * 
     * @throws IllegalArgumentException if no connector instancewith the given id is available
     */
    void forceUpdate(String connectorId, ConnectorDescription connectorDescription)
        throws IllegalArgumentException;

    /**
     * Deletes the connector instance with the given {@code id}.
     * 
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void delete(String connectorId) throws PersistenceException;

    /**
     * Returns the description for the specified connector instance.
     */
    ConnectorDescription getAttributeValues(String connectorId);

    /**
     * Checks for the existence of the specified connector instance.
     * Returns true if connector does exist, false otherwise.
     *
     */
    Boolean connectorExists(String connectorId);
    
    // @extract-start ConnectorManager
    
	
    /**
     * Registers the given connector for XLink. 
     * Throws an 'DomainNotLinkableException' exception if, the supplied connector was 
     * not found or does not belong to a linkable domain.
     * <br/><br/>
     * The connector must provide an array of 'OpenEngSBModel/View' tuples, which define
     * the models it accepts for a given view. 
     * A toolname must be provided to display a human readable name in the 
     * XLink tool-chooser website.<br/>
     * The parameter named 'remoteHostIp' must containing the callers host-IP. This IP is used to identify 
     * the host when the user calls the XLink tool-chooser website. Therefore the host must not reach 
     * the website via a proxy. 
     * <br/><br/>
     * A XLinkUrlBluePrint is returned, it contains instructions about which OpenEngSBModel is to be used for which view 
     * and additional information. With the baseUrl and the keyNames, the caller is able to construct valid 
     * XLink-URLs for one of it's defined model. 
     * <br/><br/>
     * The classes 'XLinkDemonstrationUtils' and 'XLinkUtilsTest' in the services package provide examples of how to 
     * create XLink-URLs. 
     * <br/><br/>
     * Note that this function does not create a Connector, 
     * it must be called with an already registered Connector. 
     * 
     * @see org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException
     * @see org.openengsb.core.api.xlink.model.XLinkUrlBlueprint
     * @see org.openengsb.core.services.xlink.XLinkDemonstrationUtils
     */    
    XLinkUrlBlueprint connectToXLink(String connectorIpToLink, String remoteHostIp, 
            String toolName, ModelToViewsTuple[] modelsToViews);
    
    /**
     * Unregisters the given Connector from XLink.
     */
    void disconnectFromXLink(String connectorIpToLink, String remoteHostIp);

    // @extract-end
}
