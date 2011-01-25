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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.base.AbstractOpenengsbMojo;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.OpenEngSBJavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

/**
 * Equivalent to execute karaf or karaf.bat per hand after build by mvn clean install in a (typically) assembly
 * directory.
 *
 * @goal provision
 *
 * @inheritedByDefault false
 *
 * @requiresProject true
 */
public class Provision extends AbstractOpenengsbMojo {

    private static final String RUNNER = "target/runner/";

    /**
     * This setting should be done in the one of the assembly folders and have to point to the final directory where the
     * karaf system, etc configs and so on consist.
     *
     * @parameter expression="${provisionPathUnix}"
     */
    private String provisionArchivePathUnix;

    /**
     * The path to the executable in the unix archive file
     *
     * @parameter expression="${provisionExecutionPathUnix}"
     */
    private String provisionExecutionPathUnix;

    /**
     * Sometimes it's required that some executable files, provided in {@link #provisionExecutionPathUnix} execute other
     * files which have to made executable to work correctly on themselves. Those files should be specified here.
     *
     * @parameter expression="${additionalRequiredExecutionPathUnix}"
     */
    private String[] additionalRequiredExecutionPathUnix;

    /**
     * This setting should be done in the one of the assembly folders and have to point to the final directory where the
     * karaf system, etc configs and so on consist.
     *
     * @parameter expression="${provisionPathWindows}"
     */
    private String provisionArchivePathWindows;

    /**
     * The path to the executable in the windows archive file
     *
     * @parameter expression="${provisionExecutionPathWindows}"
     */
    private String provisionExecutionPathWindows;

    /**
     * Sometimes it's required that some executable files, provided in {@link #provisionExecutionPathWindows} execute
     * other files which have to made executable to work correctly on themselves. Those files should be specified here.
     *
     * @parameter expression="${additionalRequiredExecutionPathWindows}"
     */
    private String[] additionalRequiredExecutionPathWindows;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (provisionArchivePathWindows != null && provisionExecutionPathWindows != null
                && provisionArchivePathUnix != null && provisionExecutionPathUnix != null) {
            CommandLineBuilder command = new CommandLineBuilder();
            Map<String, String> environment = new HashMap<String, String>();
            environment.put("KARAF_DEBUG", "true");
            if (System.getProperty("os.name").startsWith("Windows")) {
                extractWindowsArchive();
                createExecutableCommand(command, provisionExecutionPathWindows);
                makeAdditionalRequiredFilesExecutable(additionalRequiredExecutionPathWindows);
                environment.put("JAVA_OPTS", "-Djline.terminal=jline.UnsupportedTerminal");
            } else {
                extractUnixArchive();
                createExecutableCommand(command, provisionExecutionPathUnix);
                makeAdditionalRequiredFilesExecutable(additionalRequiredExecutionPathUnix);
            }
            executePlatform(command, environment);
        }
    }

    private void executePlatform(CommandLineBuilder command, Map<String, String> environment)
        throws MojoFailureException {
        try {
            new OpenEngSBJavaRunner(command, environment).exec();
        } catch (PlatformException e) {
            throw new MojoFailureException(e, e.getMessage(), e.getStackTrace().toString());
        }
    }

    private void createExecutableCommand(CommandLineBuilder command, String executablePath) {
        File executable = new File(RUNNER + executablePath);
        executable.setExecutable(true);
        command.append(executable.getAbsolutePath());
    }

    private void makeAdditionalRequiredFilesExecutable(String[] additionalExecutionPath) {
        if (additionalExecutionPath == null || additionalExecutionPath.length == 0) {
            return;
        }
        for (String additionalPath : additionalExecutionPath) {
            new File(RUNNER + additionalPath).setExecutable(true);
        }
    }

    private void extractUnixArchive() throws MojoFailureException {
        try {
            extract(new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(
                provisionArchivePathUnix))), new File(RUNNER));
        } catch (FileNotFoundException e) {
            throw new MojoFailureException(e, "Provision file for UNIX could not be found (" + provisionArchivePathUnix
                    + ")", e.getMessage());
        } catch (IOException e) {
            throw new MojoFailureException(e, "Provision file for UNIX could not be found (" + provisionArchivePathUnix
                    + ")", e.getMessage());
        }
    }

    private void extractWindowsArchive() throws MojoFailureException {
        try {
            extract(new ZipArchiveInputStream(new FileInputStream(provisionArchivePathWindows)), new File(
                RUNNER));
        } catch (FileNotFoundException e) {
            throw new MojoFailureException(e, "Provision file for WINDOWS could not be found ("
                    + provisionArchivePathWindows + ")", e.getMessage());
        } catch (IOException e) {
            throw new MojoFailureException(e, "Provision file for WINDOWS could not be found ("
                    + provisionArchivePathWindows + ")", e.getMessage());
        }
    }

    private void extract(ArchiveInputStream is, File targetDir) throws IOException {
        try {
            if (targetDir.exists()) {
                FileUtils.forceDelete(targetDir);
            }
            targetDir.mkdirs();
            ArchiveEntry entry = is.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                name = name.substring(name.indexOf("/") + 1);
                File file = new File(targetDir, name);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
                entry = is.getNextEntry();
            }
        } finally {
            is.close();
        }
    }

}
