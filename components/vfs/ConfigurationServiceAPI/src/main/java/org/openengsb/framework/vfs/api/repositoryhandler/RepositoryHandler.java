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

package org.openengsb.framework.vfs.api.repositoryhandler;

import java.nio.file.Path;
import org.openengsb.framework.vfs.api.common.Tag;

/**
 * The RepositoryHandler is responsible for handling the configuration repository that is used by VFS to configure openengsb.
 */
public interface RepositoryHandler {
    /**
     * Creates a tag from the specified path.
     * @param path The specified path.
     * @param tagName The name of the tag.
     */
    void tagDirectory(Path path, String tagName);

    /**
     * Gets the youngest tag that is older than the current tag.
     * @param currentTag The current tag.
     * @return The youngest tag that is older than the current tag or null if no older tag can be found.
     */
    Tag getPreviousTag(Tag currentTag);

    Path getRepositoryPath();
    Path getConfigurationPath();
}
