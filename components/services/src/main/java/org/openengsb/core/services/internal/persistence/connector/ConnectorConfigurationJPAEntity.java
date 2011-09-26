package org.openengsb.core.services.internal.persistence.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.PersistenceException;

@Entity(name = "CONNECTOR_CONFIGURATION")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "DOMAINTYPE", "CONNECTORTYPE", "INSTANCEID" }))
public class ConnectorConfigurationJPAEntity {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    @Column(name = "DOMAINTYPE", nullable = false, length = 64)
    private String domainType;
    @Column(name = "CONNECTORTYPE", nullable = false, length = 64)
    private String connectorType;
    @Column(name = "INSTANCEID", nullable = false, length = 64)
    private String instanceId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CONNECTOR_ATTRIBUTES")
    private Map<String, String> attributes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Map<String, ConnectorPropertiesWrapperJPAEntity> properties;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public void setProperties(Map<String, ConnectorPropertiesWrapperJPAEntity> properties) {
        this.properties = properties;
    }

    public Map<String, ConnectorPropertiesWrapperJPAEntity> getProperties() {
        return properties;
    }

    public static ConnectorConfigurationJPAEntity generateFromConfigItem(ConfigItem<ConnectorDescription> config) {
        ConnectorConfigurationJPAEntity entity = new ConnectorConfigurationJPAEntity();
        ConnectorDescription desc = config.getContent();
        Map<String, String> metaData = config.getMetaData();

        entity.setInstanceId(metaData.get(Constants.ID_KEY));
        entity.setConnectorType(metaData.get(Constants.CONNECTOR_KEY));
        entity.setDomainType(metaData.get(Constants.DOMAIN_KEY));
        entity.setAttributes(desc.getAttributes());
        entity.setProperties(convertProperties(desc.getProperties()));
        return entity;
    }

    public static ConnectorConfiguration toConfigItem(ConnectorConfigurationJPAEntity entity)
        throws PersistenceException {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put(Constants.ID_KEY, entity.getInstanceId());
        metaData.put(Constants.DOMAIN_KEY, entity.getDomainType());
        metaData.put(Constants.CONNECTOR_KEY, entity.getConnectorType());

        ConnectorDescription desc = new ConnectorDescription();
        desc.setAttributes(entity.getAttributes());
        desc.setProperties(readProperties(entity.getProperties()));

        ConnectorConfiguration config = new ConnectorConfiguration(metaData, desc);
        return config;
    }

    private static Map<String, Object> readProperties(Map<String, ConnectorPropertiesWrapperJPAEntity> map)
        throws PersistenceException {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Entry<String, ConnectorPropertiesWrapperJPAEntity> entry : map.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().toObject());
        }
        return ret;
    }

    private static Map<String, ConnectorPropertiesWrapperJPAEntity> convertProperties(Map<String, Object> properties) {
        Map<String, ConnectorPropertiesWrapperJPAEntity> ret =
            new HashMap<String, ConnectorPropertiesWrapperJPAEntity>();

        for (Entry<String, Object> entry : properties.entrySet()) {
            ret.put(entry.getKey(), ConnectorPropertiesWrapperJPAEntity.getFromObject(entry.getValue()));
        }
        return ret;
    }

}
