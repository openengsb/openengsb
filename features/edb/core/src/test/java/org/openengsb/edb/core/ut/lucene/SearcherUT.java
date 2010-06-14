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

package org.openengsb.edb.core.ut.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.lucene.ATestStub;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.Searcher;
import org.openengsb.edb.core.search.lucene.LuceneIndexer;
import org.openengsb.edb.core.search.lucene.LuceneSearcher;

public class SearcherUT extends ATestStub {

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
        SearcherUT.singlePart = new GenericContent();
        SearcherUT.singlePart.setProperty(SearcherUT.KEY1, SearcherUT.PREFIX);
        SearcherUT.singlePart.setProperty(SearcherUT.KEY2, SearcherUT.MIDDLE);
        SearcherUT.singlePart.setProperty(SearcherUT.KEY3, SearcherUT.SUFFIX);
        SearcherUT.singlePart.setProperty(SearcherUT.KEY4, SearcherUT.EMPTY);
        SearcherUT.singlePart.setProperty(SearcherUT.UUID_KEY, SearcherUT.UUID_VALUE_1);
        SearcherUT.singlePart.setPath(SearcherUT.PATH1);

        SearcherUT.comboPart = new GenericContent();
        SearcherUT.comboPart.setProperty(SearcherUT.KEY1, SearcherUT.PREFIX + SearcherUT.MIDDLE);
        SearcherUT.comboPart.setProperty(SearcherUT.KEY2, SearcherUT.MIDDLE + SearcherUT.SUFFIX);
        SearcherUT.comboPart.setProperty(SearcherUT.KEY3, SearcherUT.PREFIX + SearcherUT.SUFFIX);
        SearcherUT.comboPart.setProperty(SearcherUT.KEY4, SearcherUT.PREFIX + SearcherUT.MIDDLE + SearcherUT.SUFFIX);
        SearcherUT.comboPart.setProperty(SearcherUT.UUID_KEY, SearcherUT.UUID_VALUE_2);
        SearcherUT.comboPart.setPath(SearcherUT.PATH2);

        Indexer indexer = new LuceneIndexer(SearcherUT.PATH);

        indexer.addDocuments(Arrays.asList(new GenericContent[] { SearcherUT.singlePart, SearcherUT.comboPart, }));
        SearcherUT.content = buildGC(SearcherUT.GC_COUNT, SearcherUT.FIELD_COUNT, SearcherUT.PATH);

        try {
            for (int i = 0; i < SearcherUT.STUPID_ITERATIONS; i++) {
                indexer.addDocuments(SearcherUT.content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        indexer.cleanup();
    }

    @Before
    public void setUp() throws Exception {
        searcher = new LuceneSearcher(SearcherUT.PATH);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        FileUtils.deleteDirectory(new File(SearcherUT.PATH));
    }

    @Test
    public void testSearchExactMatch() {
        term = SearcherUT.KEY1 + ":" + SearcherUT.PREFIX;
        result = searcher.search(term);
        assertNotNull(result);
        assertEquals(1, result.size());
        compareGC(SearcherUT.singlePart, result.get(0));

        term = SearcherUT.KEY4 + ":" + SearcherUT.PREFIX + SearcherUT.MIDDLE + SearcherUT.SUFFIX;
        result = searcher.search(term);
        assertNotNull(result);
        assertEquals(1, result.size());
        compareGC(SearcherUT.comboPart, result.get(0));
    }

    @Test
    public void testBatchQuery() {
        term = SearcherUT.KEY1 + ":" + "nothing";
        result = searcher.search(term);
        for (int i = 0; i < 7000; i++) {
            if (searcher.search(term).size() == 0) {
                result.add(new GenericContent());
            }
        }
        assertEquals(7000, result.size());
    }

    private static void compareGC(GenericContent expected, GenericContent actual) {
        assertEquals(expected.getProperty(SearcherUT.KEY1), actual.getProperty(SearcherUT.KEY1));
        assertEquals(expected.getProperty(SearcherUT.KEY2), actual.getProperty(SearcherUT.KEY2));
        assertEquals(expected.getProperty(SearcherUT.KEY3), actual.getProperty(SearcherUT.KEY3));
        assertEquals(expected.getProperty(SearcherUT.KEY4), actual.getProperty(SearcherUT.KEY4));
        assertEquals(expected.getProperty(SearcherUT.UUID), actual.getProperty(SearcherUT.UUID));
        assertEquals(expected.getPath(), actual.getPath());
    }

}
