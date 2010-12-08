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

    private List<String> goals;
    private Properties userproperties;

    private boolean archetypeCatalogLocalOnly = false;

    // INPUTS

    private String domain_name;
    private String version;
    private String project_name;

    private String groupId;
    private String artifactId;

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

        validateIfExecutionIsAllowed();

        initDefaults();
        readUserInput();
        initializeMavenExecutionProperties();

        executeMaven();

        Tools.renameArtifactFolderAndUpdateParentPom(artifactId, domain_name);

        System.out.println("DON'T FORGET TO ADD THE DOMAIN TO YOUR RELEASE/ASSEMBLY PROJECT!");

    }

    private void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
    }

    private void initDefaults() {
        // version should be the same as the version of the OpenEngSB
        default_version = getProject().getVersion();
    }

    private void readUserInput() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Use only local archetypeCatalog? (y/n): ");
        String in = sc.nextLine();
        if (in.equalsIgnoreCase("y")) {
            archetypeCatalogLocalOnly = true;
        }

        domain_name = Tools.readValue(sc, "Domain Name", DEFAULT_DOMAIN);
        version = Tools.readValue(sc, "Version", default_version);
        project_name = Tools.readValue(sc,
            "Prefix for project names",
            String.format("%s%s", DEFAULT_DOMAINNAME_PREFIX,
                Tools.capitalizeFirst(domain_name)));

        groupId = String.format("%s%s", DOMAIN_GROUPIDPREFIX, domain_name);
        artifactId = String.format("%s%s", DOMAIN_ARTIFACTIDPREFIX, domain_name);
    }

    private void initializeMavenExecutionProperties() {
        goals = Arrays
            .asList(new String[]{ "archetype:generate" });

        userproperties = new Properties();

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
    }

    private void executeMaven() throws MojoExecutionException {
        getNewMavenExecutor().setRecursive(true).execute(this, goals, null, null, userproperties,
            getProject(), getSession(), getMaven());
    }

}
