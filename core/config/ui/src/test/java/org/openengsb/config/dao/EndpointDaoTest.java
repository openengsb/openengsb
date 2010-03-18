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
import org.openengsb.config.dao.EndpointDao;
import org.openengsb.config.dao.ServiceAssemblyDao;
import org.openengsb.config.domain.Endpoint;
import org.openengsb.config.domain.ServiceAssembly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/daoContextTest.xml" })
@Transactional
public class EndpointDaoTest {

    @Autowired
    private EndpointDao dao;
    @Autowired
    private ServiceAssemblyDao sadao;

    private ServiceAssembly sa;

    @Before
    public void setup() {
        sa = new ServiceAssembly("a");
        sadao.persist(sa);
        dao.persist(new Endpoint("b", sa));
        dao.persist(new Endpoint("a", sa));
        dao.persist(new Endpoint("c", sa));
    }

    @After
    public void teardown() {
        sa = null;
    }

    @Test
    public void findByName_existingName_returnsTheEndpoint() {
        Endpoint e = dao.findByName("a");
        assertThat(e.getName(), is("a"));
    }

    @Test
    public void findByName_unknownName_returnsNull() {
        Endpoint e = dao.findByName("unknown");
        assertThat(e, nullValue());
    }

    @Test
    public void findByServiceAssembly_existingSA_returnsEndpointsOrderedByName() {
        List<Endpoint> endpoints = dao.findByServiceAssembly(sa);
        assertThat(endpoints.size(), Matchers.is(3));
        assertThat(endpoints.get(0).getName(), is("a"));
        assertThat(endpoints.get(1).getName(), is("b"));
        assertThat(endpoints.get(2).getName(), is("c"));
    }
}
