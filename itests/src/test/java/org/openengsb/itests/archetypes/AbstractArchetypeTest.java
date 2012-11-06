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
    protected static final String TEST_GROUP_ID = "archetype.test";
    protected static final String TEST_ARTIFACT_ID = "archetype-test";
    protected static final String TEST_VERSION = "1.0-SNAPSHOT";
    
    protected static final File TEST_ROOT = new File("target/test-classes");
    
    protected Properties systemProperties = new Properties(System.getProperties());
    
    
    @Before
    public final void setUp() throws VerificationException, IOException {
        addArchetypeData(systemProperties);
        
        systemProperties.put("groupId", TEST_GROUP_ID);
        systemProperties.put("artifactId", TEST_ARTIFACT_ID);
        systemProperties.put("version", TEST_VERSION);
        systemProperties.put("interactiveMode", "false");
        
        // need to make sure test artifacts that have been
        // created are being deleted since this can lead to
        // unstable test behavior
        Verifier verifier = new Verifier(TEST_ROOT.getAbsolutePath());
        
        verifier.deleteArtifact(TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, null);
        verifier.deleteDirectory(TEST_ARTIFACT_ID);
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
        verifier = new Verifier(TEST_ROOT.getAbsolutePath() + File.separatorChar + TEST_ARTIFACT_ID);
        
        verifier.setAutoclean(false);
        verifier.executeGoal("compile");
        verifier.verifyErrorFreeLog();
    }
    
    // gets called after the test project has been 
    // generated and before 'mvn compile' is run
    protected abstract void applyProjectModifications() throws Exception;
}
