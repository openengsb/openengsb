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

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.loom.csharp.comon.wsdltodll.test.resources.ResourceManager;

import junit.framework.Assert;

public class TestFileComparerWithWindowsCSFiles {
    private static final int EXAMPLE_FILE1_SIZE_AFTER_FILECOMPARER = 150;
    private static final int EXAMPLE_FILE2_SIZE_AFTER_FILECOMPARER = 130;

    private ResourceManager resourcesManager;
    private FileComparer fileComparer;
    private File exampleFile1;
    private File exampleFile2;
    private int exampleFile1Size;
    private int exampleFile2Size;
    private Log mockedlogger;
    private boolean isWindows;

    @Before
    public void init() throws IOException {
        mockedlogger = Mockito.mock(Log.class);
        resourcesManager = new ResourceManager();
        isWindows = true;
        exampleFile1 = resourcesManager.getWindowsExampleDoamin0();
        exampleFile2 = resourcesManager.getWindowsExampleDoamin1();
        fileComparer =
            new FileComparer(exampleFile1, exampleFile2, mockedlogger,
                isWindows);
        exampleFile1Size = fileComparer.getFileLinesAsList(exampleFile1).size();
        exampleFile2Size = fileComparer.getFileLinesAsList(exampleFile2).size();

    }

    @Test
    public void testIfRemovingSimularClassesFromFile1WorksCorrectly() throws IOException {
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
        FileComparer.removeSimilaritiesAndSaveFiles(filepathes, mockedlogger, isWindows);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile1).size(),
            EXAMPLE_FILE1_SIZE_AFTER_FILECOMPARER);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(),
            EXAMPLE_FILE2_SIZE_AFTER_FILECOMPARER);
        Assert.assertNotSame(fileComparer.getFileLinesAsList(exampleFile1).size(),
            exampleFile1Size);
        Assert.assertEquals(fileComparer.getFileLinesAsList(exampleFile2).size(),
            exampleFile2Size);
    }

}
