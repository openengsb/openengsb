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

package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openengsb.framework.vfs.api.common.Tag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VFSFileOperator implements FileOperator {

    private final Logger logger = LoggerFactory.getLogger(VFSFileOperator.class);

    public List<Tag> getTagsFromDirectory(Path tagsPath) {
        List<Tag> tags = new ArrayList<Tag>();
        //TODO handling of files that are not tags
        if (Files.exists(tagsPath)) {
            for (File file : tagsPath.toFile().listFiles()) {
                if (file.isDirectory()) {
                    try {
                        Tag tag = new ConfigurationTag(file);
                        tags.add(tag);
                    } catch (ParseException e) {
                        logger.error("Could not parse tag path: " + file.getPath());
                    }
                }
            }
        }

        return tags;
    }

    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public void copy(Path source, Path destination) throws IOException {
        FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
}
