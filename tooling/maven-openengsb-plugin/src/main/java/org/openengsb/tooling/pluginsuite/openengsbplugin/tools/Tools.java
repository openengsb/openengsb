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

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;

public abstract class Tools {

    public static String capitalizeFirst(String st) {
        if (st == null) {
            return null;
        } else if (st.matches("[\\s]*")) {
            return st;
        } else if (st.length() == 1) {
            return st.toUpperCase();
        } else {
            return st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
        }
    }

    public static void replaceInFile(File f, String pattern, String replacement) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(f));

        String str = "";
        String line = "";

        while ((line = br.readLine()) != null) {
            str += line + "\n";
        }

        br.close();

        str = str.replaceAll(pattern, replacement);

        FileWriter fw = new FileWriter(f);
        fw.write(str);
        fw.close();

    }

    public static void renameSubmoduleInPom(String oldStr, String newStr) throws MojoExecutionException {
        try {
            File pomFile = new File("pom.xml");
            if (pomFile.exists()) {
                Tools.replaceInFile(pomFile, String.format("<module>%s</module>", oldStr),
                    String.format("<module>%s</module>", newStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Couldn't modifiy module entry in pom file!");
        }
    }

    public static String readValue(Scanner sc, String name, String defaultvalue) {
        System.out.print(String.format("%s [%s]: ", name, defaultvalue));
        String line = sc.nextLine();
        if (line == null || line.matches("[\\s]*")) {
            return defaultvalue;
        }
        return line;
    }

    public static void renameArtifactFolderAndUpdateParentPom(String oldStr, String newStr) throws MojoExecutionException {
        File from = new File(oldStr);
        System.out.println(String.format("\"%s\" exists: %s", oldStr, from.exists()));
        if (from.exists()) {
            System.out.println(String.format("Trying to rename to: \"%s\"", newStr));
            File to = new File(newStr);
            if (!to.exists()) {
                from.renameTo(to);
                System.out.println("renamed successfully");
                Tools.renameSubmoduleInPom(oldStr, newStr);
            } else {
                throw new MojoExecutionException("Couldn't rename: name clash!");
            }
        } else {
            throw new MojoExecutionException("Artifact wasn't created as expected!");
        }
    }

}
