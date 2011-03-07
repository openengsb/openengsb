/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.taskbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.BundleContextAware;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.model.ProcessBag;
import org.osgi.framework.BundleContext;

public class TaskboxServiceInternalImpl implements TaskboxServiceInternal, BundleContextAware {
    private Log log = LogFactory.getLog(getClass());

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void createNewTask(ProcessBag bag) throws PersistenceException {
        Task task = new Task(bag);
        persistence.create(task);
        log.info("New human task with id " + task.getTaskId() + " created");
    }
}
