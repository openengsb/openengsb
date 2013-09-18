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

package org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.loom.csharp.comon.wsdltodll.test.resources.ResourceManager;

import junit.framework.Assert;

public class TestFileReading {
    private FileComparer fileComparer;
    private Log mockedlogger;
    private ResourceManager resourceManager;

    @Before
    public void init() throws IOException {
        mockedlogger = Mockito.mock(Log.class);
        fileComparer = new FileComparer(mockedlogger);
        resourceManager = new ResourceManager();
    }

    @Test
    public void testIfTestCsClassIsCorrectlyRead() throws IOException {
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add("/**");
        expectedResult.add(" * Licensed to the Austrian Association for Software Tool Integration (AASTI)");
        expectedResult.add(" * under one or more contributor license agreements. See the NOTICE file");
        expectedResult.add(" * distributed with this work for additional information regarding copyright");
        expectedResult.add(" * ownership. The AASTI licenses this file to you under the Apache License,");
        expectedResult.add(" * Version 2.0 (the \"License\"); you may not use this file except in compliance");
        expectedResult.add(" * with the License. You may obtain a copy of the License at");
        expectedResult.add(" *");
        expectedResult.add(" *     http://www.apache.org/licenses/LICENSE-2.0");
        expectedResult.add(" *");
        expectedResult.add(" * Unless required by applicable law or agreed to in writing, software");
        expectedResult.add(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
        expectedResult.add(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        expectedResult.add(" * See the License for the specific language governing permissions and");
        expectedResult.add(" * limitations under the License.");
        expectedResult.add(" */");
        expectedResult.add("");
        expectedResult.add("public class TestClass {");
        expectedResult.add("    private String variable;");
        expectedResult.add("    public String getMethod(){");
        expectedResult.add("    }");
        expectedResult.add("}");
        Assert.assertEquals(fileComparer.getFileLinesAsList(resourceManager.getTestCsClass()), expectedResult);
    }
}
