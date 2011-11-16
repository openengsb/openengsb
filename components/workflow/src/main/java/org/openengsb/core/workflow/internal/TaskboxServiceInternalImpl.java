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

package org.openengsb.core.workflow.internal;

import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.TaskboxServiceInternal;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.api.workflow.model.Task;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskboxServiceInternalImpl implements TaskboxServiceInternal {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskboxServiceInternalImpl.class);

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void createNewTask(ProcessBag bag) throws PersistenceException {
        Task task = new Task(bag);
        persistence.create(task);
        LOGGER.info("New human task with id {} created", task.getTaskId());
    }

}
