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

package org.openengsb.maven.se.endpoints;

import java.io.File;
import java.util.Date;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.maven.common.domains.BuildDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.LogLevelMaven;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.common.serializer.ProjectConfigurationSerializer;
import org.openengsb.maven.se.AbstractMavenEndpoint;


/**
 * @org.apache.xbean.XBean element="mavenBuilder"
 */
public class MavenBuildEndpoint extends AbstractMavenEndpoint implements BuildDomain {

    private boolean testsIncluded;

    @Override
    public void validate() throws DeploymentException {
        super.validate();
    }

    /**
     * The overridden InOut MEP depending on the parameter
     * 
     * @see BuildDomain
     * @see AbstractMavenEndpoint
     */
    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {

            if (this.projectConfiguration == null) {
                SourceTransformer sourceTransformer = new SourceTransformer();
                DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(in);

                this.projectConfiguration = ProjectConfigurationSerializer.deserializeSource(messageXml);
            }

            MavenResult result = null;

            if (this.projectConfiguration != null) {
                result = executeBuild();
            } else {
                throw new MavenException("Build Exception!");
            }

            // set result and send back
            out.setContent(MavenResultSerializer.serializeAsSource(out.getContent(), result));
            getChannel().send(exchange);
        }
    }

    /**
     * @see BuildDomain
     */
    @Override
    public Date buildEndTime() {
        return this.buildEndTime;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public Date buildStartTime() {
        return this.buildStartTime;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public boolean buildStarted() {
        return false;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public MavenResult executeBuild() throws MavenException {

        MavenResult buildResult = new MavenResult();

        buildResult = execute(this.projectConfiguration.getBaseDirectory().getAbsolutePath(), this.projectConfiguration
                .getGoals());
        File tests = new File(this.projectConfiguration.getBaseDirectory(), "target/test-classes");
        checkProjectIncludesTests(tests);

        return buildResult;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public LogLevelMaven getLogLevel() {
        return this.loglevel;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public Options getOptions() {
        return this.options;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public ProjectConfiguration getProjectConfiguration() {
        return this.projectConfiguration;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public void setLogLevel(LogLevelMaven loglevel) {
        this.loglevel = loglevel;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public void setOptions(Options options) {
        this.options = options;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public void setProjectConfiguration(ProjectConfiguration projectConfiguration) {
        this.projectConfiguration = projectConfiguration;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public boolean settingsDefined() {
        return this.settingsDefinied;
    }

    /**
     * @see BuildDomain
     */
    @Override
    public boolean testsIncluded() {
        return this.testsIncluded;
    }

    /**
     * Checks if there are TestCases included in the build path Only TestCases
     * with the Pattern *Test.class will be tracked
     * 
     * @param tests - start directory
     */
    private void checkProjectIncludesTests(File tests) {
        File[] files = tests.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    checkProjectIncludesTests(file);
                } else {
                    if (file.getName().matches("(?i).*Test.class")) {
                        this.testsIncluded = true;
                    }
                }
            }
        }
    }
}
