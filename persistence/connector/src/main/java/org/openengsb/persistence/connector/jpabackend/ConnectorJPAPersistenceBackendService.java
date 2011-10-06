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
/**
* Licensed to the Austrian Association for Software Tool Integration (AASTI)
* under one or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information regarding copyright
* ownership. The AASTI licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openengsb.persistence.connector.jpabackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceBackendService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ConnectorJPAPersistenceBackendService implements ConfigPersistenceBackendService<ConnectorDescription> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorJPAPersistenceBackendService.class);

    private EntityManager entityManager;

    @Override
    public List<ConfigItem<ConnectorDescription>> load(Map<String, String> metadata) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("load ConnectorConfiguration");
        List<ConnectorConfigurationJPAEntity> entities = searchForMetadata(metadata);

        List<ConfigItem<ConnectorDescription>> ret = new ArrayList<ConfigItem<ConnectorDescription>>();
        for (ConnectorConfigurationJPAEntity entity : entities) {
            ret.add(ConnectorConfigurationJPAEntity.toConfigItem(entity));
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void persist(ConfigItem<ConnectorDescription> config) throws PersistenceException,
        InvalidConfigurationException {
        LOGGER.debug("persisting ConnectorConfiguration");
        Preconditions.checkArgument(supports((Class<? extends ConfigItem<?>>) config.getClass()),
            "Argument type not supported");
        Preconditions.checkNotNull(config, "Config must not be null");
        Preconditions.checkNotNull(config.getMetaData(), "Invalid metadata");
        Preconditions.checkNotNull(config.getContent(), "Invalid content");

        List<ConnectorConfigurationJPAEntity> oldEntities = searchForMetadata(config.getMetaData());

        if (oldEntities.size() > 1) {
            throw new PersistenceException("Unexpected error: Found more than 1 object fitting the metadata!");
        }

        ConnectorConfigurationJPAEntity entity = ConnectorConfigurationJPAEntity.generateFromConfigItem(config);
        if (oldEntities.size() == 1) {
            ConnectorConfigurationJPAEntity old = oldEntities.get(0);
            old.setAttributes(entity.getAttributes());
            old.setProperties(entity.getProperties());
            try {
                entityManager.merge(old);
            } catch (Exception ex) {
                throw new PersistenceException(ex);
            }
            LOGGER.info("updated ConnectorConfiguration");
        }

        if (oldEntities.size() == 0) {
            try {
                entityManager.persist(entity);
            } catch (Exception ex) {
                throw new PersistenceException(ex);
            }
            LOGGER.info("inserted ConnectorConfiguration");
        }
    }

    @Override
    public void remove(Map<String, String> metadata) throws PersistenceException {
        LOGGER.debug("removing ConnectorConfiguration");
        Preconditions.checkNotNull(metadata, "Invalid metadata");

        List<ConnectorConfigurationJPAEntity> ret = searchForMetadata(metadata);
        if (ret.size() == 0) {
            throw new PersistenceException("Configuration to delete, could not be found!");
        }
        ConnectorConfigurationJPAEntity entity = ret.get(0);
        try {
            entityManager.remove(entity);
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
        LOGGER.info("removed ConnectorConfiguration");
    }

    @Override
    public boolean supports(Class<? extends ConfigItem<?>> configItemType) {
        return ConnectorConfiguration.class.isAssignableFrom(configItemType);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private List<ConnectorConfigurationJPAEntity> searchForMetadata(Map<String, String> metaData)
        throws PersistenceException {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConnectorConfigurationJPAEntity> query = cb.createQuery(ConnectorConfigurationJPAEntity.class);
        Root<ConnectorConfigurationJPAEntity> from = query.from(ConnectorConfigurationJPAEntity.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (metaData.get(Constants.ID_KEY) != null) {
            predicates.add(cb.equal(from.get("instanceId"), metaData.get(Constants.ID_KEY)));
        }
        if (metaData.get(Constants.DOMAIN_KEY) != null) {
            predicates.add(cb.equal(from.get("domainType"), metaData.get(Constants.DOMAIN_KEY)));
        }
        if (metaData.get(Constants.CONNECTOR_KEY) != null) {
            predicates.add(cb.equal(from.get("connectorType"), metaData.get(Constants.CONNECTOR_KEY)));
        }

        query.select(from);
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        try {
            return entityManager.createQuery(query).getResultList();
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
    }

}
