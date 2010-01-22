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
package org.openengsb.maven.common.test.unit;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.maven.common.MavenConnector;
import org.openengsb.maven.common.MavenResult;

@Ignore("tests do not run on hudson - locally they should run without any problems")
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
        Assert.assertEquals(MavenResult.SUCCESS, result.getResult());
    }

    @Test
    public void testRunTestGoalError() {
        File basedir = new File("target/test-classes/test-unit-fail");
        MavenResult result = runGoals(basedir, new String[] { "test" });
        Assert.assertEquals(MavenResult.FAILURE, result.getResult());
    }

    @Test
    public void testRunBuildGoalSuccess() {
        File basedir = new File("target/test-classes/build-test");
        MavenResult result = runGoals(basedir, new String[] { "package" });
        Assert.assertEquals(MavenResult.SUCCESS, result.getResult());
    }

    @Test
    public void testRunDeployGoalSuccess() {
        File basedir = new File("target/test-classes/deploy-test");
        MavenResult result = runGoals(basedir, new String[] { "deploy" });
        Assert.assertEquals(MavenResult.SUCCESS, result.getResult());
    }

    @Test
    public void testInvalidPom() {
        File basedir = new File("target/test-classes/test-invalid-pom");
        MavenResult result = runGoals(basedir, new String[] { "package" });
        Assert.assertEquals(MavenResult.ERROR, result.getResult());
    }

    private MavenResult runGoals(File basedir, String[] goals) {
        MavenConnector maven = new MavenConnector(basedir, goals, executionRequestProperties);
        return maven.execute();
    }
}
