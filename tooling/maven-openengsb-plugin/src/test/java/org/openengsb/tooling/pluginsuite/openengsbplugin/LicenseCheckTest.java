package org.openengsb.tooling.pluginsuite.openengsbplugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

public class LicenseCheckTest {

    @Test
    public void licenseCheckNoLicenseHeader_mojoShouldFail() throws Exception {
        int result =
            executeProcess(new String[]{ "mvn", "-e",
                "org.openengsb.tooling.pluginsuite:maven-openengsb-plugin:1.1.0-SNAPSHOT:licenseCheck" },
                "src/test/resources/licensecheck/fail", true);
        // TODO change separator Char
        // TODO remove hardcoding of maven command (version info etc) -> read from pom, or read from plugin
        // configuration ?
        assertEquals(1, result);
    }

    @Test
    public void licenseCheckHeaderAvailable_mojoShouldPass() throws Exception {
        int result =
            executeProcess(new String[]{ "mvn", "-e",
                "org.openengsb.tooling.pluginsuite:maven-openengsb-plugin:1.1.0-SNAPSHOT:licenseCheck" },
                "src/test/resources/licensecheck/pass", true);
        assertEquals(0, result);
    }

    @Test
    public void writeHeaderTest() throws IOException {
        String headerStr = Tools.getTxtFileContent(ClassLoader.getSystemResourceAsStream("licenseCheck/header.txt"));
        File generatedFile = Tools.generateTmpFile(headerStr, ".txt");
        assertEquals(headerStr, Tools.getTxtFileContent(new FileInputStream(generatedFile)));
        assertTrue(generatedFile.delete());
    }

    private int executeProcess(String[] command, File targetDirectory, boolean printOutput) throws IOException,
        InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // TODO use URI!!!!
        processBuilder.directory(targetDirectory);
        Process p = processBuilder.start();
        if (printOutput) {
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        p.waitFor();
        return p.exitValue();
    }

    private int executeProcess(String[] command, String targetDirectory, boolean printOutput) throws IOException,
        InterruptedException {
        return executeProcess(command, new File(targetDirectory), printOutput);
    }

}
