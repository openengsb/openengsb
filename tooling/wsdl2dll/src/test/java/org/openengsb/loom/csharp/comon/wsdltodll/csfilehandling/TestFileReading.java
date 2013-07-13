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
import org.openengsb.loom.csharp.comon.wsdltodll.test.resources.ResourceManager;

public class TestFileReading {
    private List<String> expectedResult;
    private FileComparer fileComparer;
    private Log mockedlogger;
    private ResourceManager resourceManager;

    @Before
    public void init() throws IOException {
        mockedlogger = Mockito.mock(Log.class);
        expectedResult = new LinkedList<String>();
        fileComparer = new FileComparer(mockedlogger);
        resourceManager = new ResourceManager();
    }

    @Test
    public void testIfTestCsClassIsCorrectlyRead() throws IOException {
        expectedResult.add("public class TestClass {");
        expectedResult.add("    private String variable;");
        expectedResult.add("    public String getMethod(){");
        expectedResult.add("    }");
        expectedResult.add("}");
        Assert.assertEquals(fileComparer.getFileLinesAsList(resourceManager.getTestCsClass()), expectedResult);
    }

}
