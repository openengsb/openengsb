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

package org.openengsb.core.ekb.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.internal.converter.ConnectorInformation;
import org.openengsb.core.ekb.internal.converter.EDBConverter;
import org.openengsb.core.ekb.internal.converter.EDBConverterUtils;
import org.openengsb.core.ekb.internal.models.SubModel;
import org.openengsb.core.ekb.internal.models.TestModel;
import org.openengsb.core.ekb.internal.models.TestModel2.ENUM;

/**
 * The EDBConverter test file only tests the converting Model -> EDBObject since the other way round is tested by the
 * QueryInterfaceService test file.
 */
public class EDBConverterTest {
    private EDBConverter converter;

    @Before
    public void setUp() {
        converter = new EDBConverter();
        EngineeringDatabaseService edbService = mock(EngineeringDatabaseService.class);
        converter.setEdbService(edbService);
    }

    @Test
    public void checkIfModelAgentIsSet_shouldWork() {
        TestModel model = new TestModel();
        assertThat("TestModel isn't enhanced. Maybe you forgot to set the java agent?",
            model instanceof OpenEngSBModel, is(true));
    }

    @Test
    public void testSimpleModelToEDBObjectConversion_shouldWork() {
        TestModel model = new TestModel();
        model.setId("test");
        Date date = new Date();
        model.setDate(date);
        model.setEnumeration(ENUM.A);
        model.setName("testobject");

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(0);

        assertThat(object.get("connectorId").toString(), is("testconnector"));
        assertThat(object.get("id").toString(), is("test"));
        assertThat(object.get("oid").toString(), is(EDBConverterUtils.createOID(model, "testdomain", "testconnector")));
        assertThat(object.get("domainId").toString(), is("testdomain"));
        assertThat(object.get("name").toString(), is("testobject"));
        assertThat(object.get("instanceId").toString(), is("testinstance"));
        assertThat((ENUM) object.get("enumeration"), is(ENUM.A));
        assertThat((Date) object.get("date"), is(date));
        assertThat((object.get(EDBConstants.MODEL_TYPE)).toString(), is(TestModel.class.toString()));
    }

    @Test
    public void testComplexModelToEDBObjectConversion_shouldWork() {
        TestModel model = new TestModel();
        model.setId("test");

        SubModel sub = new SubModel();
        sub.setId("sub");
        sub.setValue("teststring");
        model.setSub(sub);

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(1);
        assertThat(object.get("sub").toString(), is(EDBConverterUtils.createOID(sub, "testdomain", "testconnector")));
        EDBObject subObject = objects.get(0);
        assertThat(subObject.get("id").toString(), is("sub"));
        assertThat((subObject.get(EDBConstants.MODEL_TYPE)).toString(), is(SubModel.class.toString()));
    }

    @Test
    public void testComplexListModelToEDBObjectConversion_shouldWork() {
        TestModel model = new TestModel();
        model.setId("test");

        SubModel sub1 = new SubModel();
        sub1.setId("sub1");
        sub1.setValue("teststring1");
        SubModel sub2 = new SubModel();
        sub2.setId("sub2");
        sub2.setValue("teststring2");
        List<SubModel> subs = new ArrayList<SubModel>();
        subs.add(sub1);
        subs.add(sub2);

        model.setSubs(subs);

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(2);

        assertThat(object.get("subs0").toString(),
            is(EDBConverterUtils.createOID(sub1, "testdomain", "testconnector")));
        assertThat(object.get("subs1").toString(),
            is(EDBConverterUtils.createOID(sub2, "testdomain", "testconnector")));

        EDBObject subObject1 = objects.get(0);
        assertThat(subObject1.get("id").toString(), is("sub1"));
        assertThat((subObject1.get(EDBConstants.MODEL_TYPE)).toString(), is(SubModel.class.toString()));
        EDBObject subObject2 = objects.get(1);
        assertThat(subObject2.get("id").toString(), is("sub2"));
        assertThat((subObject2.get(EDBConstants.MODEL_TYPE)).toString(), is(SubModel.class.toString()));
    }

    @Test
    public void testMapModelToEDBObjectConversion_shouldWork() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("keyA", "valueA");
        map.put("keyB", "valueB");
        TestModel model = new TestModel();
        model.setId("test");
        model.setMap(map);

        ConnectorInformation id = getTestConnectorInformation();

        EDBObject object = converter.convertModelToEDBObject(model, id).get(0);

        assertThat(object.get("map0.key").toString(), is("keyA"));
        assertThat(object.get("map0.value").toString(), is("valueA"));
        assertThat(object.get("map1.key").toString(), is("keyB"));
        assertThat(object.get("map1.value").toString(), is("valueB"));
    }

    private ConnectorInformation getTestConnectorInformation() {
        return new ConnectorInformation("testdomain", "testconnector", "testinstance");
    }
}
