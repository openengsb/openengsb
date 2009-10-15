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

package org.openengsb.maven.build;

import java.io.File;
import java.util.Arrays;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.build.constants.BuildMvnTestConstants;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.se.endpoints.MavenBuildEndpoint;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class BuildDomainFailureTest extends TestCase {

    @Resource(name = "unit_constants_build")
    private BuildMvnTestConstants CONSTANTS;

    private File baseDirectory;

    private File settings;

    private ClassPathResource res;

    @Before
    @Override
    public void setUp() throws Exception {
        this.res = new ClassPathResource(this.CONSTANTS.getTest_project_fail());
        this.baseDirectory = this.res.getFile();

        this.res = new ClassPathResource(this.CONSTANTS.getTest_settings_file());
        this.settings = this.res.getFile();
    }

    @Test
    public void buildShouldFail_clean_install() throws MavenException {

        Options options = new Options();
        options.setSettings(this.settings);

        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setGoals(Arrays.asList(new String[] { "clean", "install" }));
        projectConfig.setBaseDirectory(this.baseDirectory);

        MavenBuildEndpoint maven = new MavenBuildEndpoint();
        maven.setOptions(options);
        maven.setProjectConfiguration(projectConfig);
        MavenResult result = maven.executeBuild();

        assertEquals(MavenResult.FAILURE, result.getMavenOutput());
    }

}
