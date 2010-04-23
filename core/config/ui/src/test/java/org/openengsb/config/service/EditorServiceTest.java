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
package org.openengsb.config.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.config.dao.PersistedObjectDao;
import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ServiceAssembly;
import org.openengsb.config.service.impl.EditorServiceImpl;

import com.google.common.collect.Maps;

public class EditorServiceTest {

    private EditorService es;
    private PersistedObjectDao dao;
    private ServiceAssembly sa;

    @Before
    public void setup() {
        EditorServiceImpl esi = new EditorServiceImpl();
        es = esi;
        dao = mock(PersistedObjectDao.class);
        esi.setPersistedObjectDao(dao);
        sa = new ServiceAssembly("a");
    }

    @Test
    public void validateName_existingPOWithName_returnsFalse() {
        when(dao.findByName(sa, "existing")).thenReturn(
                new PersistedObject(PersistedObject.Type.Bean, "existing", sa));
        PersistedObject po = new PersistedObject(PersistedObject.Type.Bean, "a", sa);
        Map<String, String> updates = Maps.newHashMap();
        updates.put("id", "existing");
        assertThat(es.validateNameUpdate(po, updates), is(false));
    }

    @Test
    public void validateName_noPOWithName_returnsTrue() {
        when(dao.findByName(sa, "nonexisting")).thenReturn(null);
        PersistedObject po = new PersistedObject(PersistedObject.Type.Bean, "a", sa);
        Map<String, String> updates = Maps.newHashMap();
        updates.put("id", "nonexisting");
        assertThat(es.validateNameUpdate(po, updates), is(true));
    }
}
