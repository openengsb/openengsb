/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.domain;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.google.common.collect.Maps;

@Entity
@NamedQueries( { @NamedQuery(name = "PersistedObject", query = "select p from PersistedObject p") })
@SuppressWarnings("serial")
public class PersistedObject extends AbstractDomainObject {
    public static enum Type {
        Bean, Endpoint
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Type persistedType;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    private ServiceAssembly serviceAssembly;
    private String componentType;
    private String declaredType;

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.ALL })
    @MapKey(name = "key")
    private Map<String, Attribute> attributes;

    public PersistedObject() {
        name = "";
        componentType = "";
        declaredType = "";
        attributes = Maps.newHashMap();
    }

    public PersistedObject(Type type, String name, ServiceAssembly sa) {
        this();
        this.persistedType = type;
        this.name = name;
        this.serviceAssembly = sa;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Type getPersistedType() {
        return persistedType;
    }

    public void setPersistedType(Type persistedType) {
        this.persistedType = persistedType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceAssembly(ServiceAssembly serviceAssembly) {
        this.serviceAssembly = serviceAssembly;
    }

    public ServiceAssembly getServiceAssembly() {
        return serviceAssembly;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getDeclaredType() {
        return declaredType;
    }

    public void setDeclaredType(String declaredType) {
        this.declaredType = declaredType;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Attribute> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getDetachedValues() {
        Map<String, String> map = Maps.newHashMap();
        for (Map.Entry<String, Attribute> e : attributes.entrySet()) {
            map.put(e.getKey(), e.getValue().toStringValue());
        }
        return map;
    }

    public boolean isBean() {
        return Type.Bean.equals(persistedType);
    }

    public boolean isEndpoint() {
        return Type.Endpoint.equals(persistedType);
    }

    public String extractNewName(Map<String, String> map) {
        if (persistedType.equals(Type.Bean)) {
            return map.get("id");
        } else {
            return map.get("service") + '.' + map.get("endpoint");
        }
    }
}
