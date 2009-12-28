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

package org.openengsb.maven.installfile;

import java.io.File;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.installfile.constants.InstallFileMvnTestConstants;
import org.openengsb.maven.se.endpoints.MavenInstallFileEndpoint;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenInstallFileEndpointTest extends TestCase {

    @Resource(name = "unit_constants_installfile")
    private InstallFileMvnTestConstants constants;
    private File fileToInstall;
    private File settingsFile;

    @Before
    @Override
    public void setUp() throws Exception {
        ClassPathResource res = new ClassPathResource(this.constants.validFullPathToFile);
        this.fileToInstall = res.getFile();
        res = new ClassPathResource(this.constants.settingsFile);
        this.settingsFile = res.getFile();
        deleteTestRepo();
    }

    private void deleteTestRepo() throws Exception {
        File testRepo = new File("target/test-classes/testRepo/");
        if (testRepo.exists()) {
            FileUtils.deleteDirectory(testRepo);
        }
    }

    @Test
    @Ignore
    public void installFile_shouldReturnPositiveResultAndRepoStructureShouldExistForValidInput() throws MavenException {
        MavenInstallFileEndpoint endpoint = new MavenInstallFileEndpoint();
        Options options = new Options();
        options.setSettings(this.settingsFile);
        endpoint.setOptions(options);

        MavenResult result = endpoint.installFile(new InstallFileDescriptor(this.fileToInstall.getAbsolutePath(),
                this.constants.validGroupId, this.constants.validArtifactId, this.constants.validVersion,
                this.constants.jarPackaging));

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
        assertTrue("The file was not correctly installed into the repo.", new File("target/test-classes/testRepo/"
                + this.constants.validGroupId.replace('.', File.separatorChar) + File.separator
                + this.constants.validArtifactId + File.separator + this.constants.validVersion + File.separator
                + this.constants.validArtifactId + "-" + this.constants.validVersion + ".jar").exists());
    }

    @Test
    @Ignore
    public void installFile_shouldReturnNegativeResultIfFileToBeInstalledDoesNotExist() throws MavenException {
        MavenInstallFileEndpoint endpoint = new MavenInstallFileEndpoint();
        Options options = new Options();
        options.setSettings(this.settingsFile);
        endpoint.setOptions(options);

        MavenResult result = endpoint.installFile(new InstallFileDescriptor(this.constants.invalidFullPathToFile,
                this.constants.validGroupId, this.constants.validArtifactId, this.constants.validVersion,
                this.constants.jarPackaging));

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertFalse("There should not exist any file in the test repo.", new File("target/test-classes/testRepo/"
                + this.constants.validGroupId.replace('.', File.separatorChar) + File.separator
                + this.constants.validArtifactId + File.separator + this.constants.validVersion + File.separator
                + this.constants.validArtifactId + "-" + this.constants.validVersion + ".jar").exists());
    }

    @Test
    @Ignore
    public void installFile_shouldReturnNegativeResultIfFileDescriptorIsNotValid() throws MavenException {
        MavenInstallFileEndpoint endpoint = new MavenInstallFileEndpoint();
        Options options = new Options();
        options.setSettings(this.settingsFile);
        endpoint.setOptions(options);

        InstallFileDescriptor invalidDescriptor = new InstallFileDescriptor(null, this.constants.validGroupId,
                this.constants.validArtifactId, this.constants.validVersion, this.constants.jarPackaging);

        assertFalse("File descriptor is NOT invalid!", invalidDescriptor.validate());

        MavenResult result = endpoint.installFile(invalidDescriptor);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
    }
}