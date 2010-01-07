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

/**
 * The {@link Reset} interface describes a reset operation on a repository.
 * This has to be done in a serialized method to avoid strange side effects on
 * simultaneous calls. The object implementing the {@link Reset} interface
 * collects all data and finally does a reset on the {@link #reset()} method.
 * Have in mind that at least the {@link #setMode(String mode)} and
 * {@link #setDepth(int depth)} methods have to be called to determine reset
 * effects (ref, index and workdir) and how far the reset should go. In
 * addition, {@link #setAuthor(String, String)} methods are required since
 * {@link #reset()} has to fail with a {@link RepositoryManagementException}
 * otherwise.
 */
public interface Reset {

    String RESET_HARD = "hard";
    String RESET_SOFT = "soft";
    String RESET_DEFAULT = "";

    /**
     * Set how many steps shall be reseted.
     */
    Reset setDepth(int depth);

    /**
     * Sets the mode (rollback behavior based on the implementation)
     */
    Reset setMode(String mode);

    /**
     * Finally does an reset against the source base. Returns the current head
     * identifier.
     * 
     * @return head identifier/hash
     */
    String reset() throws RepositoryStateException;
}
