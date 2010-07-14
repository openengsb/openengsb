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
package org.openengsb.persistence;

import org.junit.Before;
import org.junit.Test;

public class PersistenceEndpointTest {

    private PersistenceEndpoint endpoint;

    @Before
    public void setUp() throws Exception {
        this.endpoint = new PersistenceEndpoint();
        this.endpoint.setPersistence(getPersistenceImpl());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAndQuerySingle() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAndQueryMultiple() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyResultQuery() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDelete() throws Exception {

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() throws Exception {

    }
}
