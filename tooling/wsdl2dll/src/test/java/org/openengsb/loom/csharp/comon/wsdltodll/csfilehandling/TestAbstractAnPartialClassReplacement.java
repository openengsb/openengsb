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

public class TestAbstractAnPartialClassReplacement {
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
    public void testIfAbstractPartialClassIsCorrectlyConvertedToAInterface() {
        file1AsList.add("public abstract partial class test {");
        file1AsList.add("protected abstract testMethod1(String arg1);");
        file1AsList.add("protected abstract testMethod2(String arg1, Object obj);");
        file1AsList.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        file1AsList.add("public testMethod4(String arg1) {");
        file1AsList.add("}");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>();
        expectedResult.add("public interface ItestSoapBinding {");
        expectedResult.add("  testMethod1(String arg1);");
        expectedResult.add("  testMethod2(String arg1, Object obj);");
        expectedResult.add("  testMethod3(String arg1, Object obj, Boolean b);");
        expectedResult.add("public testMethod4(String arg1) {");
        expectedResult.add("}");
        expectedResult.add("}");
        Assert.assertEquals(expectedResult, fileComparer.replaceAbstractClasses(file1AsList));
    }

    @Test
    public void testIfInvalidClassStructureIsNotReplaced() {
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add("test4 public class {");
        file1AsList.add("}");
        file1AsList.add("public class test3 {");
        file1AsList.add("}");
        Assert.assertEquals(file1AsList, fileComparer.replaceAbstractClasses(file1AsList));
    }

    @Test
    public void testIfNoReplaceHappensWhenNoAbstractPartialClasses() {
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add("public class test3 {");
        file1AsList.add("}");
        Assert.assertEquals(file1AsList,
            fileComparer.replaceAbstractClasses(file1AsList));
    }

    @Test
    public void testOneReplacingAbstractClassesWithImplementation() {
        String abstractclassLine =
            "public abstract partial class test4 : WebService {";
        String resultingClass =
            "public interface Itest4SoapBinding {";
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add(abstractclassLine);
        file1AsList.add("}");
        file1AsList.add("public abstract class test3 {");
        file1AsList.add("}");

        List<String> linesWithoutAbstractClasses = fileComparer.replaceAbstractClasses(file1AsList);
        Assert.assertEquals(linesWithoutAbstractClasses.size(), file1AsList.size());
        Assert.assertTrue(linesWithoutAbstractClasses.contains(resultingClass));
        Assert.assertFalse(linesWithoutAbstractClasses.contains(abstractclassLine));
    }

    @Test
    public void testOneReplacingAbstractClasses() {
        String abstractclassLine = "public abstract class test4 {";
        String resultingClass = "  class test4 {";
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add(abstractclassLine);
        file1AsList.add("}");
        file1AsList.add("public abstract class test3 {");
        file1AsList.add("}");

        List<String> linesWithoutAbstractClasses = fileComparer.replaceAbstractClasses(file1AsList);
        Assert.assertEquals(linesWithoutAbstractClasses.size(), file1AsList.size());
        Assert.assertTrue(linesWithoutAbstractClasses.contains(resultingClass));
        Assert.assertFalse(linesWithoutAbstractClasses.contains(abstractclassLine));
    }

    @Test
    public void testReplaceOneAbstractPartialClasses() {
        String partialAbstractclassLine =
            "public abstract partial class test4 {";
        String resultingInterfaceClass = "public interface Itest4SoapBinding {";
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add(partialAbstractclassLine);
        file1AsList.add("}");
        file1AsList.add("public abstract class test3 {");
        file1AsList.add("}");

        List<String> linesWithoutAbstractClasses = fileComparer.replaceAbstractClasses(file1AsList);
        Assert.assertEquals(linesWithoutAbstractClasses.size(), file1AsList.size());
        Assert.assertTrue(linesWithoutAbstractClasses.contains(resultingInterfaceClass));
        Assert.assertFalse(linesWithoutAbstractClasses.contains(partialAbstractclassLine));
    }

    @Test
    public void testReplaceFourAbstractPartialClasses() {
        List<String> abstractPartialClasses = new
            LinkedList<String>();
        List<String> resultingAbstractPartialClasses = new LinkedList<String>();
        for (int i = 0; i < 4; i++) {
            abstractPartialClasses.add("public abstract partial class test" + i + " {");
            resultingAbstractPartialClasses.add("public interface Itest" + i + "SoapBinding {");

        }
        file1AsList.addAll(abstractPartialClasses);
        file1AsList.add("}");
        file1AsList.add("}");
        file1AsList.add("}");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>(resultingAbstractPartialClasses);
        expectedResult.add("}");
        expectedResult.add("}");
        expectedResult.add("}");
        expectedResult.add("}");
        Assert.assertEquals(fileComparer.replaceAbstractClasses(file1AsList), expectedResult);
    }
}
