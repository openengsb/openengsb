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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.openengsb.edb.core.entities.GenericContent;


/**
 * Definition allows to manage and the directory index of the underlying search
 * tool.
 */
public interface Indexer {

    /**
     * Retrieve the index-writer (i.e. for testing purposes). Using the writer
     * directly should be avoided.
     * 
     * @return The index
     */
    IndexWriter getWriter();

    /**
     * Add a list of GenericContent instances to the index, storing all
     * key-value pairs as fields, using successive calls of
     * {@link #addDocument(GenericContent)}. Calls index optimization
     * afterwards.
     * 
     * @param gcList - List of content to index
     * @throws IOException - If an error occurs during index optimization
     */
    Indexer addDocuments(List<GenericContent> gcList) throws IOException;

    /**
     * Closing the index-writer. Should be called after an instance will be
     * dismissed (which is unlikely to be properly detected from inside this
     * class).
     */
    void cleanup();

    /**
     * Commit all changes and close the index-writer.
     */
    void commit() throws IOException;

    /**
     * Add a GenericContent instance to the index, storing all key-value pairs
     * as fields. Older versions of the GenericContent, identified by its UUID
     * will be deleted.
     * 
     * @param gc - Content to index
     */
    Indexer addDocument(GenericContent gc) throws IOException;

    /**
     * Remove a GenericContent instance from the index, identified by its UUID.
     * 
     * @param gc - content being removed from index
     * @throws IOException
     */
    Indexer removeDocument(GenericContent gc) throws IOException;

}