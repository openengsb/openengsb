package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

public class LicenseCheckTest extends AbstractMojoTest {

    private static final String goal = "licenseCheck";

    @BeforeClass
    public static void buildInvocationCommand() throws Exception {
        prepare(goal);
    }

    @Test
    public void licenseCheckNoLicenseHeader_mojoShouldFail() throws Exception {
        int result =
            Tools.executeProcess(new String[]{ "mvn", "-e",
                invocation },
                "src/test/resources/licensecheck/fail", false);

        assertEquals(1, result);
    }

    @Test
    public void licenseCheckHeaderAvailable_mojoShouldPass() throws Exception {
        int result =
            Tools.executeProcess(new String[]{ "mvn", "-e",
                invocation },
                "src/test/resources/licensecheck/pass", false);
        assertEquals(0, result);
    }

    @Test
    public void writeHeaderTest() throws IOException {
        String headerStr = Tools.getTxtFileContent(ClassLoader.getSystemResourceAsStream("licenseCheck/header.txt"));
        File generatedFile = Tools.generateTmpFile(headerStr, ".txt");
        assertEquals(headerStr, Tools.getTxtFileContent(new FileInputStream(generatedFile)));
        assertTrue(generatedFile.delete());
    }

}
