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
package org.openengsb.build.maven;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.EventHelper;
import org.openengsb.drools.BuildDomain;
import org.openengsb.drools.events.BuildEvent;
import org.openengsb.drools.events.BuildStartEvent;
import org.openengsb.maven.common.AbstractMavenDomainImpl;
import org.openengsb.maven.common.MavenParameters;
import org.openengsb.maven.common.MavenResult;

public class MavenBuildDomainImpl extends AbstractMavenDomainImpl implements BuildDomain {

    private Log log = LogFactory.getLog(getClass());

    private EventHelper eventHelper;

    @Override
    public Boolean buildProject() {
        log.info("Building project using maven connector.");

        BuildStartEvent startEvent = new BuildStartEvent();
        MavenParameters params = getMavenParametersForMavenCall();
        startEvent.setToolConnector("maven-build");
        startEvent.setParameters(params.toString());
        eventHelper.sendEvent(startEvent);

        MavenResult mavenResult = callMaven(params);

        BuildEvent event = new BuildEvent();
        event.setToolConnector("maven-build");
        event.setBuildSuccessful(mavenResult.isSuccess());
        event.setBuildOutput(mavenResult.getOutput());
        eventHelper.sendEvent(event);

        return mavenResult.isSuccess();
    }

    public void setEventHelper(EventHelper eventHelper) {
        this.eventHelper = eventHelper;
    }
}
