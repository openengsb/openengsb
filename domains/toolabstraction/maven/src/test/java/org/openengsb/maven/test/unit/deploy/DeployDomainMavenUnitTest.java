/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.maven.test.unit.deploy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.domains.DeployDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.se.endpoints.MavenDeployEndpoint;
import org.openengsb.maven.test.unit.deploy.constants.DeployMvnTestConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Collection of unit tests for implementing class of DeployDomain for maven.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class DeployDomainMavenUnitTest {
    /**
     * The constants needed for testing
     */
    @Resource(name = "unit_constants_deploy")
    private DeployMvnTestConstants CONSTANTS;

    /**
     * Creates a new DeployDomain
     * 
     * @param files The files that shall be administered by the deploy-domain
     */
    private DeployDomain createNewDeployDomain(String[] files) {
        MavenDeployEndpoint domain = new MavenDeployEndpoint() {
        };
        domain.setFiles(files);

        return domain;
    }

    /* end creators */

    /* set up */

    /**
     * Creates a new Maven test-repository on the file-system. * @throws
     * IOException
     */
    @Before
    public void createRepository() throws IOException {
        File repositoryFile = new File(this.CONSTANTS.REPOSITORY);
        if (repositoryFile.exists()) {
            FileUtils.deleteDirectory(repositoryFile);
        }

        assertTrue("Could not create test-repository", repositoryFile.mkdirs());
    }

    /**
     * Deletes the Maven test-repository again.
     * 
     * @throws IOException
     */
    @After
    public void deleteRepository() throws IOException {
        File repositoryFile = new File(this.CONSTANTS.REPOSITORY);

        FileUtils.deleteDirectory(repositoryFile);
    }

    /* end set up */

    /* tests */

    /**
     * Tests if all artifacts listed in the xbean.xml can be listed by the
     * implementation.
     */
    @Test
    @Ignore
    public void listFilesToDeploy_shouldReturnFilesSet() {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.VALID_ARTIFACTS);
        assertArrayEquals(this.CONSTANTS.VALID_ARTIFACTS, deployDomain.listFilesToDeploy());
    }

    /**
     * Tests if the deploy-process for a single artifact was successful by
     * checking the implemenation's return-value and asserting that the
     * deploy-structure for saind artifact was created in the repository
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnPositiveResultAndHaveDeployedArtifactOnPositiveResult() throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.VALID_ARTIFACTS);
        MavenResult result = deployDomain.deployFile(this.CONSTANTS.VALID_ARTIFACT);

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
        assertEquals(this.CONSTANTS.VALID_ARTIFACT, result.getFile());

        // TODO check result.deployed files, once they are implemented

        assertThatMavenStructureExistsInRepository(new File(this.CONSTANTS.VALID_ARTIFACT).getName());
    }

    /**
     * Verify's, that the deploy-process was not successful for a not-existing
     * artifact by checking the implementation's return-value and asserting that
     * the artifact was not deployed to the repository.
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnNegativeResultOnNotConfiguredFile() throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.VALID_ARTIFACTS);
        MavenResult result = deployDomain.deployFile(this.CONSTANTS.NON_EXISTING_ARTIFACT);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals(this.CONSTANTS.NON_EXISTING_ARTIFACT, result.getFile());
        assertNotNull(result.getErrorMessage());
        assertFalse(result.getErrorMessage().isEmpty());

        assertThatArtifactStructuresDoesNotExistInRepository(new File(this.CONSTANTS.NON_EXISTING_ARTIFACT).getName());
    }

    /**
     * Verify's, that the deploy-process was not successful for a invalid
     * artifact (invalid pom) by checking the implementation's return-value and
     * asserting that the artifact was not deployed to the repository.
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnNegativeResultwithErrormessageAndDeployedArtifactOnInvalidPom()
            throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.INVALID_ARTIFACTS);
        MavenResult result = deployDomain.deployFile(this.CONSTANTS.INVALID_POM_ARTIFACT);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals(this.CONSTANTS.INVALID_POM_ARTIFACT, result.getFile());
        assertNotNull(result.getErrorMessage());
        assertFalse(result.getErrorMessage().isEmpty());

        assertThatArtifactStructuresDoesNotExistInRepository(new File(this.CONSTANTS.INVALID_POM_ARTIFACT).getName());
    }

    /**
     * Verify's, that the deploy-process was not successful for a invalid
     * artifact (missin pom) by checking the implementation's return-value and
     * asserting that the artifact was not deployed to the repository.
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void deployFile_shouldReturnNegativeResultwithErrormessageAndDeployedArtifactOnMissingPom()
            throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.INVALID_ARTIFACTS);
        MavenResult result = deployDomain.deployFile(this.CONSTANTS.MISSING_POM_ARTIFACT);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals(this.CONSTANTS.MISSING_POM_ARTIFACT, result.getFile());
        assertNotNull(result.getErrorMessage());
        assertFalse(result.getErrorMessage().isEmpty());

        assertThatArtifactStructuresDoesNotExistInRepository(new File(this.CONSTANTS.INVALID_POM_ARTIFACT).getName());
    }

    /**
     * Tests that all artifact administered by the domain are deployed upon an
     * execute-call. This is done by verifying the result and validating the
     * deploy-files in the repository
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void execute_shouldReturnPositiveResultAndHaveDeployedAllArtifactsOnEcecutionOnValidArtifacts()
            throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.VALID_ARTIFACTS);
        List<MavenResult> resultList = deployDomain.executeDeploy();

        assertEquals(this.CONSTANTS.VALID_ARTIFACTS.length, resultList.size());

        for (MavenResult result : resultList) {
            String resultFile = result.getFile();
            assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
            assertThatMavenStructureExistsInRepository(new File(resultFile).getName());

            // TODO check result.deployedFiles, once they are implemented
        }
    }

    /**
     * Tests that all artifact administered by the domain are not deployed upon
     * an execute-call (because all are invalid). This is done by verifying the
     * result and validating that there are no artifacts in the repository
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void execute_shouldReturnNegativeResultsWithErrorMessagesAndNotHaveMovedOneFile() throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.INVALID_ARTIFACTS);
        List<MavenResult> resultList = deployDomain.executeDeploy();

        assertEquals(this.CONSTANTS.INVALID_ARTIFACTS.length, resultList.size());

        for (MavenResult result : resultList) {
            String resultFile = result.getFile();

            assertEquals(MavenResult.ERROR, result.getMavenOutput());
            assertThatArtifactStructuresDoesNotExistInRepository(new File(resultFile).getName());
            assertNotNull(result.getErrorMessage());
            assertFalse(result.getErrorMessage().isEmpty());
        }
    }

    /**
     * Tests that only valid artifacts are deployed upon an execute-call and
     * invalid aren't. This is done by verifying the result and validating that
     * the artifacts have been deployed or not, depending on the
     * validity-status.
     * 
     * @throws MavenException
     */
    @Test
    @Ignore
    public void execute_shouldBehaveAppropriatelyDependingOnThePomsValidity() throws MavenException {
        DeployDomain deployDomain = createNewDeployDomain(this.CONSTANTS.SOME_VALID_SOME_INVALID_ARTIFACTS);
        List<MavenResult> resultList = deployDomain.executeDeploy();

        assertEquals(this.CONSTANTS.SOME_VALID_SOME_INVALID_ARTIFACTS.length, resultList.size());

        for (MavenResult result : resultList) {
            String resultFile = result.getFile();
            String resultFilename = new File(resultFile).getName();

            if (isElementListedInArray(this.CONSTANTS.VALID_ARTIFACTS, resultFile)) {
                assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
                assertThatMavenStructureExistsInRepository(resultFilename);
            } else if (isElementListedInArray(this.CONSTANTS.INVALID_ARTIFACTS, resultFile)) {
                assertEquals(MavenResult.ERROR, result.getMavenOutput());
                assertThatArtifactStructuresDoesNotExistInRepository(resultFilename);
                assertNotNull(result.getErrorMessage());
                assertFalse(result.getErrorMessage().isEmpty());
            } else {
                assertTrue("result contained unknown file", false);
            }
        }
    }

    /* end tests */

    /* helpers */

    /**
     * Asserts that an artifact was not deployed to the repository
     * 
     * @param artifactName The artifact's name.
     */
    private void assertThatArtifactStructuresDoesNotExistInRepository(String artifactName) {
        File artifactPath = new File(this.CONSTANTS.REPOSITORY + this.CONSTANTS.GROUP_ID_PATH + artifactName);
        assertFalse("Artifact deployed even though it should not have", artifactPath.exists());

    }

    /**
     * Asserts that all the expected files, the deploy-process generates, are
     * present in the repository
     * 
     * @param artifactName The artifact's name
     */
    private void assertThatMavenStructureExistsInRepository(String artifactName) {
        // validate basic artifact directory
        File artifactPath = new File(this.CONSTANTS.REPOSITORY + this.CONSTANTS.GROUP_ID_PATH + artifactName);

        assertTrue("Artifact was not created in repository", artifactPath.exists());
        assertThatMetadataExists(artifactPath);

        // validate version directory with metadata
        File versionPath = new File(artifactPath, this.CONSTANTS.VERSION);
        assertTrue("No version was created for artifact", versionPath.exists());
        assertThatMetadataExists(versionPath);

        // get version directory's other files
        final String finalArtifactName = artifactName;
        File[] jarAndPomFiles = versionPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(finalArtifactName);
            }
        });

        // validate them
        assertEquals("Amount of deployed files differs from expected amount", this.CONSTANTS.EXPECTED_DEPLOYED_FILES
                .intValue(), jarAndPomFiles.length);

        Pattern pattern = Pattern.compile(artifactName + "\\-" + this.CONSTANTS.DEPLOYED_VERSION
                + "\\-[0-9]{8}\\.[0-9]{6}\\-1\\.(jar|pom)(\\.md5|\\.sha1)?");
        for (File file : jarAndPomFiles) {
            assertTrue("Unexpected file was deployed: " + file.getName(), pattern.matcher(file.getName()).matches());
        }
    }

    /**
     * Assert's that the artifact's metadata-files exist
     * 
     * @param basePath The path where the metadata is expectes
     */
    private void assertThatMetadataExists(File basePath) {
        for (String metadataName : this.CONSTANTS.MAVEN_METADATA_FILES) {
            assertTrue("missing metadata-file " + metadataName, new File(basePath, metadataName).exists());
        }
    }

    /**
     * Tests if an Object equals at least one element of an array.
     * 
     * @param <T> The type
     * @param array The array
     * @param element The object to look for in the array
     * @return true if the array contains the object, false otherwise
     */
    private <T> boolean isElementListedInArray(T[] array, T element) {
        for (T arrayEement : array) {
            if (arrayEement.equals(element)) {
                return true;
            }
        }

        return false;
    }

    /* end helpers */
}
