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

package org.openengsb.maven.test.unit;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.se.endpoints.MavenTestEndpoint;
import org.openengsb.maven.test.unit.constants.TestMvnTestConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class TestDomainNoSurefireTest extends TestCase {

    @Resource(name = "unit_constants_test")
    private TestMvnTestConstants CONSTANTS;

    private File pom;

    private File pom_modified;

    private File baseDirectory;

    private File testDir;

    private ClassPathResource res;

    @Before
    @Override
    public void setUp() throws Exception {
        this.res = new ClassPathResource(this.CONSTANTS.getTest_no_surefire());
        this.baseDirectory = this.res.getFile();

        this.pom = new File(this.baseDirectory, "/pom.xml");
        this.testDir = new File(this.baseDirectory, "/testDir");

        // create testDir and copy old pom.xml
        FileUtils.mkdir(this.testDir.getAbsolutePath());
        FileUtils.copyFileToDirectory(this.pom, this.testDir);

    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // Delete modified pom
        new File(this.baseDirectory, "pom.xml").delete();
        FileUtils.copyFileToDirectory(new File(this.testDir, "pom.xml"), this.baseDirectory);

        // delete copy and testDir
        new File(this.testDir, "pom.xml").delete();
        FileUtils.deleteDirectory(this.testDir);

        this.pom_modified = new File(this.baseDirectory, "pom-modified.xml");
        if (this.pom_modified != null && this.pom_modified.exists()) {
            this.pom_modified.delete();
        }
    }

    @Test
    @Ignore
    public void shouldRunTestsParameter_BaseDirectory_enableTestReport_TestReportDir() throws Exception {
        MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setTestReport(true);
        mavenTester.setTestReportDir(new File(this.baseDirectory, "Test-Surefire-Report"));

        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        assertEquals(String.valueOf(mavenTester.getBaseDirectory()), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getTextContent());

        assertEquals(String.valueOf(mavenTester.isTestReport()), mavenTester.getDom().getSurefireConfigurationNode()
                .getFirstChild().getNextSibling().getTextContent());

        assertEquals(String.valueOf(mavenTester.getTestReportDir().getAbsolutePath()), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getNextSibling().getTextContent());

        MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void shouldRunTestsParameter_BaseDirectory_TestReportDir_TestSrcDir_TestClassesDir() throws Exception {
        MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setTestReportDir(new File(this.baseDirectory, "Test-Surefire-Report"));
        mavenTester.setTestSrcDirs(new File(this.baseDirectory, "test"));
        mavenTester.setTestClassesDir(new File(this.baseDirectory, "test-classes"));

        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        assertEquals(mavenTester.getBaseDirectory().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getTextContent());

        assertEquals(mavenTester.getTestReportDir().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getTextContent());

        assertEquals(mavenTester.getTestSrcDirs().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getNextSibling().getTextContent());

        assertEquals(mavenTester.getTestClassesDir().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getNextSibling().getNextSibling()
                .getTextContent());

        MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());

    }

    @Test
    @Ignore
    public void shouldCreateSurefirePluginWithOneExclusion() throws Exception {
        MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setExcludes(new String[] { "**/HelpTest.java" });

        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        assertEquals(mavenTester.getExcludes()[0], mavenTester.getDom().getSurefireConfigurationNode().getFirstChild()
                .getNextSibling().getFirstChild().getTextContent());
    }

    @Test
    @Ignore
    public void shouldRunTests() throws MavenException, IOException, Exception {
        MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setExcludes(new String[] { "**/HelloWorldTest.java" });

        mavenTester.configureSystem();

        MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    @Ignore
    public void shouldRunTestsParameter_BaseDirectory_skipTests() throws Exception {
        MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setSkipTests(true);

        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        assertEquals(String.valueOf(mavenTester.getBaseDirectory()), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getTextContent());

        assertEquals(String.valueOf(mavenTester.isSkipTests()), mavenTester.getDom().getSurefireConfigurationNode()
                .getFirstChild().getNextSibling().getTextContent());

        MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

}
