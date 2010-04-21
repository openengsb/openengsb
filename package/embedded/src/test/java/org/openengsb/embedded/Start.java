/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.embedded;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MavenExecutionException;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.openengsb.embedded.JbiTypeChecker.JbiType;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Start {
    public static void main(String[] args) throws Exception {
        cleanup();
        FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(new String[] {
                "src/main/webapp/WEB-INF/applicationContext.xml", "src/test/resources/testContext.xml" });
        ctx.start();

        JBIContainer jbiContainer = (JBIContainer) ctx.getBean("jbi");

        Configuration configuration = new DefaultConfiguration();
        MavenEmbedder embedder = new MavenEmbedder(configuration);
        embedder.setLogger(new MavenEmbedderConsoleLogger());
        Xpp3Dom[] artifacts = getServicemixInstallers(embedder);

        for (int i = 0; i < artifacts.length; ++i) {
            File installerFile = getLocalInstallerZipPath(embedder, artifacts[i]);
            JbiType type = JbiTypeChecker.checkJbiInstallerType(installerFile);
            if (type == JbiType.SHARED_LIBRARY) {
                jbiContainer.getInstallationService().installSharedLibrary(installerFile.getAbsolutePath());
            } else if (type == JbiType.SERVICE_ASSEMBLY) {
                jbiContainer.getAdminCommandsService().installArchive(installerFile.getAbsolutePath());
            } else {
                jbiContainer.installArchive(installerFile.getAbsolutePath());
            }
        }

        System.in.read();
        jbiContainer.shutDown();
        ctx.stop();
    }

    private static void cleanup() throws IOException {
        new File("derby.log").delete();
        FileUtils.deleteDirectory("activemq-data");
        FileUtils.deleteDirectory("data");
    }

    @SuppressWarnings("unchecked")
    private static Xpp3Dom[] getServicemixInstallers(MavenEmbedder embedder) throws ProjectBuildingException,
            MavenExecutionException {
        MavenProject project = embedder.readProject(new File("pom.xml"));
        Plugin plugin = project.getPlugin("org.codehaus.mojo:dependency-maven-plugin");
        List<PluginExecution> executions = plugin.getExecutions();
        Xpp3Dom d = (Xpp3Dom) executions.get(0).getConfiguration();
        return d.getChild("artifactItems").getChildren("artifactItem");
    }

    private static File getLocalInstallerZipPath(MavenEmbedder embedder, Xpp3Dom artifact) {
        String groupId = artifact.getChild("groupId").getValue();
        String artifactId = artifact.getChild("artifactId").getValue();
        Xpp3Dom n = artifact.getChild("classifier");
        String classifier = n != null ? n.getValue() : null;
        String version = artifact.getChild("version").getValue();
        Artifact a = classifier != null ? embedder.createArtifactWithClassifier(groupId, artifactId, version, "zip",
                classifier) : embedder.createArtifact(groupId, artifactId, version, null, "zip");
        // embedder.resolve(a, null, embedder.getLocalRepository());
        return new File(embedder.getLocalRepository().getBasedir(), embedder.getLocalRepository().pathOf(a));
    }
}