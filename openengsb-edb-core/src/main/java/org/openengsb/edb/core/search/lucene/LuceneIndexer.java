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

package org.openengsb.edb.core.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.search.Indexer;


/**
 * Class to allow simple creation and management of a Lucene Directory (search
 * index).
 */
public class LuceneIndexer implements Indexer {

    private IndexWriter writer;

    /**
     * Create or open an index, used to add documents (search-able content) or
     * to perform searches.
     * 
     * @param path - File path of the new/existing index
     * @throws IOException if the path is invalid, locked or some internal
     *         I/O-error occurs.
     */
    public LuceneIndexer(String path) throws IOException {
        this.writer = new IndexWriter(new File(path), new ReallySimpleAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
    }

    /**
     * Add a GenericContent instance to the index, storing all key-value pairs
     * as fields.
     * 
     * @param gc - Content to index
     */
    public Indexer addDocument(GenericContent gc) throws IOException {
        Set<Entry<Object, Object>> entries = gc.getEntireContent();
        Document doc = new Document();
        for (Entry<Object, Object> entry : entries) {
            doc.add(new Field(entry.getKey().toString(), entry.getValue().toString(), Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
        }

        this.writer.updateDocument(new Term(GenericContent.UUID_NAME, gc.getUUID()), doc, new ReallySimpleAnalyzer());
        // this.writer.addDocument(doc);

        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.ac.tuwien.ifs.engsb.edb.lucene.IIndexer#getWriter()
     */
    public IndexWriter getWriter() {
        return this.writer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * at.ac.tuwien.ifs.engsb.edb.lucene.IIndexer#addDocuments(java.util.List)
     */
    public Indexer addDocuments(List<GenericContent> gcList) throws IOException {
        for (GenericContent gc : gcList) {
            addDocument(gc);
        }

        // TODO review behavior
        this.writer.optimize();
        this.writer.commit();
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.ac.tuwien.ifs.engsb.edb.lucene.IIndexer#cleanup()
     */
    public void cleanup() {
        try {
            try {
                this.writer.close(true);
            } catch (CorruptIndexException e) {
                // FIXME delete the entire index and recreate
                if (IndexWriter.isLocked(this.writer.getDirectory())) {
                    IndexWriter.unlock(this.writer.getDirectory());
                }
            } catch (IOException e) {
                // TODO most certainly File-I/O probs but something should be
                // done
                if (IndexWriter.isLocked(this.writer.getDirectory())) {
                    IndexWriter.unlock(this.writer.getDirectory());
                }
            }
        } catch (IOException e) {
            // TODO panic, now its really a mess
            e.printStackTrace();
        }
    }

    public Indexer removeDocument(GenericContent gc) throws IOException {

        Term searchTerm = new Term(GenericContent.UUID_NAME, gc.getUUID());

        this.writer.deleteDocuments(searchTerm);

        return this;
    }

    public void commit() throws IOException {
        this.writer.optimize();
        this.writer.commit();

        cleanup();
    }
}
