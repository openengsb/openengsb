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
package org.openengsb.core.workflow.test;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.workflow.RuleManager;
import org.openengsb.core.workflow.WorkflowException;
import org.openengsb.core.workflow.WorkflowService;
import org.openengsb.core.workflow.internal.RuleBaseException;
import org.openengsb.core.workflow.model.Event;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class RuntimeTest implements BundleActivator {

    private static Log log = LogFactory.getLog(RuntimeTest.class);

    private BundleContext bundleContext;

    private Timer timer = new Timer();

    public RuntimeTest() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;
        initTimer();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        timer.cancel();
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
                ServiceReference workflowRef = bundleContext.getServiceReference(WorkflowService.class.getName());

                if (workflowRef == null) {
                    log.error("workflow service not found");
                    return;
                }
                WorkflowService workflowService = (WorkflowService) bundleContext.getService(workflowRef);
                Event event = new Event("", "hello");
                try {
                    workflowService.processEvent(event);
                } catch (WorkflowException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }

}
