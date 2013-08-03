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
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class TestRemoveAttributesNotBoundToClass {
    private List<String> file1AsList;
    private FileComparer fileComparer;
    private Log mockedlogger;

    @Before
    public void init() throws IOException {
        mockedlogger = Mockito.mock(Log.class);
        file1AsList = new LinkedList<String>();
        fileComparer = new FileComparer(mockedlogger);
    }

    @Test
    public void testIfAttributesThatAreNotBoundAreRemovedCorrectly() {
        file1AsList.add("[Test]");
        file1AsList.add("[Test1]");
        file1AsList.add("[Test2]");
        file1AsList.add("[Test3]");
        file1AsList.add("");
        file1AsList.add("public abstract partial class test {");
        file1AsList.add("protected abstract testMethod1(String arg1);");
        file1AsList.add("protected abstract testMethod2(String arg1, Object obj);");
        file1AsList.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        file1AsList.add("public testMethod4(String arg1) {");
        file1AsList.add("}");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>();
        expectedResult.add("public abstract partial class test {");
        expectedResult.add("protected abstract testMethod1(String arg1);");
        expectedResult.add("protected abstract testMethod2(String arg1, Object obj);");
        expectedResult.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        expectedResult.add("public testMethod4(String arg1) {");
        expectedResult.add("}");
        expectedResult.add("}");
        List<String> res = fileComparer.removeAttributesNotBoundToClass(file1AsList);
        Assert.assertEquals(expectedResult, res);
    }

    @Test
    public void testIfAttributesThatAreBoundAreNotRemoved() {
        file1AsList.add("[Test]");
        file1AsList.add("[Test1]");
        file1AsList.add("[Test2]");
        file1AsList.add("[Test3]");
        file1AsList.add("public abstract partial class test {");
        file1AsList.add("protected abstract testMethod1(String arg1);");
        file1AsList.add("protected abstract testMethod2(String arg1, Object obj);");
        file1AsList.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        file1AsList.add("public testMethod4(String arg1) {");
        file1AsList.add("}");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>(file1AsList);
        List<String> res = fileComparer.removeAttributesNotBoundToClass(file1AsList);
        Assert.assertEquals(expectedResult, res);
    }

    @Test
    public void testIfAttributesThatAreBoundAreNotRemovedWhereTheClassOpeningBracketIsNotInTheSameLine() {
        file1AsList.add("[Test]");
        file1AsList.add("[Test1]");
        file1AsList.add("[Test2]");
        file1AsList.add("[Test3]");
        file1AsList.add("public abstract partial class test");
        file1AsList.add("{");
        file1AsList.add("protected abstract testMethod1(String arg1);");
        file1AsList.add("protected abstract testMethod2(String arg1, Object obj);");
        file1AsList.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        file1AsList.add("public testMethod4(String arg1) {");
        file1AsList.add("}");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>(file1AsList);
        List<String> res = fileComparer.removeAttributesNotBoundToClass(file1AsList);
        Assert.assertEquals(expectedResult, res);
    }
}
