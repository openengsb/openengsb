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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.google.common.collect.Lists;

@Entity
@NamedQueries( { @NamedQuery(name = "ServiceAssembly.findAll", query = "select sa from ServiceAssembly sa") })
@SuppressWarnings("serial")
public class ServiceAssembly extends AbstractDomainObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @OneToMany(mappedBy = "serviceAssembly", cascade = { CascadeType.REMOVE })
    private List<PersistedObject> persistedObjects;

    public ServiceAssembly() {
        name = "";
        persistedObjects = new ArrayList<PersistedObject>();
    }

    public ServiceAssembly(String name) {
        this.name = name;
        persistedObjects = new ArrayList<PersistedObject>();
    }

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

    public List<PersistedObject> getPersistedObjects() {
        return persistedObjects;
    }

    public void setPersistedObjects(List<PersistedObject> persistedObjects) {
        this.persistedObjects = persistedObjects;
    }

    public List<PersistedObject> getEndpoints() {
        List<PersistedObject> list = Lists.newArrayList();
        for (PersistedObject p : persistedObjects) {
            if (p.getPersistedType().equals(PersistedObject.Type.Endpoint)) {
                list.add(p);
            }
        }
        return list;
    }

    public List<PersistedObject> getBeans() {
        List<PersistedObject> list = Lists.newArrayList();
        for (PersistedObject p : persistedObjects) {
            if (p.getPersistedType().equals(PersistedObject.Type.Bean)) {
                list.add(p);
            }
        }
        return list;
    }
}
