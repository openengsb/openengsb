package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

public class PushVersionTest extends MojoPreparation {

    @Before
    public void buildInvocationCommand() throws Exception {
        prepareGoal("pushVersion");
    }

    @Test
    public void pushVersionOfExampleProject_ShouldPass() throws Exception {
        File pomFile = null;
        try {
            File pomBeforePush = new File("src/test/resources/pushVersion/beforePushVersion.xml");
            pomFile = new File("src/test/resources/pushVersion/pom.xml");
            FileUtils.copyFile(pomBeforePush, pomFile);
            int result = Tools.executeProcess(
                    Arrays.asList(new String[] { mvnCommand, "-e", invocation, "-DdevelopmentVersion=2.0-SNAPSHOT" }),
                    new File("src/test/resources/pushVersion"), true);
            assertEquals(0, result);
            File pomAfterPush = new File("src/test/resources/pushVersion/afterPushVersion.xml");
            assertEquals(FileUtils.readFileToString(pomAfterPush), FileUtils.readFileToString(pomFile));
        } finally {
            FileUtils.deleteQuietly(pomFile);
        }
    }

}
