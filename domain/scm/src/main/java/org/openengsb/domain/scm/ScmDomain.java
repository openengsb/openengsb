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

package org.openengsb.domain.scm;

import java.io.File;

import org.openengsb.core.common.Domain;

/**
 * ScmDomain is an abstraction for working with SCM tools. 
 * 
 */
public interface ScmDomain extends Domain {

    /**
     * Polls the represented repository for updates. Returns true if there have
     * been changes since the last poll.
     */
    boolean poll();

    /**
     * Exports the current head of the repository to the specified directory.
     * 
     * @param directory if the directory is non-existent, it'll be created. if
     *        the directory already exists it must not contain any files.
     */
    void export(File directory);

    /**
     * Checks if file or directory specified by relative {@code path} exists in repository.
     * 
     * @return true if item exists in repository, otherwise false
     */
    boolean exists(String path);

    /**
     * Checks if file or directory specified by relative {@code path} and {@code id}
     * (commit-ref) exists in repository.
     * 
     * @return true if item exists in repository, otherwise false
     */
    boolean exists(String path, CommitRef id);

    /**
     * Adds new working {@code file} to SCM index.
     * 
     * @throws {@link ScmException} if working {@code file} does not exist or is
     *         not accessible.
     */
    void addFile(File file) throws ScmException;

    /**
     * Adds new working {@code directory} to SCM repository. The
     * {@code recursive} option sets if also its children will be added to
     * index.
     * 
     * @throws {@link ScmException} if working {@code directory} does not exist
     *         or is not accessible.
     */
    void addDirectory(File directory, boolean recursive) throws ScmException;

    /**
     * Commit a single {@code file} to SCM repository. The {@code comment}
     * parameter adds message to given commit.
     * 
     * @throws {@link ScmException} if working {@code file} does not exist or is
     *         not accessible.
     * @return commit-ref, see {@link CommitRef}
     */
    CommitRef commitFile(File file, String comment) throws ScmException;

    /**
     * Commit changes on working {@code directory} to SCM repository. The
     * {@code comment} parameter adds message to given commit. The
     * {@code recursive} option set if also changes for its children will be
     * committed.
     * 
     * @throws {@link ScmException} if working {@code directory} does not exist
     *         or is not accessible.
     * @return commit-ref, see {@link CommitRef}
     */
    CommitRef commitDirectory(File directory, String comment, boolean recursive) throws ScmException;

    /**
     * Copy repository file specified by relative {@code path}
     * and commit-ref {@code id} into working {@code directory}.
     * 
     * @throws {@link ScmException} if working {@code directory} is not
     *         accessible or can not be created.
     */
    void checkoutFile(String path, CommitRef id, File directory) throws ScmException;

    /**
     * Copy repository directory specified by relative {@code path} and
     * commit-ref {@code id} into newly created working {@code directory}. To
     * get also all children use {@code recursive} option.
     * 
     * @throws {@link ScmException} if working {@code directory} is not
     *         accessible or can not be created.
     */
    void checkoutDirectory(String path, CommitRef id, boolean recursive, File directory) throws ScmException;
}
