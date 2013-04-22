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

package org.openengsb.ui.admin.xlink;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.xlink.exceptions.OpenXLinkException;
import org.openengsb.core.api.xlink.service.ui.XLinkMock;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.domain.OOSourceCode.model.OOClass;
import org.openengsb.domain.OOSourceCode.model.OOVariable;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.domain.SQLCode.model.SQLCreateField;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.xlink.mocking.XLinkMockImpl;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

/**
 * The following tests, test the XLink transformation logic based on the XLink showcase from github.
 * The defined mappings between the data points are defined at
 * http://svn.openengsb.org/repos/openengsb/:bak-cpr/thesis/evaluation/testData
 */
public class XLinkMockTest extends AbstractUITest {
    
    private OsgiUtilsService mockedServiceUtils;
    private ApplicationContextMock customContext; 
    private ModelRegistry mockedRegistry;
    
    private final String modelVersion = "3.0.0.SNAPSHOT";
    private XLinkMock xlinkMocking;
    
    @Before
    public void setup() throws Exception {
        mockOsgiService();
        customMockContext();
        setupTesterWithSpringMockContext();  
        xlinkMocking = new XLinkMockImpl(mockedServiceUtils, null);
    }
    
    private void mockOsgiService() throws Exception {
        mockedServiceUtils = mock(OsgiUtilsService.class);
        mockedRegistry = mock(ModelRegistry.class);
        when(mockedServiceUtils.getService(ModelRegistry.class)).thenReturn(mockedRegistry);
    }    
    
    private void customMockContext() throws UserExistsException {
        ((ConnectorManagerImpl) serviceManager).setUtilsService(mockedServiceUtils);
        customContext = new ApplicationContextMock();
        customContext.putBean("osgiUtilsService", mockedServiceUtils);
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().getComponentInstantiationListeners().remove(defaultPaxWicketInjector);
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), customContext));
    }       
    
    @Test
    public void testGetModelIdentifierMock_withOOClass() throws ClassNotFoundException {
        when(mockedRegistry.loadModel(isA(ModelDescription.class))).thenReturn((Class) OOClass.class);          
        List<String> modelClassIdentifiers = 
                xlinkMocking.getModelIdentifierToModelDescription(OOClass.class.getName(), modelVersion);
        assertThat(modelClassIdentifiers.size(), is(2));
        assertTrue(modelClassIdentifiers.contains("className"));
        assertTrue(modelClassIdentifiers.contains("attributes"));
    } 
    
    @Test
    public void testGetModelIdentifierMock_withSQLCreate() throws ClassNotFoundException {
        when(mockedRegistry.loadModel(isA(ModelDescription.class))).thenReturn((Class) SQLCreate.class);          
        List<String> modelClassIdentifiers = 
                xlinkMocking.getModelIdentifierToModelDescription(SQLCreate.class.getName(), modelVersion);
        assertThat(modelClassIdentifiers.size(), is(2));
        assertTrue(modelClassIdentifiers.contains("tableName"));
        assertTrue(modelClassIdentifiers.contains("fields"));
    }       
    
    @Test
    public void testTransformSQLModel_ToOOClassModel() throws ClassNotFoundException, OpenXLinkException {
        when(mockedRegistry.loadModel(isA(ModelDescription.class))).thenReturn((Class) OOClass.class);  
        SQLCreate sqlTable = new SQLCreate();
        sqlTable.setTableName("Client");
        sqlTable.setFields(new SQLCreateField[0]);
        List<Object> resultingObjects = xlinkMocking.transformModelObject(SQLCreate.class.getName(), 
                modelVersion, OOClass.class.getName(), modelVersion, sqlTable);
        assertThat(resultingObjects.size(), is(1));
        assertTrue(resultingObjects.get(0) instanceof OOClass);
        assertThat(((OOClass) resultingObjects.get(0)).getClassName(), is("Customer"));
    }
    
    @Test
    public void testTransformOOClassModel_ToSQLModel() throws ClassNotFoundException, OpenXLinkException {
        when(mockedRegistry.loadModel(isA(ModelDescription.class))).thenReturn((Class) SQLCreate.class);  
        OOClass classDefinition = new OOClass();
        classDefinition.setClassName("Facility_Output");
        OOVariable[] attributes = new OOVariable[5];
        attributes[0] = new OOVariable("fo_Id", "long", false, false);
        attributes[1] = new OOVariable("itemName", "string", false, false);
        attributes[2] = new OOVariable("outputAmount", "int", false, false);
        attributes[3] = new OOVariable("outputDate", "DateTime", false, false);
        attributes[4] = new OOVariable("productionFacility_Id", "long", false, false);
        classDefinition.setAttributes(attributes);
        
        List<Object> resultingObjects = xlinkMocking.transformModelObject(OOClass.class.getName(), 
                modelVersion, SQLCreate.class.getName(), modelVersion, classDefinition);
        assertThat(resultingObjects.size(), is(1));
        assertTrue(resultingObjects.get(0) instanceof SQLCreate);
        assertThat(((SQLCreate) resultingObjects.get(0)).getTableName(), is("Facility_Output"));
        assertThat(((SQLCreate) resultingObjects.get(0)).getFields().length, is(5));
        assertThat(((SQLCreate) resultingObjects.get(0)).getFields()[1].getFieldName(), is("productLabel"));
    }
    
}
