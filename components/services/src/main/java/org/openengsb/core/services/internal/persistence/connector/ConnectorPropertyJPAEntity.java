package org.openengsb.core.services.internal.persistence.connector;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.openengsb.core.api.persistence.PersistenceException;

@Entity
public class ConnectorPropertyJPAEntity {

    @Column(name = "STRVALUE", nullable = false, length = 512)
    private String strValue;
    @Column(name = "CLASSNAME", nullable = false, length = 128)
    private String clazz;

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public static ConnectorPropertyJPAEntity getFromObject(Object obj) {
        ConnectorPropertyJPAEntity entity = new ConnectorPropertyJPAEntity();
        entity.setStrValue(obj.toString());
        entity.setClazz(ClassUtils.primitiveToWrapper(obj.getClass()).getName());
        return entity;
    }

    public Object toObject() throws PersistenceException {
        try {
            Class<?> clazz = Class.forName(this.clazz);
            return ConstructorUtils.invokeConstructor(clazz, this.strValue);
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
    }

}
