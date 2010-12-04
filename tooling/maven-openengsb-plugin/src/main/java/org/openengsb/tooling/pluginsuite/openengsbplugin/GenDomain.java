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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.Tools;

/**
 * guides through the creation of a domain for the OpenEngSB via the domain archetype
 * 
 * @goal genDomain
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 */
public class GenDomain extends AbstractOpenengsbMojo {

    private boolean archetypeCatalogLocalOnly = false;

    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes";
    private static final String ARCHETYPE_ARTIFACTID = "openengsb-tooling-archetypes-domain";

    private static final String DOMAIN_GROUPIDPREFIX = "org.openengsb.domain.";
    private static final String DOMAIN_ARTIFACTIDPREFIX = "openengsb-domain-";

    private static final String DEFAULT_DOMAIN = "mydomain";

    private static final String DEFAULT_DOMAINNAME_PREFIX = "OpenEngSB :: Domain :: ";

    // DYNAMIC DEFAULTS

    private String default_version;

    @Override
    public void execute() throws MojoExecutionException {

        if (!getProject().isExecutionRoot()) {
            return;
        }

        assert (getMavenExecutor() != null);

        initDefaults();

        Scanner sc = new Scanner(System.in);

        System.out.print("Use only local archetypeCatalog? (y/n): ");
        String in = sc.nextLine();
        if (in.equalsIgnoreCase("y")) {
            archetypeCatalogLocalOnly = true;
        }

        String domain_name = readValue(sc, "Domain Name", DEFAULT_DOMAIN);
        String version = readValue(sc, "Version", default_version);
        String project_name = readValue(sc,
            "Prefix for project names",
            String.format("%s%s", DEFAULT_DOMAINNAME_PREFIX,
                Tools.capitalizeFirst(domain_name)));

        String groupId = String.format("%s%s", DOMAIN_GROUPIDPREFIX, domain_name);
        String artifactId = String.format("%s%s", DOMAIN_ARTIFACTIDPREFIX, domain_name);

        List<String> goals = Arrays
            .asList(new String[]{ "archetype:generate" });

        Properties userproperties = new Properties();

        userproperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        userproperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        userproperties.put("archetypeVersion", version);
        userproperties.put("groupId", groupId);
        userproperties.put("artifactId", artifactId);
        userproperties.put("version", version);
        userproperties.put("domainName", domain_name);
        userproperties.put("implementationArtifactId", artifactId);
        userproperties.put("package", groupId);
        userproperties.put("name", project_name);
        userproperties
            .put("domainInterface", String.format("%s%s", Tools.capitalizeFirst(domain_name), "Domain"));
        userproperties.put("implementationName", project_name);

        // local archetype catalog only
        if (archetypeCatalogLocalOnly) {
            userproperties.put("archetypeCatalog", "local");
        }

        getMavenExecutor().execute(this, goals, null, null, userproperties,
            getProject(), getSession(), getMaven(), true);

        File from = new File(artifactId);
        System.out.println(String.format("\"%s\" exists: %s", artifactId, from.exists()));
        if (from.exists()) {
            System.out.println(String.format("Trying to rename to: \"%s\"", domain_name));
            File to = new File(domain_name);
            if (!to.exists()) {
                from.renameTo(to);
                System.out.println("renamed successfully");
                renameSubmoduleInPom(artifactId, domain_name);
            } else {
                throw new MojoExecutionException("Couldn't rename: name clash!");
            }
            System.out.println("DON'T FORGET TO ADD THE DOMAIN TO YOUR RELEASE/ASSEMBLY PROJECT!");
        } else {
            throw new MojoExecutionException("Artifact wasn't created as expected!");
        }

    }

    private void initDefaults() {
        // version should be the same as the version of the OpenEngSB
        default_version = getProject().getVersion();
    }

    private String readValue(Scanner sc, String name, String defaultvalue) {
        System.out.print(String.format("%s [%s]: ", name, defaultvalue));
        String line = sc.nextLine();
        if (line == null || line.matches("[\\s]*")) {
            return defaultvalue;
        }
        return line;
    }

    private void renameSubmoduleInPom(String artifactId, String domain_name) throws MojoExecutionException {
        try {
            File pomFile = new File("pom.xml");
            if (pomFile.exists()) {
                Tools.replaceInFile(pomFile, String.format("<module>%s</module>", artifactId),
                    String.format("<module>%s</module>", domain_name));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Couldn't modifiy module entry in pom file!");
        }
    }

}
