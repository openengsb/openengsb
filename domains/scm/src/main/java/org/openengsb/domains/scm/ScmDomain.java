/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.scm;

import java.io.File;

import org.openengsb.core.common.Domain;

public interface ScmDomain extends Domain {

    /**
     * Polls the represented repository for updates. Returns true if there have been changes since the last poll.
     */
    boolean poll();

    /**
     * Exports the current head of the repository to the specified directory.
     *
     * @param directory if the directory is non-existent, it'll be created. if the directory already exists it must not
     *        contain any files.
     */
    void export(File directory);
}
