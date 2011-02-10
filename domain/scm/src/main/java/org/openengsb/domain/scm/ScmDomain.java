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
import java.util.List;

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

    List<CommitRef> update();

    /**
     * Exports the current head of the repository to the specified directory.
     * 
     * @param directory if the directory is non-existent, it'll be created. if
     *        the directory already exists it must not contain any files.
     */
    void export(File directory);

    File export();

    File export(CommitRef ref);

    /**
     * Check if HEAD revision of a {@code file} exists in repository.
     * 
     * @param file relative repository path to file
     * 
     * @return true if item exists in repository, otherwise false
     */
    boolean exists(String file);

    File get(String file);

    /**
     * Check if given {@code version} of a {@code file} exists in repository.
     * 
     * @param file relative repository path to file
     * @param version file version
     * 
     * @return true if file exists in repository, otherwise false
     */
    boolean exists(String file, CommitRef version);

    File get(String file, CommitRef ref);

    /**
     * Checkout {@code version} of repository {@code path} to specified working
     * {@code directory}. To checkout folder with all its children set the
     * {@code recursive} flag.
     * 
     * 
     * @throws {@link ScmException} if working {@code directory} is not
     *         accessible or can not be created.
     */
    void checkout(String repository, CommitRef version, File directory, boolean recursive);

    /**
     * Checkout HEAD version of repository {@code path} to specified working
     * {@code directory}. To checkout folder with all its children set the
     * {@code recursive} flag.
     * 
     * 
     * @throws {@link ScmException} if working {@code directory} is not
     *         accessible or can not be created.
     */
    void checkout(String path, File directory, boolean recursive);

    /**
     * Returns the current HEAD of the repository as {@link CommitRef}.
     */
    CommitRef getHead();

    /**
     * Adds one or more working files to the repository and commits them.
     * 
     * @throws {@link ScmException} if one of the files doesn't exist in working
     *         directory.
     */
    CommitRef add(String comment, File... file);

    /**
     * Removes one or more working files to the repository and commits them.
     * 
     * @throws {@link ScmException} if one of the files doesn't exist in working
     *         directory.
     */
    CommitRef remove(String comment, File... file);

    /**
     * Tags the actual HEAD of the repository.
     */
    TagRef tagRepo(String tagName);

    /**
     * Tags the commit of the repository identified by the reference.
     */
    TagRef tagRepo(String tagName, CommitRef ref);

    /**
     * Resolved the {@link CommitRef} for a specific tag name.
     */
    CommitRef getCommitRefForTag(TagRef ref);

}
