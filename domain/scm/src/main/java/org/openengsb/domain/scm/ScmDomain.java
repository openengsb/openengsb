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
     * Looks up changes in a remote repository and updates or checks out a local
     * repository. Check outs are performed if the local repository doesn't yet
     * exist. Updates are performed if the local repository already exists.
     * 
     * @return a list of {@link CommitRef} representing the commits. If a check
     *         out was performed the whole history is included. If an update was
     *         performed the list contains the changes since the last update.
     *         {@code null} if there where no changes since the last update or
     *         the branch reference couldn't be resolved in the remote
     *         repository.
     */
    List<CommitRef> update();

    /**
     * Exports the state of the repository representing the latest commit in a
     * compressed format without its SCM specific files and folders.
     * 
     * @return a compressed file with the contents of the latest commit
     */
    File export();

    /**
     * Exports the state of the repository representing the given commit ref in
     * a compressed format without its SCM specific files and folders.
     * 
     * @param ref that represents a commit in the repository
     * @return a compressed file with the contents of the given commit ref
     */
    File export(CommitRef ref);

    /**
     * Check if HEAD revision of a {@code file} exists in repository.
     * 
     * @param file relative repository path to file
     * 
     * @return true if item exists in repository, otherwise false
     */
    boolean exists(String file);

    /**
     * Retrieves a single file from a repository if it exists in the lastest
     * revision.
     * 
     * @param file relative repository path to file
     * 
     * @return the file with its name and extension without the folder
     *         structure. {@code null} if it doesn't exist in the latest
     *         revision.
     */
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

    /**
     * Retrieves a single file from a repository if it exists in a given
     * revision.
     * 
     * @param file relative repository path to file
     * @param ref reference to the revision where file is in
     * @return the file with its name and extension without the folder
     *         structure. {@code null} if it doesn't exist in the given
     *         revision.
     */
    File get(String file, CommitRef ref);

    /**
     * Returns the current HEAD of the repository as {@link CommitRef}.
     */
    CommitRef getHead();

    /**
     * Adds one or more working files to the repository and commits them.
     */
    CommitRef add(String comment, File... file);

    /**
     * Removes one or more working files to the repository and commits them.
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
