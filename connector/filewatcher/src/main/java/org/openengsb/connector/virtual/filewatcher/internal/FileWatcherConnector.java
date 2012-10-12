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
package org.openengsb.connector.virtual.filewatcher.internal;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Timer;

import org.openengsb.core.api.Event;
import org.openengsb.core.common.VirtualConnector;
import org.openengsb.core.workflow.api.WorkflowService;

public class FileWatcherConnector extends VirtualConnector {

    private String watchfile;

    private WorkflowService workflowService;

    private Timer timer = new Timer();

    public FileWatcherConnector(String instanceId) {
        super(instanceId);
    }

    public FileWatcherConnector(String instanceId, String watchfile) {
        super(instanceId);
        setWatchfile(watchfile);
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setWatchfile(String watchfile) {
        this.watchfile = watchfile;
        File file = new File(watchfile);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        timer.schedule(new DirectoryWatcher(file) {
            @Override
            protected void onFileModified() {
                Event event = new Event();
                event.setName("test");
                workflowService.processEvent(event);
            }
        }, 0, 1000);
    }
}
