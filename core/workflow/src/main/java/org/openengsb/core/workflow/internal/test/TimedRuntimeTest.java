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
package org.openengsb.core.workflow.internal.test;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.internal.RuleBaseException;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class TimedRuntimeTest implements BundleContextAware {

    private static Log log = LogFactory.getLog(TimedRuntimeTest.class);

    private BundleContext bundleContext;

    public TimedRuntimeTest() {
        initTimer();
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private void initTimer() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                log.info("getting service-ref");
                ServiceReference ref = bundleContext.getServiceReference(RuleManager.class.getName());
                if (ref == null) {
                    log.error("service not found");
                    return;
                }
                RuleManager manager = (RuleManager) bundleContext.getService(ref);
                try {
                    Collection<RuleBaseElementId> list = manager.list(RuleBaseElementType.Rule);
                    for (RuleBaseElementId i : list) {
                        log.info("found rule: " + i.getName());
                    }
                } catch (RuleBaseException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        };
        Timer t = new Timer();
        t.schedule(task, 1000, 1000);
    }

}
