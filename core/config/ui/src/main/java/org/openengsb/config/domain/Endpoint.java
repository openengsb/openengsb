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
@NamedQueries( { @NamedQuery(name = "Endpoint.findAll", query = "from Endpoint") })
public class Endpoint extends AbstractDomainObject {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @ManyToOne(optional = false)
    private ServiceAssembly serviceAssembly;
    private String componentType;
    private String endpointType;
    @OneToMany(mappedBy = "endpoint", cascade = { CascadeType.ALL })
    @MapKey(name = "key")
    private Map<String, KeyValue> values;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public void setValues(Map<String, KeyValue> values) {
        this.values = values;
    }

    public Map<String, KeyValue> getValues() {
        return values;
    }

    public Map<String, String> getDetachedValues() {
        Map<String, String> map = Maps.newHashMap();
        for (Map.Entry<String, KeyValue> e : values.entrySet()) {
            map.put(e.getKey(), e.getValue().getValue());
        }
        return map;
    }
}
