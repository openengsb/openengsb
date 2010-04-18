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
package org.openengsb.test.maven;

import java.util.Map.Entry;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.EventHelper;
import org.openengsb.drools.TestDomain;
import org.openengsb.drools.events.TestEvent;
import org.openengsb.drools.events.TestStartEvent;
import org.openengsb.maven.common.AbstractMavenDomainImpl;
import org.openengsb.maven.common.MavenParameters;
import org.openengsb.maven.common.MavenResult;

public class MavenTestDomainImpl extends AbstractMavenDomainImpl implements TestDomain {

    private EventHelper eventHelper;

    public MavenTestDomainImpl(ContextHelper contextHelper, EventHelper eventHelper) {
        super(contextHelper);
        this.eventHelper = eventHelper;
    }

    @Override
    public Boolean runTests() {
        TestStartEvent startEvent = new TestStartEvent();
        MavenParameters params = getMavenParametersForMavenCall("test/maven-test");
        startEvent.setToolConnector("maven-test");
        startEvent.setParameters(params.toString());
        eventHelper.sendEvent(startEvent);

        MavenResult mavenResult = callMaven(params);
        TestEvent event = new TestEvent();
        event.setToolConnector("maven-test");
        event.setTestRunSuccessful(mavenResult.isSuccess());
        event.setTestOutput(mavenResult.getOutput());
        for (Entry<String, byte[]> report : mavenResult.getTestReports().entrySet()) {
            event.setValue("testReport-" + report.getKey(), report.getValue());
            event.setValue("testReport-" + report.getKey() + ".type", "text/xml");
        }
        eventHelper.sendEvent(event);
        return mavenResult.isSuccess();
    }
}