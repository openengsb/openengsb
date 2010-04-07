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
package org.openengsb.core;

import java.lang.reflect.Method;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.contextcommon.ContextHelperImpl;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

public class ContextRegistrationTest {
    private ContextHelperImpl contextHelper;
    
    @Before
    public void setUp() {
        contextHelper = Mockito.mock(ContextHelperImpl.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSURegistering() throws Exception {
        OpenEngSBComponent component = new OpenEngSBComponent();
        OpenEngSBEndpoint endpoint = new OpenEngSBEndpoint(component, Mockito.mock(ServiceEndpoint.class));
        endpoint.setContextHelper(contextHelper);
        
        Method method = OpenEngSBEndpoint.class.getDeclaredMethod("register", new Class[0]);
        method.setAccessible(true);
        method.invoke(endpoint, new Object[0]);
        
        Mockito.verify(contextHelper, Mockito.never()).store(Mockito.anyMap());
    }
}
