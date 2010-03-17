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
package org.openengsb.trac.test;

import java.util.HashMap;
import java.util.Vector;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.trac.TracConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class TracTest {
    @Resource
    private TracConnector tracConnector;

    @Test
    public void testGetFields() {
        Vector<HashMap<?, ?>> fields = tracConnector.getFields();
    }
}