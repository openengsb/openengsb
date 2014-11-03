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
package org.openengsb.connector.userprojects.file.internal;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileWatcher extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    protected File[] watchFiles;
    private Map<File, Long> lastModifiedMap = new HashMap<>();

    public FileWatcher(File... watchfiles) {
        this.watchFiles = watchfiles;
        for (File watchFile : watchfiles) {
            lastModifiedMap.put(watchFile, 0L);
        }
    }

    @Override
    public void run() {
        Set<File> modifiedFiles = new HashSet<>();
        for (File watchFile : watchFiles) {
            LOGGER.debug("polling file " + watchFile);
            long lm = watchFile.lastModified();
            if (lm == lastModifiedMap.get(watchFile)) {
                continue;
            }
            modifiedFiles.add(watchFile);
            lastModifiedMap.put(watchFile, lm);
        }
        if (!modifiedFiles.isEmpty()) {
            onFilesModified(modifiedFiles);
        }
    }

    protected abstract void onFilesModified(Set<File> modifiedFiles);

}
