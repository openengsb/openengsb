package org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling;

import java.io.File;
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

public class TestFileComparerWithLinuxCSFiles {
    private static final int EXAMPLE_FILE1_SIZE_AFTER_FILECOMPARER = 140;
    private static final int EXAMPLE_FILE2_SIZE_AFTER_FILECOMPARER = 123;

    private ResourceManager resourcesManager;
    private FileComparer fileComparer;
    private File exampleFile1;
    private File exampleFile2;
    private int exampleFile1Size;
    private int exampleFile2Size;
    private Log mockedlogger;

    @Before
    public void init() throws IOException {
        mockedlogger = Mockito.mock(Log.class);
        resourcesManager = new ResourceManager();
        exampleFile1 = resourcesManager.getLinuxExampleDoamin0();
        exampleFile2 = resourcesManager.getLinuxExampleDoamin1();
        fileComparer =
            new FileComparer(exampleFile1, exampleFile2, mockedlogger,
                false);
        exampleFile1Size = fileComparer.getFileLinesAsList(exampleFile1).size();
        exampleFile2Size = fileComparer.getFileLinesAsList(exampleFile2).size();

    }

    @Test
    public void testIfRemovingSimilarClassesFromFile1WorksCorrectly() throws IOException {
        fileComparer.removeSimilarClassesFromFile1();
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile1).size(),
            EXAMPLE_FILE1_SIZE_AFTER_FILECOMPARER);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(),
            EXAMPLE_FILE2_SIZE_AFTER_FILECOMPARER);
        Assert.assertNotSame(fileComparer.getFileLinesAsList(exampleFile1).size(),
            exampleFile1Size);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(),
            exampleFile2Size);
    }

    @Test
    public void testIfRemovingSimilarClassesWithAListofFilesWorksCorrectly() throws IOException {
        List<String> filepathes = new LinkedList<String>();
        filepathes.add(exampleFile1.getAbsolutePath());
        filepathes.add(exampleFile2.getAbsolutePath());
        FileComparer.removeSimilaritiesAndSaveFiles(filepathes,
            mockedlogger, false);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile1).size(), 140);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(), 123);
        Assert.assertNotSame(fileComparer.getFileLinesAsList(exampleFile1).size(),
            EXAMPLE_FILE1_SIZE_AFTER_FILECOMPARER);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(),
            EXAMPLE_FILE2_SIZE_AFTER_FILECOMPARER);
    }
}
