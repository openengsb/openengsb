package org.openengsb.maven.common.test.unit;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.maven.common.MavenConnector;
import org.openengsb.maven.common.MavenResult;

public class TestMavenConnector {

    private Properties executionRequestProperties;

    @Before
    public void setUp() {
        executionRequestProperties = new Properties();
    }

    @Test
    public void testRunTestGoalSuccess() {
        File basedir = new File("target/test-classes/test-unit-success");
        MavenResult result = runGoals(basedir, new String[] { "test" });
        Assert.assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    public void testRunTestGoalError() {
        File basedir = new File("target/test-classes/test-unit-fail");
        MavenResult result = runGoals(basedir, new String[] { "test" });
        Assert.assertEquals(MavenResult.FAILURE, result.getMavenOutput());
    }

    @Test
    public void testRunBuildGoalSuccess() {
        File basedir = new File("target/test-classes/build-test");
        MavenResult result = runGoals(basedir, new String[] { "package" });
        Assert.assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    public void testRunDeployGoalSuccess() {
        File basedir = new File("target/test-classes/deploy-test");
        MavenResult result = runGoals(basedir, new String[] { "deploy" });
        Assert.assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    public void testInvalidPom() {
        File basedir = new File("target/test-classes/test-invalid-pom");
        MavenResult result = runGoals(basedir, new String[] { "package" });
        Assert.assertEquals(MavenResult.ERROR, result.getMavenOutput());
    }

    private MavenResult runGoals(File basedir, String[] goals) {
        MavenConnector maven = new MavenConnector(basedir, goals, executionRequestProperties);
        return maven.execute();
    }
}
