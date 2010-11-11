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

public interface ScmDomain extends Domain {

    /**
     * Polls the represented repository for updates. Returns true if there have been changes since the last poll.
     * @deprecated
     */
    boolean poll();

    /**
     * Exports the current head of the repository to the specified directory.
     *
     * @param directory if the directory is non-existent, it'll be created. if the directory already exists it must not
     *        contain any files.
     *        
     * @deprecated
     */
    void export(File directory);
    
	/**
	 * Checks if item specified by relative path exists in repository
	 * 
	 * @param path repository item's relative path   
	 * @return true if item exists in repository, otherwise false
	 */
	 boolean exists(String path);
    
    
	/**
	 * Checks if item specified by combination of relative path 
	 * and commit-ref exists in repository
	 * 
	 * @param path repository item's relative path  
	 * @param id commit reference, see {@link CommitRef}
	 * @return true if item exists in repository, otherwise false
	 */
	 boolean exists(String path, CommitRef id);

	/**
	 * Adds new file to repository
	 * 
	 * @param file file to add
	 */
	 void addFile(File file);

	/**
	 * Adds new directory to repository
	 * 
	 * @param dir directory to add
	 * @param recursive add directory and all its children to full recursion
	 */
	 void addDirectory(File dir, boolean recursive);

	/**
	 * Commit single file to repository
	 * 
	 * @param file file to commit
	 * @param comment user comment
	 * @return commit reference, see {@link CommitRef} 
	 */
	 CommitRef commitFile(File file, String comment);
		
	/**
	 * Commit directory to repository
	 * 
	 * @param dir directory to commit
	 * @param comment user comment
	 * @param recursive commit directory and all its children to full recursion
	 * @return commit reference, see {@link CommitRef}
	 */
	 CommitRef commitDirectory(File dir, String comment, boolean recursive);
	
	/**
	 * Checkout single file from repository
	 * 
	 * @param path relative path to repository item  
	 * @param id file commit reference, see {@link CommitRef}
	 * @param dir local directory, the working copy 
	 */
	 void checkoutFile(String path, CommitRef id, File dir);
	
	/**
	 * Checkout directory from repository
	 * 
	 * @param path relative path to repository item  
	 * @param id directory commit reference, see {@link CommitRef}
	 * @param depth checkout depth, see {@link CheckoutDepth}
	 * @param dir local directory, the working copy directory
	 */
	 void checkoutDirectory(String path, CommitRef id, CheckoutDepth depth, File dir);
}
