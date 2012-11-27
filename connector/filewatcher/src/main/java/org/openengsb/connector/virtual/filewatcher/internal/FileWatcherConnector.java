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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import org.apache.commons.lang.ArrayUtils;
import org.openengsb.connector.virtual.filewatcher.FileSerializer;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.common.VirtualConnector;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;

public class FileWatcherConnector extends VirtualConnector implements EventSupport {

    private Class<?> modelType;

    private QueryInterface queryService;

    private PersistInterface persistService;

    private FileSerializer<?> fileSerializer;

    private File watchfile;


    private List<?> localModels = new ArrayList<Object>();

    private Timer timer;

    public FileWatcherConnector(String instanceId, PersistInterface persistService, QueryInterface queryService) {
        super(instanceId);
        this.persistService = persistService;
        this.queryService = queryService;
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ArrayUtils.contains(this.getClass().getInterfaces(), method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onEvent(Event event) {
        try {
            update();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void update() throws IOException {
        List<?> models = queryService.queryForActiveModels(modelType);
        if(models == null){
            watchfile.delete();
            localModels = Collections.emptyList();
        } else {
            fileSerializer.writeFile(watchfile, (List) models);
            localModels = models;
        }

    }

    private static <ModelType> EKBCommit buildCommit(List<ModelType> localModels, List<ModelType> newModels) {
        EKBCommit result = new EKBCommit();
        List<ModelType> inserted = subtract(newModels, localModels);
        result.addInserts(inserted);
        List<ModelType> deleted = subtract(localModels, newModels);
        result.addDeletes(deleted);
        return result;
    }

    private static <ModelType> List<ModelType> subtract(List<ModelType> list1, List<ModelType> list2) {
        ArrayList<ModelType> result = new ArrayList<ModelType>(list1);
        result.removeAll(list2);
        return result;
    }

    public void setFileSerializer(FileSerializer<?> fileSerializer) {
        this.fileSerializer = fileSerializer;
    }

    public void setLocalModels(List<?> localModels) {
        this.localModels = localModels;
    }

    public void setModelType(Class<?> modelType) {
        this.modelType = modelType;
    }

    public void setWatchfile(File watchfile) {
        if (!watchfile.getParentFile().exists()) {
            watchfile.getParentFile().mkdirs();
        }
        this.watchfile = watchfile;
        try {
            initFilePoller();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initFilePoller() throws IOException {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        DirectoryWatcher watcher = new DirectoryWatcher(watchfile) {
            @Override
            protected synchronized void onFileModified() {
                List<?> newModels = null;
                try {
                    newModels = fileSerializer.readFile(watchfile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(localModels == null){
                    System.out.println("localModels are null, WTF?????");
                }
                EKBCommit commit = buildCommit((List) localModels, (List) newModels);
                persistService.commit(commit);
            }
        };
        if (watchfile.exists()) {
            localModels = fileSerializer.readFile(watchfile);
        } else {
            localModels = Collections.emptyList();
        }
        timer.schedule(watcher, 0, 1000);
    }
}
