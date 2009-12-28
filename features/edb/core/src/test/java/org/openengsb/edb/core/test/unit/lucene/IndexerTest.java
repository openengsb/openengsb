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

package org.openengsb.edb.core.test.unit.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.lucene.LuceneIndexer;
import org.openengsb.util.IO;


public class IndexerTest extends ATestStub {

    private static Indexer indexer;
    private static List<GenericContent> content;
    private static final String PATH = "dump";
    private static final int GC_COUNT = 1000;
    private static final int FIELD_COUNT = 50;
    private static final int STUPID_ITERATIONS = 2;

    private IndexWriter writer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IndexerTest.indexer = new LuceneIndexer(IndexerTest.PATH);
        IndexerTest.content = buildGC(IndexerTest.GC_COUNT, IndexerTest.FIELD_COUNT, IndexerTest.PATH);
    }

    @AfterClass
    public static void tearDown() {
        IndexerTest.content = null;
        IndexerTest.indexer.cleanup();
        IO.deleteStructure(new File(IndexerTest.PATH));
    }

    @Test
    public void testAddDocuments() {
        try {
            IndexerTest.indexer.addDocuments(IndexerTest.content);
            this.writer = IndexerTest.indexer.getWriter();
            assertEquals(IndexerTest.GC_COUNT, this.writer.numDocs());
        } catch (IOException e) {
            e.printStackTrace();
            fail("I/O-Exception");
        }
    }

    /**
     * just trying what happens on mass insertions of documents
     */
    @Test
    public void testAddDocumentsIntense() {
        long time = System.currentTimeMillis();
        try {
            // multiple inserts
            for (int i = 0; i < IndexerTest.STUPID_ITERATIONS; i++) {
                IndexerTest.indexer.addDocuments(IndexerTest.content);

            }
            // final insert to optimize&flush
            // => (STUPID_ITERATIONS + 1) x content.size() documents will be
            // added
            IndexerTest.indexer.addDocuments(IndexerTest.content);
            time = System.currentTimeMillis() - time;

            this.writer = IndexerTest.indexer.getWriter();
            int expectedDocs = IndexerTest.content.size();
            assertEquals(expectedDocs, this.writer.numDocs());
            System.out.println("insert/update of " + ((IndexerTest.STUPID_ITERATIONS + 2) * IndexerTest.content.size())
                    + " elements took " + time + "ms.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("I/O-Exception");
        }

    }

    /**
     * just trying what happens on mass insertions of documents
     */
    @Test
    public void testAddDocumentsBlockwise() {
        long time = System.currentTimeMillis();
        try {
            for (int i = 0; i < IndexerTest.STUPID_ITERATIONS; i++) {
                IndexerTest.indexer.addDocuments(IndexerTest.content);
            }

            time = System.currentTimeMillis() - time;

            this.writer = IndexerTest.indexer.getWriter();
            int expectedDocs = IndexerTest.content.size();
            assertEquals(expectedDocs, this.writer.numDocs());
            System.out.println("insert/update of "
                    + ((IndexerTest.STUPID_ITERATIONS + 2) * IndexerTest.content.size() + IndexerTest.STUPID_ITERATIONS
                            * IndexerTest.content.size()) + " elements took " + time + "ms.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("I/O-Exception");
        }

    }
}
