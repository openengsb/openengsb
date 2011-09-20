/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.openengsb.labs.paxexam.karaf.options.KarafDistributionConfigurationFilePutOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EDBIT extends AbstractExamTestHelper {

    private EngineeringDatabaseService edbService;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "url", "jdbc:h2:mem:itests"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "driverClassName", "org.h2.jdbcx.JdbcDataSource"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "username", ""),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "password", "")
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
    }
    
    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(edbService, notNullValue());
    }

    @Test
    public void testInsert_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("testobject");
        testObject.put("testkey", "testvalue");
        commit.add(testObject);
        Long testtime = edbService.commit(commit);
        assertThat(testtime.intValue(), not(0));
    }
    
    @Test(expected = EDBException.class)
    public void testDoubleCommit_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        edbService.commit(commit);
        edbService.commit(commit);
    }
    
    @Test
    public void testRetrieveObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject");
        testObject.put("newtestkey", "newtestvalue");
        commit.add(testObject);
        
        edbService.commit(commit);
        
        EDBObject obj = edbService.getObject("newtestobject");
        assertThat(obj, notNullValue());
    }
    
    @Test
    public void testQueryForObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject");
        testObject.put("newtestkey", "newtestvalue");
        commit.add(testObject);
        
        edbService.commit(commit);
        
        List<EDBObject> objects = edbService.query("newtestkey", "newtestvalue");
        assertThat(objects, notNullValue());
        assertThat(objects.size(), not(0));
    }
}
