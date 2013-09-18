/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.loom.csharp.comon.wsdltodll;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling.FileComparer;

/**
 * Goal which creates a DLL from a WSDL file.
 * 
 * @goal run
 * @phase process-sources
 */
public class WsdlToDll extends AbstractMojo {

    private static final String LINUX_WSDL_SERVER_PARAMETER = "-server";
    private static final String LINUX_WSDL_COMMAND = "wsdl2";
    private static final String LINUX_CSC_COMMAND = "mcs";
    private static final String WINDWOS_WSDL_SERVER_PARAMETER = "/serverInterface";
    private static final String WINDOWS_WSDL_COMMAND = "wsdl.exe";
    private static final String WINDOWS_CSC_COMMAND = "csc.exe";
    private static final String NUGET_COMMAND = "nuget";

    private String cscCommand;
    private String wsdlCommand;
    private String serverParameter;
    private Boolean windowsModus;

    /**
     * Owner of the Nuget package
     * 
     * @parameter
     * @required default-Value="OpenEngSB"
     */
    private String owner;
    /**
     * Author of the nuget package.
     * 
     * @parameter
     * @required default-Value="OpenEngSB
     */
    private String author = "OpenEngSB";
    /**
     * The Url of the project
     * 
     * @parameter
     * @optional default-Value=null
     */
    private String projectUrl;
    /**
     * Url to the license
     * 
     * @parameter
     * @optional Default-Value=null
     */
    private String licenseUrl;
    /**
     * Url to the icon
     * 
     * @parameter
     * @optional Default-Value=null
     */
    private String iconUrl;
    /**
     * Location of the file.
     * 
     * @parameter
     * @required Default-Value=false
     */
    private boolean requireLicenseAcceptance;
    /**
     * Location of the file.
     * 
     * @parameter
     * @required Default-Value=""
     */
    private String releaseNotes;
    /**
     * Location of the file.
     * 
     * @parameter
     * @required Default-Value=OpenEngSB 2012
     */
    private String copyright;

    /**
     * List of default pathes where to search for the installation of the .net framework.
     */
    private static final String[] DEFAULT_WSDL_PATHS = new String[]{
        System.getenv("ProgramFiles(x86)") + "\\Microsoft SDKs\\Windows\\",
        System.getenv("ProgramFiles") + "\\Microsoft SDKs\\Windows\\" };

    private static final String[] DEFAULT_CSC_PATHS = new String[]{
        System.getenv("windir") + "\\Microsoft.NET\\Framework64\\",
        System.getenv("windir") + "\\Microsoft.NET\\Framework\\" };

    /**
     * Location of the file.
     * 
     * @parameter default-Value=null
     * @required
     */
    private File outputDirectory;
    /**
     * Location of the wsdl.exe command
     * 
     * @parameter default-Value=null
     */
    private File wsdlExeFolderLocation;
    /**
     * Location of the csc command.
     * 
     * @parameter default-Value=null
     */
    private File cscFolderLocation;
    /**
     * Nuget folder
     * 
     * @parameter default-Value=null
     */
    private String nugetFolder;
    /**
     * Create the Nuget Packages
     * 
     * @parameter default-Value=false
     */
    private boolean generateNugetPackage;
    /**
     * Location of the wsdl file
     * 
     * @parameter
     * @required
     */
    private List<String> wsdlLocations;

    /**
     * Namespace of the WSDL file. This should be the namespace in which a domain should be located.
     * 
     * @parameter
     * @required
     */
    private String namespace;

    /**
     * Version that should be written to the resulting DLL
     * 
     * @parameter
     * @required
     */
    private String targetVersion;

    private String nugetLib;

    private List<String> cspath = new ArrayList<String>();

    /**
     * Find and executes the commands wsdl.exe and csc.exe
     */
    @Override
    public void execute() throws MojoExecutionException {
        checkParameters();
        windowsModus = isWindows();
        if (windowsModus) {
            setWindowsVariables();
        } else {
            setLinuxVariables();
        }
        createDllFromWsdl();
    }

    private void checkParameters() throws MojoExecutionException {
        if (generateNugetPackage && nugetFolder == null) {
            throw new MojoExecutionException(
                "The nugetFolder has to be defined when nuget packaging is enabled");
        }
    }

    private void setWindowsVariables() {
        cscCommand = WINDOWS_CSC_COMMAND;
        wsdlCommand = WINDOWS_WSDL_COMMAND;
        serverParameter = WINDWOS_WSDL_SERVER_PARAMETER;

    }

    private void setLinuxVariables() {
        cscCommand = LINUX_CSC_COMMAND;
        wsdlCommand = LINUX_WSDL_COMMAND;
        serverParameter = LINUX_WSDL_SERVER_PARAMETER;
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        getLog().info("Operation system:" + os);
        return os.toUpperCase().contains("WINDOWS");
    }

    /**
     * Windows mode for maven execution
     * 
     * @throws MojoExecutionException
     */
    private void createDllFromWsdl() throws MojoExecutionException {
        getLog().info("Execute WSDl to cs command");
        wsdlCommand();
        getLog().info("Execute cs to dll command");
        cscCommand();
        if (generateNugetPackage) {
            if (isLinux()) {
                throw new MojoExecutionException(
                    "At this point, mono and nuget does not work so well together."
                            + "Please execute the plugin with nuget under Windows");
            }
            nugetLib = nugetFolder + "lib";
            getLog().info("Create Nuget folder structure");
            createNugetStructure();
            getLog().info("Copy the dlls to the nuget structure");
            copyFilesToNuget();
            getLog().info("Generate " + namespace + " .nuspec");
            generateNugetPackedFile();
            getLog().info("Pack .nuspec to a nuget package");
            nugetPackCommand();
        }
    }

    private String findWsdlCommand() throws MojoExecutionException {
        if (isLinux()) {
            if (isMonoCommandInstalled(wsdlCommand)) {
                return wsdlCommand;
            }
            throw new MojoExecutionException("The program '" + wsdlCommand
                    + "' is currently not installed.  You can install it by typing: "
                    + "sudo apt-get install mono-devel");
        }
        if (wsdlExeFolderLocation != null) {
            return wsdlExeFolderLocation.getAbsolutePath();
        }
        for (File sdk : findAllInstalledSDKs(DEFAULT_WSDL_PATHS)) {
            File bindir = new File(sdk, "bin");
            File wsdlFile = new File(bindir, wsdlCommand);
            if (wsdlFile.exists()) {
                wsdlExeFolderLocation = wsdlFile;
                return wsdlFile.getAbsolutePath();
            }
        }
        throw new MojoExecutionException("unable to find " + wsdlCommand
                + " in paths " + Arrays.toString(DEFAULT_WSDL_PATHS) + "\n "
                + "You can specify the path manually by adding the argument \n"
                + " -DwsdlExeFolderLocation=\"C:\\path\\to\\wsdl.exe\"");
    }

    private boolean isMonoCommandInstalled(String command) {
        List<String> commandList = new LinkedList<String>();
        commandList.add(command);
        return isMonoCommandInstalled(commandList);
    }

    private boolean isMonoCommandInstalled(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.command(command);
        try {
            executeACommand(builder.start());
        } catch (MojoExecutionException | IOException | InterruptedException e) {
            return false;
        }
        return true;
    }

    private String findCscCommand() throws MojoExecutionException {
        if (isLinux()) {
            List<String> commands = new LinkedList<String>();
            commands.add(cscCommand);
            commands.add("/help");
            if (isMonoCommandInstalled(commands)) {
                return cscCommand;
            }
            throw new MojoExecutionException("The program '" + cscCommand
                    + "' is currently not installed.  You can install it by typing: "
                    + "sudo apt-get install mono-" + cscCommand);
        }

        if (cscFolderLocation != null) {
            return cscFolderLocation.getAbsolutePath();
        }
        for (File sdk : findAllInstalledSDKs(DEFAULT_CSC_PATHS)) {
            File file = new File(sdk, cscCommand);
            getLog().info(
                "Trying to find " + cscCommand + " in "
                        + sdk.getAbsolutePath());
            if (file.exists()) {
                cscFolderLocation = file.getAbsoluteFile();
                return file.getAbsolutePath();
            }
        }
        throw new MojoExecutionException("unable to find " + cscCommand
                + " in paths " + Arrays.toString(DEFAULT_CSC_PATHS) + "\n "
                + "You can specify the path manually by adding the argument \n"
                + " -DcscFolderLocation=\"C:\\path\\to\\csc.exe\"");
    }

    private boolean isLinux() {
        return !windowsModus;
    }

    private Collection<File> findAllInstalledSDKs(String[] paths) {
        Collection<File> result = new LinkedList<File>();
        for (String s : paths) {
            File[] findAllInstalledSDKs = findInstalledSDKs(s);
            result.addAll(Arrays.asList(findAllInstalledSDKs));
        }
        return result;
    }

    private File[] findInstalledSDKs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new File[0];
        }
        File[] installedSDKs = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        Arrays.sort(installedSDKs, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
        return installedSDKs;
    }

    /**
     * Search for the wsdl command and execute it when it is found
     */
    private void wsdlCommand() throws MojoExecutionException {
        String cmd = findWsdlCommand();
        int i = 0;
        for (String location : wsdlLocations) {
            String outputFilename = new File(outputDirectory, namespace + (i++)
                    + ".cs").getAbsolutePath();
            String[] command = new String[]{ cmd, serverParameter,
                "/n:" + namespace, location, "/out:" + outputFilename };
            ProcessBuilder builder = new ProcessBuilder();
            builder.redirectErrorStream(true);
            builder.command(command);

            try {
                executeACommand(builder.start());
            } catch (IOException | InterruptedException e) {
                throw new MojoExecutionException(
                    "Error, while executing command: "
                            + Arrays.toString(command) + "\n", e);
            }
            cspath.add(outputFilename);
        }
        try {
            FileComparer.removeSimilaritiesAndSaveFiles(cspath, getLog(),
                windowsModus);
        } catch (IOException e) {
            throw new MojoExecutionException(
                "It was not possible, to remove similarities form the files",
                e);
        }

    }

    /**
     * Search for the csc command and execute it when it is found
     */
    private void cscCommand() throws MojoExecutionException {
        generateAssemblyInfo();
        String cscPath = findCscCommand();
        List<String> commandList = new LinkedList<String>(cspath);
        commandList.add(0, cscPath);
        commandList.add(1, "/target:library");
        commandList.add(2, "/out:" + namespace + ".dll");
        if (isLinux()) {
            commandList.add(3, "/reference:System.Web.Services");
        }
        String[] command = commandList.toArray(new String[commandList.size()]);
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.directory(outputDirectory);
        builder.command(command);
        try {
            executeACommand(builder.start());
        } catch (InterruptedException | IOException e) {
            throw new MojoExecutionException("Error, while executing command: "
                    + Arrays.toString(command) + "\n", e);
        }
    }

    /**
     * Generate the nuget structe (for example a lib folder)
     */
    private void createNugetStructure() {
        (new File(nugetLib)).mkdirs();
    }

    private void copyFilesToNuget() throws MojoExecutionException {
        File folder = outputDirectory;
        for (File file : folder.listFiles()) {
            if (file.getAbsoluteFile().toPath().toString().endsWith(".dll")) {
                try {
                    getLog().info(
                        "COPY FILE"
                                + (new File(nugetLib)).getAbsoluteFile()
                                    .toPath());
                    Files.copy(file.getAbsoluteFile().toPath(), (new File(
                        nugetLib + "/" + file.getName()))
                        .getAbsoluteFile().toPath(), REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new MojoExecutionException(ex.getMessage());
                }
            }
        }

    }

    /**
     * Execute nuget command to pack the .nuspec
     */
    private void nugetPackCommand() throws MojoExecutionException {
        List<String> commandList = new LinkedList<String>();
        commandList.add(0, NUGET_COMMAND);
        commandList.add(1, "pack");
        String[] command = commandList.toArray(new String[commandList.size()]);
        ProcessBuilder builder = new ProcessBuilder();
        builder.redirectErrorStream(true);
        builder.directory(new File(nugetFolder));
        builder.command(command);
        try {
            executeACommand(builder.start());
        } catch (InterruptedException | IOException e) {
            throw new MojoExecutionException("Error, while executing command: "
                    + Arrays.toString(command) + "\n", e);
        }
    }

    private void generateAssemblyInfo() throws MojoExecutionException {
        StringBuilder assemblyInfoBuilder = new StringBuilder();
        assemblyInfoBuilder.append("using System.Reflection;\n");
        assemblyInfoBuilder.append("[assembly: AssemblyTitle(\"")
            .append(namespace).append("\")]\n");
        assemblyInfoBuilder.append("[assembly: AssemblyProduct(\"")
            .append(namespace).append("\")]\n");
        assemblyInfoBuilder.append("[assembly: AssemblyCompany(\"")
            .append(owner).append("\")]\n");

        assemblyInfoBuilder.append("[assembly: AssemblyDescription(\"")
            .append(namespace + "_domain_dll").append("\")]\n");
        assemblyInfoBuilder.append("[assembly: AssemblyCopyright(\"")
            .append("Copyright @ " + owner).append("\")]\n");

        String truncatedVersion = targetVersion.replaceAll("-.*", "");
        assemblyInfoBuilder.append("[assembly: AssemblyVersion(\"")
            .append(truncatedVersion).append("\")]\n");
        assemblyInfoBuilder.append("[assembly: AssemblyFileVersion(\"")
            .append(truncatedVersion).append("\")]\n");
        assemblyInfoBuilder
            .append("[assembly: AssemblyInformationalVersion(\"")
            .append(targetVersion).append("\")]\n");

        File assemblyInfo = new File(outputDirectory, "AssemblyInfo.cs");
        FileWriter writer = null;
        try {
            writer = new FileWriter(assemblyInfo);
            writer.write(assemblyInfoBuilder.toString());
        } catch (IOException e) {
            throw new MojoExecutionException(
                "unable to write generated AssemblyInfo.cs", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore that
                }
            }
        }
        cspath.add(assemblyInfo.getAbsolutePath());
    }

    private void generateNugetPackedFile() throws MojoExecutionException {
        StringBuilder assemblyInfoBuilder = new StringBuilder();
        assemblyInfoBuilder.append("<?xml version=\"1.0\"?>\n");
        assemblyInfoBuilder.append("<package >\n");
        assemblyInfoBuilder.append("  <metadata>\n");
        assemblyInfoBuilder.append("    <id>" + namespace + "</id>\n");

        assemblyInfoBuilder.append("    <version>" + targetVersion
                + "</version>\n");
        assemblyInfoBuilder.append("    <authors>" + author + "</authors>\n");
        assemblyInfoBuilder.append("    <owners>" + owner + "</owners>\n");
        if (licenseUrl != null) {
            assemblyInfoBuilder.append("    <licenseUrl>" + licenseUrl
                    + "</licenseUrl>\n");
        }
        if (projectUrl != null) {
            assemblyInfoBuilder.append("    <projectUrl>" + projectUrl
                    + "</projectUrl>\n");
        }
        if (iconUrl != null) {
            assemblyInfoBuilder.append("    <iconUrl>" + iconUrl
                    + "</iconUrl>\n");
        }
        assemblyInfoBuilder.append("    <requireLicenseAcceptance>"
                + requireLicenseAcceptance + "</requireLicenseAcceptance>\n");
        assemblyInfoBuilder.append("    <description>" + namespace + " dll"
                + "</description>\n");
        assemblyInfoBuilder.append("    <releaseNotes>" + releaseNotes
                + "</releaseNotes>\n");
        assemblyInfoBuilder.append("    <copyright>" + copyright
                + "</copyright>\n");
        assemblyInfoBuilder.append("  </metadata>\n");
        assemblyInfoBuilder.append("</package >\n");

        File assemblyInfo = new File(nugetFolder, namespace + ".nuspec");
        FileWriter writer = null;
        try {
            writer = new FileWriter(assemblyInfo);
            writer.write(assemblyInfoBuilder.toString());
        } catch (IOException e) {
            throw new MojoExecutionException(
                "unable to write generated Nugetpacketfile.cs", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ignore that
                }
            }
        }
        cspath.add(assemblyInfo.getAbsolutePath());
    }

    private void executeACommand(Process child) throws IOException,
        MojoExecutionException, InterruptedException {
        BufferedReader brout = new BufferedReader(new InputStreamReader(
            child.getInputStream()));
        String error = "";
        String tmpLine;
        String input = "";
        String last = "";
        boolean isAWarningLines = false;
        while ((tmpLine = brout.readLine()) != null) {
            if (isNotALineToFilter(tmpLine)) {
                if (isAWarningLines) {
                    if (isNotAMonoWsdlWarningLine(tmpLine)) {
                        isAWarningLines = false;
                    } else {
                        continue;
                    }
                }
                input += tmpLine + "\n";
            } else {
                isAWarningLines = isLinux();
            }
            last = tmpLine;
        }
        getLog().info("Waiting until process is finished");

        child.waitFor();
        if (child.exitValue() > 0) {
            throw new MojoExecutionException(input);
        }

        // Because the wsdl.exe can not be executed in a outputDirectory, the
        // file has to be moved to the corresponding
        // folder
        if (last.contains("'") && last.contains(".cs")) {
            String filepath = last.split("'")[1];
            File file = new File(filepath);
            boolean moved = file.renameTo(new File(outputDirectory, file
                .getName()));
            if (!moved) {
                throw new MojoExecutionException("Unable to move file: "
                        + file.getAbsolutePath());
            }
            input += "Moving file " + file.getName() + " to " + cspath + "\n";
            getLog().info(input);
        }
        if (child.waitFor() != 0) {
            throw new MojoExecutionException(error);
        }
    }

    /**
     * Checks if the output string from the command prompt is a line that can be ignored or not
     * 
     * @param line
     * @return
     */
    private boolean isNotAMonoWsdlWarningLine(String line) {
        return !line.startsWith(" ") && !line.equals("");
    }

    /**
     * Checks if the line is not a line to filter
     * 
     * @param line
     * @return
     */
    private boolean isNotALineToFilter(String line) {
        return !(line.startsWith("Schema validation error:")
                || line.startsWith("Warning:") || line.contains("warnings"));
    }
}
