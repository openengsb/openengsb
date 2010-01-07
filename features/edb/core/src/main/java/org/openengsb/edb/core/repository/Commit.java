/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.edb.core.repository;

import java.io.File;

import org.openengsb.edb.core.entities.GenericContent;


/**
 * The {@link Commit} interface describes a single commit to a repository. This
 * commit have to be done in a serialized method; since otherwise its possible
 * to do two commits at once which can generate strange side effects. The object
 * implementing the {@link Commit} interface collects all data and finally do a
 * commit on the {@link #commit()} method. Have in mind that at least the
 * {@link #add(File...)} or {@link #add(GenericContent...)} methods have to be
 * called to add any content for a commit and furthermore the
 * {@link #setMessage(String)} and {@link #setAuthor(String, String)} methods
 * are required since {@link #commit()} have to fail with a
 * {@link RepositoryManagementException} otherwise.
 */
public interface Commit {

    /**
     * Adds a generic content object to the index which have to be committed.
     */
    Commit add(GenericContent... contents);

    /**
     * Adds a file to the index which have to be committed.
     */
    Commit add(File... files);

    /**
     * Deletes generic content objects from the index.
     */
    Commit delete(GenericContent... contents);

    /**
     * Deletes a file from the index.
     */
    Commit delete(File... files);

    /**
     * Sets a message for an commit.
     */
    Commit setMessage(String message);

    /**
     * Sets the full identification for an author of the commit.
     */
    Commit setAuthor(String fullName, String email);

    /**
     * Finally does an commit against the source base. Returns the current head
     * identifier.
     * 
     * @return head identifier/hash
     */
    String commit() throws RepositoryStateException;

}
