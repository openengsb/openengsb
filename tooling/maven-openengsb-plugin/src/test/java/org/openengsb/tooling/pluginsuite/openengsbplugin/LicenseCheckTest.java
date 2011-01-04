/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
