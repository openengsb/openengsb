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
package org.openengsb.config.dao;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.ServiceAssembly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/daoContextTest.xml" })
@Transactional
public class ServiceAssemblyDaoTest {

    @Autowired
    private ServiceAssemblyDao dao;

    @Test
    public void persist_validSa_persistsSaToDatabase() {
        ServiceAssembly sa = new ServiceAssembly();
        sa.setName("a");
        dao.persist(sa);
        assertThat(sa.getId(), notNullValue());
    }

    @Test(expected = PersistenceException.class)
    public void persist_persistingSecondSaWithSameName_shouldFailDueUniqueConstraint() {
        ServiceAssembly sa = new ServiceAssembly();
        sa.setName("a");
        dao.persist(sa);
        ServiceAssembly sa2 = new ServiceAssembly();
        sa2.setName("a");
        dao.persist(sa2);
    }
}
