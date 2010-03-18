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
package org.openengsb.config.dao.jpa;

import java.util.List;

import javax.persistence.NoResultException;

import org.openengsb.config.dao.EndpointDao;
import org.openengsb.config.domain.Endpoint;
import org.openengsb.config.domain.ServiceAssembly;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class JpaEndpointDao extends JpaBaseDao<Endpoint> implements EndpointDao {
    @Override
    @SuppressWarnings("unchecked")
    public List<Endpoint> findByServiceAssembly(ServiceAssembly sa) {
        return em.createQuery("select e from Endpoint e where e.serviceAssembly = :sa order by name")
                .setParameter("sa", sa).getResultList();
    }

    @Override
    public Endpoint findByName(String name) {
        try {
            return (Endpoint) em.createQuery("select e from Endpoint e where e.name = :name")
                    .setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
