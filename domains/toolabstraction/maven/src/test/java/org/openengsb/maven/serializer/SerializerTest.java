/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.maven.serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.transform.Source;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.build.constants.BuildMvnTestConstants;
import org.openengsb.maven.common.exceptions.SerializationException;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.common.serializer.ProjectConfigurationSerializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class SerializerTest extends TestCase {

    @Resource(name = "unit_constants_build")
    private BuildMvnTestConstants CONSTANTS;

    private File baseDirectory;

    private ClassPathResource res;

    @Before
    @Override
    public void setUp() throws Exception {
        this.res = new ClassPathResource(this.CONSTANTS.getTest_project());
        this.baseDirectory = this.res.getFile();
    }

    @Test
    public void buildSerializer_Failure() throws SerializationException {
        MavenResult buildresult = new MavenResult();
        buildresult.setMavenOutput(MavenResult.FAILURE);
        buildresult.setTimestamp(new Date().getTime());
        buildresult.setTask("Some Task");
        buildresult.setErrorMessage("errormessage");

        List<Exception> exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception("test"));
        exceptions.add(new Exception("test2"));
        buildresult.setExceptions(exceptions);

        Source source = MavenResultSerializer.serializeAsSource(null, buildresult);
        MavenResult result = MavenResultSerializer.deserializeSource(source);

        assertEquals(buildresult.getMavenOutput(), result.getMavenOutput());
        assertEquals(buildresult.getTimestamp(), result.getTimestamp());
        assertEquals(buildresult.getTask(), result.getTask());

        assertEquals(buildresult.getExceptions().get(0).getMessage(), result.getExceptions().get(0).getMessage());
        assertEquals(buildresult.getExceptions().get(1).getMessage(), result.getExceptions().get(1).getMessage());

        assertEquals(buildresult.getErrorMessage(), result.getErrorMessage());
    }

    @Test
    public void buildSerializer_Success() throws SerializationException {
        MavenResult buildresult = new MavenResult();
        buildresult.setMavenOutput(MavenResult.SUCCESS);
        buildresult.setTimestamp(new Date().getTime());
        buildresult.setTask("Some Task");

        List<Exception> exceptions = new ArrayList<Exception>();
        exceptions.add(new Exception("test"));
        exceptions.add(new Exception("test2"));
        buildresult.setExceptions(exceptions);

        Source source = MavenResultSerializer.serializeAsSource(null, buildresult);
        MavenResult result = MavenResultSerializer.deserializeSource(source);

        assertEquals(buildresult.getMavenOutput(), result.getMavenOutput());
        assertEquals(buildresult.getTimestamp(), result.getTimestamp());

        assertNull(result.getTask());
        assertNull(result.getExceptions());
    }

    @Test
    public void deploySerializer_Failure() throws SerializationException {
        MavenResult deployresult1 = new MavenResult();
        deployresult1.setMavenOutput(MavenResult.FAILURE);
        deployresult1.setTimestamp(new Date().getTime());
        deployresult1.setTask("Some Task");
        deployresult1.setFile("testFile");
        deployresult1.setDeployedFiles(new String[] { "deployFile1", "deployFile2" });

        List<Exception> exceptions1 = new ArrayList<Exception>();
        exceptions1.add(new Exception("test"));
        exceptions1.add(new Exception("test2"));
        deployresult1.setExceptions(exceptions1);

        MavenResult deployresult2 = new MavenResult();
        deployresult2.setMavenOutput(MavenResult.FAILURE);
        deployresult2.setTimestamp(new Date().getTime());
        deployresult2.setTask("Some Task and more");

        List<Exception> exceptions2 = new ArrayList<Exception>();
        exceptions2.add(new Exception("test3"));
        exceptions2.add(new Exception("test4"));
        deployresult2.setExceptions(exceptions2);

        List<MavenResult> listResults = new ArrayList<MavenResult>();
        listResults.add(deployresult1);
        listResults.add(deployresult2);

        Source source = MavenResultSerializer.serialize(null, listResults);
        List<MavenResult> result = MavenResultSerializer.deserializeListSource(source);

        assertEquals(deployresult1.getMavenOutput(), result.get(0).getMavenOutput());
        assertEquals(deployresult1.getTimestamp(), result.get(0).getTimestamp());
        assertEquals(deployresult1.getTask(), result.get(0).getTask());

        assertEquals(deployresult1.getExceptions().get(0).getMessage(), result.get(0).getExceptions().get(0)
                .getMessage());
        assertEquals(deployresult1.getExceptions().get(1).getMessage(), result.get(0).getExceptions().get(1)
                .getMessage());

        assertEquals(deployresult1.getFile(), result.get(0).getFile());

        assertEquals(deployresult1.getDeployedFiles()[0], result.get(0).getDeployedFiles()[0]);
        assertEquals(deployresult1.getDeployedFiles()[1], result.get(0).getDeployedFiles()[1]);
    }

    @Test
    public void deploySerializer_Success() throws SerializationException {
        MavenResult deployresult1 = new MavenResult();
        deployresult1.setMavenOutput(MavenResult.SUCCESS);
        deployresult1.setTimestamp(new Date().getTime());
        deployresult1.setTask("Some Task");

        List<Exception> exceptions1 = new ArrayList<Exception>();
        exceptions1.add(new Exception("test"));
        exceptions1.add(new Exception("test2"));
        deployresult1.setExceptions(exceptions1);

        MavenResult deployresult2 = new MavenResult();
        deployresult2.setMavenOutput(MavenResult.SUCCESS);
        deployresult2.setTimestamp(new Date().getTime());
        deployresult2.setTask("Some Task and more");

        List<Exception> exceptions2 = new ArrayList<Exception>();
        exceptions2.add(new Exception("test3"));
        exceptions2.add(new Exception("test4"));
        deployresult2.setExceptions(exceptions2);

        List<MavenResult> listResults = new ArrayList<MavenResult>();
        listResults.add(deployresult1);
        listResults.add(deployresult2);
        Source source = MavenResultSerializer.serialize(null, listResults);
        List<MavenResult> result = MavenResultSerializer.deserializeListSource(source);

        assertEquals(deployresult1.getMavenOutput(), result.get(0).getMavenOutput());
        assertEquals(deployresult1.getTimestamp(), result.get(0).getTimestamp());

        assertNull(result.get(0).getTask());
        assertNull(result.get(0).getExceptions());
    }

    @Test
    public void projectConfigurationSerializer() {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setBaseDirectory(this.baseDirectory);
        projectConfig.setGoals(Arrays.asList(new String[] { "clean", "package" }));

        Source source = ProjectConfigurationSerializer.serialize(null, projectConfig);
        ProjectConfiguration result = ProjectConfigurationSerializer.deserializeSource(source);

        assertEquals(projectConfig.getBaseDirectory().getAbsolutePath(), result.getBaseDirectory().getAbsolutePath());

        List<String> goals = projectConfig.getGoals();

        assertTrue(goals.contains(result.getGoals().get(0)));
        assertTrue(goals.contains(result.getGoals().get(1)));

    }
}
