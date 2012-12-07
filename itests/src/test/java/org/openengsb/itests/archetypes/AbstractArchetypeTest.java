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

package org.openengsb.itests.archetypes;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractArchetypeTest {
    // testing setup
    // should not me modified
    protected static final String DEFAULT_TEST_GROUP_ID = "archetype.test";
    protected static final String DEFAULT_TEST_ARTIFACT_ID = "archetype-test";
    protected static final String DEFAULT_TEST_VERSION = "1.0.0-SNAPSHOT";
    
    protected static final File TEST_ROOT = new File("target/test-classes");
    
    protected Properties systemProperties = new Properties(System.getProperties());
    
    
    @Before
    public final void setUp() throws VerificationException, IOException {
        // add default project data
        systemProperties.put("groupId", DEFAULT_TEST_GROUP_ID);
        systemProperties.put("artifactId", DEFAULT_TEST_ARTIFACT_ID);
        systemProperties.put("version", DEFAULT_TEST_VERSION);
//        systemProperties.put("interactiveMode", "false");
        
        // default project data may be overwritten here
        addArchetypeData(systemProperties);
        
        // need to make sure old test artifacts that have been
        // created are being deleted since this can lead to
        // unstable test behavior
        Verifier verifier = new Verifier(TEST_ROOT.getAbsolutePath());
        
        verifier.deleteArtifact(
            systemProperties.getProperty("groupId", DEFAULT_TEST_GROUP_ID),
            systemProperties.getProperty("artifactId", DEFAULT_TEST_ARTIFACT_ID),
            systemProperties.getProperty("version", DEFAULT_TEST_VERSION),
            null);
        verifier.deleteDirectory(systemProperties.getProperty("artifactId", DEFAULT_TEST_ARTIFACT_ID));
    }
    
    /* The data of the archetype to test are added here.
    Data needed are "archetypeGroupId", "archetypeArtifactId"
    and "archetypeVersion". */ 
    protected abstract void addArchetypeData(Properties properties);
    
    @Test
    public void testArchetype_shouldSucceed() throws VerificationException {
        // generates a test project for the given archetype
        Verifier verifier = new Verifier(TEST_ROOT.getAbsolutePath());
        
        verifier.setSystemProperties(systemProperties);
        verifier.setAutoclean(false);
        verifier.executeGoal("archetype:generate");
        verifier.verifyErrorFreeLog();
        
        // calls method to apply certain changes to the generated
        // project (changing imports, etc)
        try {
            applyProjectModifications();
        }
        catch (Exception e) {
            throw new VerificationException("Error applying project modifications", e);
        }
        
        // attempts to perform 'mvn compile' on the generated
        // archetype to verify it's working without errors
        verifier = new Verifier(
            TEST_ROOT.getAbsolutePath()
            + File.separatorChar
            + systemProperties.getProperty("artifactId", DEFAULT_TEST_ARTIFACT_ID));
        
        verifier.setAutoclean(false);
        verifier.executeGoal("compile");
        verifier.verifyErrorFreeLog();
    }
    
    // gets called after the test project has been 
    // generated and before 'mvn compile' is run
    protected abstract void applyProjectModifications() throws Exception;
}
