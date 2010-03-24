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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.domain.PersistedObject.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/daoContextTest.xml" })
@Transactional
public class PersistedObjectDaoTest {

    @Autowired
    private PersistedObjectDao dao;
    @Autowired
    private ServiceAssemblyDao sadao;

    private ServiceAssembly sa;
    private ServiceAssembly otherSa;

    @Before
    public void setup() {
        sa = new ServiceAssembly("a");
        otherSa = new ServiceAssembly("b");
        sadao.persist(sa);
        sadao.persist(otherSa);
        dao.persist(new PersistedObject(Type.Endpoint, "b", sa));
        dao.persist(new PersistedObject(Type.Endpoint, "a", sa));
        dao.persist(new PersistedObject(Type.Endpoint, "c", sa));
    }

    @After
    public void teardown() {
        sa = null;
    }

    @Test
    public void findByName_existingName_returnsThePO() {
        PersistedObject p = dao.findByName("a");
        assertThat(p.getName(), is("a"));
    }

    @Test
    public void findByName_unknownName_returnsNull() {
        PersistedObject p = dao.findByName("unknown");
        assertThat(p, nullValue());
    }

    @Test
    public void findByServiceAssembly_existingSA_returnsPOsOrderedByName() {
        List<PersistedObject> pos = dao.findByServiceAssembly(sa);
        assertThat(pos.size(), Matchers.is(3));
        assertThat(pos.get(0).getName(), is("a"));
        assertThat(pos.get(1).getName(), is("b"));
        assertThat(pos.get(2).getName(), is("c"));
    }

    @Test
    public void findByName_existingNameAndSa_returnsThePO() {
        PersistedObject p = dao.findByName(sa, "a");
        assertThat(p.getName(), is("a"));
    }

    @Test
    public void findByName_existingNameOtherSa_returnsNull() {
        assertThat(dao.findByName(otherSa, "a"), nullValue());
    }

    @Test
    public void findByName_nonexistingNameExistingSa_returnsNull() {
        assertThat(dao.findByName(sa, "nonExisting"), nullValue());
    }
}
