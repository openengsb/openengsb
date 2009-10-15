/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.edb.core.search;

import java.io.File;
import java.util.List;

import org.openengsb.edb.core.entities.GenericContent;


public interface Searcher {

    /**
     * Simple search. Expecting a valid Lucene search query and returns a list
     * of GenericContent instances. (Lucene query syntax: {[key]:[value]}* AND
     * [".*"])
     * 
     * @param search - Query as String, format see above.
     * @return A List of GenericContent instances
     */
    List<GenericContent> search(String search);

    /**
     * Closing the index. Should be called after an instance will be dismissed
     * (which is unlikely to be properly detected from inside this class).
     */
    void cleanup();

    /**
     * Returns the base path of the searcher.
     */
    File getBaseIndex();

}