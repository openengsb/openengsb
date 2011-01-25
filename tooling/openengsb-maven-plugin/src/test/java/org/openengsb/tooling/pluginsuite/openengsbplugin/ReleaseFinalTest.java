package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

public class ReleaseFinalTest extends AbstractReleaseTest {

    @Ignore
    @Test
    public void testFinalRelease_shouldPassAndReleaseToRepo() throws Exception {
        prepareGoal("releaseFinal");
        int result = Tools.executeProcess(Arrays.asList(new String[] { mvnCommand, "-e", invocation,
            "-DconnectionUrl=foo" }), new File("src/test/resources/release/final"), true);
        assertEquals(0, result);
    }

}
