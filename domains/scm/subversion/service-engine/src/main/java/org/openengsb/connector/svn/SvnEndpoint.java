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
package org.openengsb.connector.svn;

import java.util.Date;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.LinkingEndpoint;
import org.openengsb.drools.ScmDomain;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @org.apache.xbean.XBean element="svnEndpoint" description="SVN SCM Endpoint"
 */
public class SvnEndpoint extends LinkingEndpoint<ScmDomain> {
    private SvnConfiguration configuration = null;
    private Scheduler scheduler;
    private int pollInterval;

    @Override
    protected ScmDomain getImplementation(ContextHelper contextHelper, MessageProperties msgProperties) {
        return new SvnScmImplementation(configuration);
    }

    @Override
    public void activate() throws Exception {
        super.activate();

        scheduler = new StdSchedulerFactory().getScheduler();
        JobDetail job = new JobDetail("RepositoryPoller", null, RepositoryPoller.class);
        Trigger trigger = TriggerUtils.makeSecondlyTrigger(pollInterval);
        trigger.setStartTime(new Date());
        trigger.setName("PollTrigger");

        scheduler.scheduleJob(job, trigger);
        scheduler.start();
    }

    @Override
    public void deactivate() throws Exception {
        scheduler.shutdown();

        super.deactivate();
    }

    public void setConfiguration(SvnConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

}
