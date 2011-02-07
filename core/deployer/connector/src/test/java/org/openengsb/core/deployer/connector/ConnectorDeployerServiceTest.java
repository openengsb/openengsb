/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.deployer.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectorDeployerServiceTest {
    
    private ConnectorDeployerService connectorDeployerService;
    
    @Before
    public void setUp() throws Exception {
        connectorDeployerService = new ConnectorDeployerService();
        
        FileUtils.touch(new File("example.connector"));
        FileUtils.touch(new File("other.txt"));
    }
    
    @After
    public void tearDown() {
        FileUtils.deleteQuietly(new File("example.connector"));
        FileUtils.deleteQuietly(new File("other.txt"));
    }
    
    @Test
    public void deployer_canHandleConnectorFiles(){
        File connectorFile = new File("example.connector");

        assertThat(connectorDeployerService.canHandle(connectorFile), is(true));
    }

    @Test
    public void deployer_cannotHandleUnknownFiles(){
        File otherFile = new File("other.txt");
        
        assertThat(connectorDeployerService.canHandle(otherFile), is(false));
    }

}
