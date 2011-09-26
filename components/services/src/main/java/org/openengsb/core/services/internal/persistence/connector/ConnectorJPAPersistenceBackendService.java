package org.openengsb.core.services.internal.persistence.connector;

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

        ConnectorConfigurationJPAEntity entity = ConnectorConfigurationJPAEntity.generateFromConfigItem(config);
        try {
            entityManager.persist(entity);
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
        LOGGER.info("inserted ConnectorConfiguration");
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
            predicates.add(cb.equal(from.get("INSTANCEID"), metaData.get(Constants.ID_KEY)));
        }
        if (metaData.get(Constants.DOMAIN_KEY) != null) {
            predicates.add(cb.equal(from.get("DOMAINTYPE"), metaData.get(Constants.DOMAIN_KEY)));
        }
        if (metaData.get(Constants.CONNECTOR_KEY) != null) {
            predicates.add(cb.equal(from.get("CONNECTORTYPE"), metaData.get(Constants.CONNECTOR_KEY)));
        }

        query.select(from);
        query.where(cb.and((Predicate[]) predicates.toArray()));
        try {
            return entityManager.createQuery(query).getResultList();
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
    }

}
