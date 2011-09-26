package org.openengsb.core.services.internal.persistence.connector;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.persistence.PersistenceException;

@Entity
public class ConnectorPropertiesWrapperJPAEntity {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    @Column(name = "COLLECTION_TYPE", length = 128)
    private String collectionType;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ConnectorPropertyJPAEntity> properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public Set<ConnectorPropertyJPAEntity> getProperties() {
        return properties;
    }

    public void setProperties(Set<ConnectorPropertyJPAEntity> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    public static ConnectorPropertiesWrapperJPAEntity getFromObject(Object property) {
        Class<?> clazz = property.getClass();

        ConnectorPropertiesWrapperJPAEntity wrapper = new ConnectorPropertiesWrapperJPAEntity();

        Set<ConnectorPropertyJPAEntity> propSet = new HashSet<ConnectorPropertyJPAEntity>();
        wrapper.setProperties(propSet);
        wrapper.setCollectionType(clazz.getName());
        Object[] arr;
        if (clazz.isArray()) {
            Class<?> compClass = clazz.getComponentType();
            if (compClass.isPrimitive()) {
                compClass = ClassUtils.primitiveToWrapper(compClass);
                int length = Array.getLength(property);
                Object wrapperArray = Array.newInstance(compClass, length);
                for (int i = 0; i < length; i++) {
                    Array.set(wrapperArray, i, Array.get(property, i));
                }
                arr = (Object[]) wrapperArray;
            } else {
                arr = (Object[]) property;

            }
            loopProperties(Arrays.asList(arr), propSet);
            return wrapper;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            Collection<Object> coll = (Collection<Object>) property;
            loopProperties(coll, propSet);
            return wrapper;
        }

        wrapper.setCollectionType(null);
        propSet.add(ConnectorPropertyJPAEntity.getFromObject(property));
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public Object toObject() throws PersistenceException {
        if (collectionType == null) {
            ConnectorPropertyJPAEntity entity = properties.toArray(new ConnectorPropertyJPAEntity[1])[0];
            return entity.toObject();
        }
        try {
            Class<?> collectionClass = Class.forName(collectionType);
            if (collectionClass.isArray()) {
                Object arr = Array.newInstance(collectionClass, properties.size());
                int i = 0;
                for (ConnectorPropertyJPAEntity entity : properties) {
                    Array.set(arr, i, entity.toObject());
                    i++;
                }
                return arr;
            } else {
                Collection<Object> collection = (Collection<Object>) collectionClass.newInstance();
                for (ConnectorPropertyJPAEntity entity : properties) {
                    collection.add(entity.toObject());
                }
                return collection;
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    private static void loopProperties(Iterable<Object> iter, Set<ConnectorPropertyJPAEntity> propSet) {
        Iterator<Object> iterator = iter.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            ConnectorPropertyJPAEntity entity = ConnectorPropertyJPAEntity.getFromObject(obj);
            propSet.add(entity);
        }
    }

}
