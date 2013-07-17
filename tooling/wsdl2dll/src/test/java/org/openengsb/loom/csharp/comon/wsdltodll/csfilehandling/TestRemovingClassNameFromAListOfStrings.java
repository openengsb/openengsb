package org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling.FileComparer;


public class TestRemovingClassNameFromAListOfStrings {
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
    public void testIfAbstractClassWithAbstractMethodsIsCorrectRemoved() {
        String classname = "test";
        file1AsList.add("public abstract partial class " + classname + " {");
        file1AsList.add("protected abstract testMethod1(String arg1);");
        file1AsList.add("protected abstract testMethod2(String arg1, Object obj);");
        file1AsList.add("protected abstract testMethod3(String arg1, Object obj, Boolean b);");
        file1AsList.add("public testMethod4(String arg1) {");
        file1AsList.add("}");
        file1AsList.add("}");
        file1AsList.add("public class CLASSNAME {");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>();
        expectedResult.add("");
        expectedResult.add("public class CLASSNAME {");
        expectedResult.add("}");
        List<String> res = fileComparer.removeLinesContainingClassname(file1AsList, classname);
        Assert.assertEquals(expectedResult.size(), res.size());
        for (int i = 0; i < res.size(); i++) {
            Assert.assertEquals(expectedResult.get(i), res.get(i));
        }
    }

    @Test
    public void testIfInvalidClassStructureIsRemoved() {
        String classToRemove = "test4";
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add(classToRemove + " public class {");
        file1AsList.add("}");
        file1AsList.add("public class test3 {");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>();
        expectedResult.add("public class test {");
        expectedResult.add("}");
        expectedResult.add("public class test2 {");
        expectedResult.add("}");
        expectedResult.add("");
        expectedResult.add("public class test3 {");
        expectedResult.add("}");
        Assert.assertEquals(expectedResult, fileComparer.removeLinesContainingClassname(file1AsList, classToRemove));
    }

    @Test
    public void testIfNoReplaceHappensWhenTheClassNameIsNotPresent() {
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add("public class test3 {");
        file1AsList.add("}");
        Assert.assertEquals(file1AsList,
            fileComparer.removeLinesContainingClassname(file1AsList, "DOESNOTEXIST"));
    }

    @Test
    public void testReplacingAbstractClassesWithImplementingClass() {
        String classNameToRemove = "test4";
        String abstractclassLine = "public abstract class " + classNameToRemove + " : Test {";
        file1AsList.add("public class test {");
        file1AsList.add("}");
        file1AsList.add("public class test2 {");
        file1AsList.add("}");
        file1AsList.add(abstractclassLine);
        file1AsList.add("}");
        file1AsList.add("public abstract class test3 {");
        file1AsList.add("}");
        List<String> expectedResult = new LinkedList<String>();
        expectedResult.add("public class test {");
        expectedResult.add("}");
        expectedResult.add("public class test2 {");
        expectedResult.add("}");
        expectedResult.add("");
        expectedResult.add("public abstract class test3 {");
        expectedResult.add("}");

        List<String> linesWithoutAbstractClasses =
            fileComparer.removeLinesContainingClassname(file1AsList, classNameToRemove);
        Assert.assertEquals(linesWithoutAbstractClasses, expectedResult);
    }

    @Test
    public void testReplaceAClassInClasses() {
        List<String> expectedResult = new LinkedList<String>();
        int removeClassLine = 3;
        int numberOfClasses = 4;
        for (int i = 0; i < numberOfClasses; i++) {
            file1AsList.add("public class test" + i + " {");
            if (removeClassLine == i) {
                expectedResult.add("");
            }
            else {
                expectedResult.add(file1AsList.get(i));
            }
        }
        for (int i = 0; i < numberOfClasses; i++) {
            file1AsList.add("}");
            if (removeClassLine != i) {
                expectedResult.add("}");
            }
        }

        Assert.assertEquals(fileComparer.removeLinesContainingClassname(file1AsList, "test" + removeClassLine),
            expectedResult);
    }
}
