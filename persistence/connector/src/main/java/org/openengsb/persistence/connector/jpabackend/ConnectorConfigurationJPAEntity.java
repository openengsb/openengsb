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

package org.openengsb.persistence.connector.jpabackend;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.AbstractDataRow;

@SuppressWarnings("serial")
@Entity(name = "CONNECTOR_CONFIGURATION")
public class ConnectorConfigurationJPAEntity extends AbstractDataRow {

    @Column(name = "DOMAINTYPE", nullable = false, length = 63)
    private String domainType;
    @Column(name = "CONNECTORTYPE", nullable = false, length = 63)
    private String connectorType;
    @Column(name = "INSTANCEID", nullable = false, length = 63, unique = true)
    private String instanceId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CONNECTOR_ATTRIBUTES")
    @MapKeyColumn(name = "ATTR_KEY", length = 255)
    @Column(name = "ATTR_VALUE", length = 255)
    private Map<String, String> attributes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @MapKeyColumn(name = "PROP_KEY", length = 255)
    private Map<String, ConnectorPropertiesWrapperJPAEntity> properties;

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setProperties(
            Map<String, ConnectorPropertiesWrapperJPAEntity> properties) {
        this.properties = properties;
    }

    public Map<String, ConnectorPropertiesWrapperJPAEntity> getProperties() {
        return properties;
    }

    public static ConnectorConfigurationJPAEntity generateFromConfigItem(
            ConfigItem<ConnectorDescription> config) {
        ConnectorConfigurationJPAEntity entity = new ConnectorConfigurationJPAEntity();
        ConnectorDescription desc = config.getContent();
        Map<String, String> metaData = config.getMetaData();

        entity.setInstanceId(metaData.get(Constants.CONNECTOR_PERSISTENT_ID));
        entity.setConnectorType(desc.getConnectorType());
        entity.setDomainType(desc.getDomainType());
        entity.setAttributes(desc.getAttributes());
        entity.setProperties(convertProperties(desc.getProperties()));
        return entity;
    }

    public static ConnectorConfiguration toConfigItem(
            ConnectorConfigurationJPAEntity entity) throws PersistenceException {
        Map<String, String> metaData = new HashMap<String, String>();

        metaData.put(Constants.CONNECTOR_PERSISTENT_ID, entity.getInstanceId());

        ConnectorDescription desc = new ConnectorDescription();
        desc.setConnectorType(entity.getConnectorType());
        desc.setDomainType(entity.getDomainType());
        desc.setAttributes(entity.getAttributes());
        desc.setProperties(readProperties(entity.getProperties()));

        ConnectorConfiguration config = new ConnectorConfiguration(metaData,
            desc);
        return config;
    }

    private static Map<String, Object> readProperties(
            Map<String, ConnectorPropertiesWrapperJPAEntity> map)
        throws PersistenceException {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Entry<String, ConnectorPropertiesWrapperJPAEntity> entry : map
            .entrySet()) {
            ret.put(entry.getKey(), entry.getValue().toObject());
        }
        return ret;
    }

    private static Map<String, ConnectorPropertiesWrapperJPAEntity> convertProperties(
            Map<String, Object> properties) {
        Map<String, ConnectorPropertiesWrapperJPAEntity> ret =
            new HashMap<String, ConnectorPropertiesWrapperJPAEntity>();

        for (Entry<String, Object> entry : properties.entrySet()) {
            ret.put(entry.getKey(), ConnectorPropertiesWrapperJPAEntity
                .getFromObject(entry.getValue()));
        }
        return ret;
    }
}
