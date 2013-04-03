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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import org.apache.commons.lang.ArrayUtils;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.connector.virtual.filewatcher.FileSerializer;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.common.VirtualConnector;
import org.openengsb.core.ekb.api.CommitEvent;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcherConnector extends VirtualConnector implements EventSupport {
    private Logger LOGGER = LoggerFactory.getLogger(FileWatcherConnector.class);

    private Class<?> modelType;

    private QueryInterface queryService;

    private PersistInterface persistService;

    private FileSerializer<?> fileSerializer;

    private File watchfile;

    private AuthenticationContext authenticationContext;

    private List<?> localModels = new ArrayList<Object>();

    private Timer timer;

    private EKBCommit lastCommit;

    private final TransformationEngine transformationEngine;

    private boolean ignoreLastModification = false;

    public FileWatcherConnector(String instanceId, String domainType, PersistInterface persistService,
            QueryInterface queryService, AuthenticationContext authenticationContext,
            TransformationEngine transformationEngine) {
        super(instanceId, domainType);
        this.authenticationContext = authenticationContext;
        this.persistService = persistService;
        this.queryService = queryService;
        this.transformationEngine = transformationEngine;
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ArrayUtils.contains(this.getClass().getInterfaces(), method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        return null;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof CommitEvent)) {
            LOGGER.debug("caught non-commit-event - ignoring");
            return;
        }
        else {
            EKBCommit commit = ((CommitEvent) event).getCommit();

            if (commit == lastCommit) {
                LOGGER.debug("caught event with our own commit ({}) - ignoring", commit.toString());
                return;
            }
            LOGGER.debug("caught commit event: {}", event.toString());

            ModelDescription localModelDesc = new ModelDescription(modelType, "3.0.0.SNAPSHOT");
            List<Object> sourceModels = new ArrayList<Object>();

            for (OpenEngSBModel model : commit.getInserts())
            {
                ModelDescription tempModelDesc = new ModelDescription(model.getClass(), "3.0.0.SNAPSHOT");

                if (transformationEngine.isTransformationPossible(tempModelDesc, localModelDesc)) {
                    LOGGER.debug("transforming from model {} to {}", tempModelDesc.toString(), localModelDesc.toString());
                    sourceModels.add(transformationEngine.performTransformation(tempModelDesc, localModelDesc, model));
                }
            }

            if (sourceModels.size() > 0) {
                try {
                    ignoreLastModification = true;
                    fileSerializer.writeFile(watchfile, (List) sourceModels);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private synchronized void update() throws IOException {
        List<?> models = queryService.queryForActiveModels(modelType);
        if (models == null) {
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
                if (ignoreLastModification) {
                    ignoreLastModification = false;
                    return;
                }

                List<?> newModels = null;
                try {
                    newModels = fileSerializer.readFile(watchfile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                EKBCommit commit = buildCommit((List) localModels, (List) newModels);
                commit.setConnectorId("filewatcher");
                commit.setInstanceId(instanceId);
                commit.setDomainId(domainType);
                ContextHolder.get().setCurrentContextId("foo");
                authenticationContext.login("admin", new Password("password"));
                LOGGER.debug("committing {}", commit.toString());
                lastCommit = commit;
                persistService.commit(commit);
                authenticationContext.logout();
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
