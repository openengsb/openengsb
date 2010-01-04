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

package org.openengsb.edb.core.test.performance.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.Searcher;
import org.openengsb.edb.core.search.lucene.LuceneIndexer;
import org.openengsb.edb.core.search.lucene.LuceneSearcher;
import org.openengsb.edb.core.test.unit.lucene.ATestStub;
import org.openengsb.util.IO;

public class SearcherPerfTest extends ATestStub {

    private static List<GenericContent> content;
    private static final String PATH = "target/dump";
    private static final int GC_COUNT = 2000;
    private static final int FIELD_COUNT = 50;
    private static final int STUPID_ITERATIONS = 3;

    private static GenericContent singlePart;
    private static GenericContent comboPart;

    private static final String PREFIX = "prefix";
    private static final String MIDDLE = "middle";
    private static final String SUFFIX = "suffix";
    private static final String EMPTY = "_";

    private static final String PATH1 = "/customer/projectId"
            + "/region/componentNumber/cpuNumber/peripheralBoardAddress/" + "inputOutputModule/channelName";
    private static final String PATH2 = "/customer/projectId1"
            + "/region/componentNumber/cpuNumber/peripheralBoardAddress/" + "inputOutputModule/channelName";

    private static final String UUID_KEY = "uuid";
    private static final String UUID_VALUE_1 = "a";
    private static final String UUID_VALUE_2 = "b";

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String KEY4 = "key4";
    private static final String UUID = "uuid";

    private Searcher searcher;
    private List<GenericContent> result;
    private String term;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SearcherPerfTest.singlePart = new GenericContent();
        SearcherPerfTest.singlePart.setProperty(SearcherPerfTest.KEY1, SearcherPerfTest.PREFIX);
        SearcherPerfTest.singlePart.setProperty(SearcherPerfTest.KEY2, SearcherPerfTest.MIDDLE);
        SearcherPerfTest.singlePart.setProperty(SearcherPerfTest.KEY3, SearcherPerfTest.SUFFIX);
        SearcherPerfTest.singlePart.setProperty(SearcherPerfTest.KEY4, SearcherPerfTest.EMPTY);
        SearcherPerfTest.singlePart.setProperty(SearcherPerfTest.UUID_KEY, SearcherPerfTest.UUID_VALUE_1);
        SearcherPerfTest.singlePart.setPath(SearcherPerfTest.PATH1);

        SearcherPerfTest.comboPart = new GenericContent();
        SearcherPerfTest.comboPart
                .setProperty(SearcherPerfTest.KEY1, SearcherPerfTest.PREFIX + SearcherPerfTest.MIDDLE);
        SearcherPerfTest.comboPart
                .setProperty(SearcherPerfTest.KEY2, SearcherPerfTest.MIDDLE + SearcherPerfTest.SUFFIX);
        SearcherPerfTest.comboPart
                .setProperty(SearcherPerfTest.KEY3, SearcherPerfTest.PREFIX + SearcherPerfTest.SUFFIX);
        SearcherPerfTest.comboPart.setProperty(SearcherPerfTest.KEY4, SearcherPerfTest.PREFIX + SearcherPerfTest.MIDDLE
                + SearcherPerfTest.SUFFIX);
        SearcherPerfTest.comboPart.setProperty(SearcherPerfTest.UUID_KEY, SearcherPerfTest.UUID_VALUE_2);
        SearcherPerfTest.comboPart.setPath(SearcherPerfTest.PATH2);

        Indexer indexer = new LuceneIndexer(SearcherPerfTest.PATH);

        indexer.addDocuments(Arrays.asList(new GenericContent[] { SearcherPerfTest.singlePart,
                SearcherPerfTest.comboPart, }));
        SearcherPerfTest.content = buildGC(SearcherPerfTest.GC_COUNT, SearcherPerfTest.FIELD_COUNT,
                SearcherPerfTest.PATH);

        try {
            for (int i = 0; i < SearcherPerfTest.STUPID_ITERATIONS; i++) {
                indexer.addDocuments(SearcherPerfTest.content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        indexer.cleanup();
    }

    @Before
    public void setUp() throws Exception {
        searcher = new LuceneSearcher(SearcherPerfTest.PATH);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IO.deleteStructure(new File(SearcherPerfTest.PATH));
    }

    @Test
    public void testSearchExactMatch() {
        term = SearcherPerfTest.KEY1 + ":" + SearcherPerfTest.PREFIX;
        result = searcher.search(term);
        assertNotNull(result);
        assertEquals(1, result.size());
        compareGC(SearcherPerfTest.singlePart, result.get(0));

        term = SearcherPerfTest.KEY4 + ":" + SearcherPerfTest.PREFIX + SearcherPerfTest.MIDDLE
                + SearcherPerfTest.SUFFIX;
        result = searcher.search(term);
        assertNotNull(result);
        assertEquals(1, result.size());
        compareGC(SearcherPerfTest.comboPart, result.get(0));
    }

    @Test
    public void testBatchQuery() {
        term = SearcherPerfTest.KEY1 + ":" + "nothing";
        result = searcher.search(term);
        for (int i = 0; i < 7000; i++) {
            if (searcher.search(term).size() == 0) {
                result.add(new GenericContent());
            }
        }
        assertEquals(7000, result.size());
    }

    private static void compareGC(GenericContent expected, GenericContent actual) {
        assertEquals(expected.getProperty(SearcherPerfTest.KEY1), actual.getProperty(SearcherPerfTest.KEY1));
        assertEquals(expected.getProperty(SearcherPerfTest.KEY2), actual.getProperty(SearcherPerfTest.KEY2));
        assertEquals(expected.getProperty(SearcherPerfTest.KEY3), actual.getProperty(SearcherPerfTest.KEY3));
        assertEquals(expected.getProperty(SearcherPerfTest.KEY4), actual.getProperty(SearcherPerfTest.KEY4));
        assertEquals(expected.getProperty(SearcherPerfTest.UUID), actual.getProperty(SearcherPerfTest.UUID));
        assertEquals(expected.getPath(), actual.getPath());
    }

}
