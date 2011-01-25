package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

public class ReleaseNightlyTest extends AbstractReleaseTest {

    @Test
    public void testNightlyRelease_shouldPassAndReleaseToRepo() throws Exception {
        prepareGoal("releaseNightly");
        int result = Tools.executeProcess(Arrays.asList(new String[] { mvnCommand, "-e", invocation }), new File(
                "src/test/resources/release/snapshot"));
        assertEquals(0, result);
        File deployedDir = new File(snapshotRepo, "foo/releaseTestProject");
        assertTrue(deployedDir.exists());
    }

}
