/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.domain.scm;

import java.io.File;

import org.openengsb.core.common.Domain;

/**
 * ScmDomain is an abstraction for working with SCM tools.
 *
 */
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

    /**
     * Check if HEAD revision of a {@code file} exists in repository.
     *
     * @param file relative repository path to file
     *
     * @return true if item exists in repository, otherwise false
     */
    boolean exists(String file);

    /**
     * Check if given {@code version} of a {@code file} exists in repository.
     *
     * @param file relative repository path to file
     * @param version file version
     *
     * @return true if file exists in repository, otherwise false
     */
    boolean exists(String file, CommitRef version);

    /**
     * Adds new working {@code file} to index.
     *
     * @throws {@link ScmException} if working {@code file} does not exist or is not accessible.
     */
    void add(File file);

    /**
     * Adds new working {@code directory} to index.
     *
     * @throws {@link ScmException} if working {@code directory} does not exist or is not accessible.
     */
    void add(File directory, boolean recursive);

    /**
     * Commit changes of working {@code file} to repository.
     *
     * @throws {@link ScmException} if working {@code file} does not exist or is not accessible.
     * @return version number, see {@link CommitRef}
     */
    CommitRef commit(File file, String comment);

    /**
     * Commit working {@code directory} changes to repository.
     *
     * @throws {@link ScmException} if working {@code directory} does not exist or is not accessible.
     * @return version number, see {@link CommitRef}
     */
    CommitRef commit(File directory, String comment, boolean recursive);

    /**
     * Checkout {@code version} of repository {@code path} to specified working {@code directory}. To checkout folder
     * with all its children set the {@code recursive} flag.
     *
     *
     * @throws {@link ScmException} if working {@code directory} is not accessible or can not be created.
     */
    void checkout(String repository, CommitRef version, File directory, boolean recursive);

    /**
     * Checkout HEAD version of repository {@code path} to specified working {@code directory}. To checkout folder with
     * all its children set the {@code recursive} flag.
     *
     *
     * @throws {@link ScmException} if working {@code directory} is not accessible or can not be created.
     */
    void checkout(String path, File directory, boolean recursive);

}
