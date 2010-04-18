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

package org.openengsb.edb.core.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.Searcher;
import org.openengsb.edb.core.search.lucene.LuceneIndexer;
import org.openengsb.edb.core.search.lucene.LuceneSearcher;
import org.openengsb.util.IO;

public class SearcherTest extends ATestStub {

    private static List<GenericContent> content;
    private static final String PATH = "target/dump";
    private static final int GC_COUNT = 2000;
    private static final int FIELD_COUNT = 50;
    private static final int STUPID_ITERATIONS = 3;

    private static GenericContent singlePart;
    private static GenericContent comboPart;
    private static GenericContent strangePart;

    private static final String PREFIX = "prefix";
    private static final String MIDDLE = "middle";
    private static final String SUFFIX = "suffix";
    private static final String EMPTY = "_";

    private static final String OTHER = "other";
    private static final String NUMBER = "16";
    private static final String STRANGE = "XM00.232E.XM00";
    private static final String ADDITIONAL = "&&# a ))";

    private static final String PATH1 = "/customer/projectId"
            + "/region/componentNumber/cpuNumber/peripheralBoardAddress/" + "inputOutputModule/channelName";
    private static final String PATH2 = "/customer/projectId1"
            + "/region/componentNumber/cpuNumber/peripheralBoardAddress/" + "inputOutputModule/channelName";

    private static final String UUID_KEY = "uuid";
    private static final String UUID_VALUE_1 = "a";
    private static final String UUID_VALUE_2 = "b";
    private static final String UUID_VALUE_3 = "c";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String KEY4 = "key4";
    private static final String UUID = "uuid";
    private static final String PATH_NAME = "path";

    private Searcher searcher;
    private List<GenericContent> result;
    private String term;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SearcherTest.singlePart = new GenericContent();
        SearcherTest.singlePart.setProperty(SearcherTest.KEY1, SearcherTest.PREFIX);
        SearcherTest.singlePart.setProperty(SearcherTest.KEY2, SearcherTest.MIDDLE);
        SearcherTest.singlePart.setProperty(SearcherTest.KEY3, SearcherTest.SUFFIX);
        SearcherTest.singlePart.setProperty(SearcherTest.KEY4, SearcherTest.EMPTY);
        SearcherTest.singlePart.setProperty(SearcherTest.UUID_KEY, SearcherTest.UUID_VALUE_1);
        SearcherTest.singlePart.setPath(SearcherTest.PATH1);

        SearcherTest.comboPart = new GenericContent();
        SearcherTest.comboPart.setProperty(SearcherTest.KEY1, SearcherTest.PREFIX + SearcherTest.MIDDLE);
        SearcherTest.comboPart.setProperty(SearcherTest.KEY2, SearcherTest.MIDDLE + SearcherTest.SUFFIX);
        SearcherTest.comboPart.setProperty(SearcherTest.KEY3, SearcherTest.PREFIX + SearcherTest.SUFFIX);
        SearcherTest.comboPart.setProperty(SearcherTest.KEY4, SearcherTest.PREFIX + SearcherTest.MIDDLE
                + SearcherTest.SUFFIX);
        SearcherTest.comboPart.setProperty(SearcherTest.UUID_KEY, SearcherTest.UUID_VALUE_2);
        SearcherTest.comboPart.setPath(SearcherTest.PATH2);

        SearcherTest.strangePart = new GenericContent();
        SearcherTest.strangePart.setProperty(SearcherTest.KEY1, SearcherTest.NUMBER);
        SearcherTest.strangePart.setProperty(SearcherTest.KEY2, SearcherTest.STRANGE);
        SearcherTest.strangePart.setProperty(SearcherTest.KEY3, SearcherTest.OTHER);
        SearcherTest.strangePart.setProperty(SearcherTest.KEY4, SearcherTest.ADDITIONAL);
        SearcherTest.strangePart.setProperty(SearcherTest.UUID_KEY, SearcherTest.UUID_VALUE_3);
        SearcherTest.strangePart.setPath(SearcherTest.PATH2);

        Indexer indexer = new LuceneIndexer(SearcherTest.PATH);

        indexer.addDocuments(Arrays.asList(new GenericContent[] { SearcherTest.singlePart, SearcherTest.comboPart,
                SearcherTest.strangePart, }));
        SearcherTest.content = buildGC(SearcherTest.GC_COUNT, SearcherTest.FIELD_COUNT, SearcherTest.PATH);

        try {
            for (int i = 0; i < SearcherTest.STUPID_ITERATIONS; i++) {
                indexer.addDocuments(SearcherTest.content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        indexer.cleanup();
    }

    @Before
    public void setUp() throws Exception {
        this.searcher = new LuceneSearcher(SearcherTest.PATH);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IO.deleteStructure(new File(SearcherTest.PATH));
    }

    @Test
    public void testSearchExactMatch() {
        this.term = SearcherTest.KEY1 + ":" + SearcherTest.PREFIX;
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(1, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));

        this.term = SearcherTest.KEY4 + ":" + SearcherTest.PREFIX + SearcherTest.MIDDLE + SearcherTest.SUFFIX;
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(1, this.result.size());
        compareGC(SearcherTest.comboPart, this.result.get(0));
    }

    @Test
    public void testSearchWildcardMatch() {
        this.term = SearcherTest.KEY1 + ":" + "*" + SearcherTest.PREFIX + "*";
        this.result = this.searcher.search(this.term);

        assertNotNull(this.result);
        assertEquals(2, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));
        compareGC(SearcherTest.comboPart, this.result.get(1));
    }

    @Test
    public void testSearchMatchCombined() {
        this.term = SearcherTest.KEY1 + ":" + SearcherTest.PREFIX + "*" + SearcherTest.MIDDLE + "*";
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(1, this.result.size());
        compareGC(SearcherTest.comboPart, this.result.get(0));

        this.term = SearcherTest.KEY1 + ":" + SearcherTest.PREFIX + "*" + SearcherTest.MIDDLE + "*" + " AND "
                + SearcherTest.KEY2 + ":" + SearcherTest.MIDDLE + SearcherTest.SUFFIX;

        assertNotNull(this.result);
        assertEquals(1, this.result.size());
        compareGC(SearcherTest.comboPart, this.result.get(0));
    }

    @Test
    public void testSearchPathFilter() {
        this.term = SearcherTest.PATH_NAME + ":" + "/customer/*/*/*/*/*/*/*";
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(3, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));
        compareGC(SearcherTest.comboPart, this.result.get(1));
        compareGC(SearcherTest.strangePart, this.result.get(2));

        this.term = SearcherTest.PATH_NAME + ":" + "/customer/projectId/*/*/*/*/*/*";
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(1, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));
    }

    @Test
    public void testOrOnSameProperty() {
        this.term = SearcherTest.UUID + ":" + SearcherTest.UUID_VALUE_1 + " OR " + SearcherTest.UUID + ":"
                + SearcherTest.UUID_VALUE_2;
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(2, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));
        compareGC(SearcherTest.comboPart, this.result.get(1));
    }

    @Test
    public void testOrDifferentProperties() {
        this.term = SearcherTest.UUID + ":" + SearcherTest.UUID_VALUE_1 + " OR " + SearcherTest.KEY2 + ":"
                + SearcherTest.MIDDLE + SearcherTest.SUFFIX;
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(2, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(0));
        compareGC(SearcherTest.comboPart, this.result.get(1));
    }

    @Test
    public void testBracketToGroupProperties() {
        this.term = SearcherTest.UUID + ":" + SearcherTest.UUID_VALUE_1 + " OR " + "(" + SearcherTest.KEY2 + ":"
                + SearcherTest.MIDDLE + SearcherTest.SUFFIX + " AND " + SearcherTest.KEY1 + ":" + SearcherTest.PREFIX
                + SearcherTest.MIDDLE + ")";
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(2, this.result.size());
        compareGC(SearcherTest.singlePart, this.result.get(1));
        compareGC(SearcherTest.comboPart, this.result.get(0));
    }

    @Test
    public void testSearchAll() {
        this.term = "*";
        this.result = this.searcher.search(this.term);
        assertNotNull(this.result);
        assertEquals(SearcherTest.GC_COUNT + 3, this.result.size());
    }

    @Test
    public void testCrazyTest() throws Exception {
        this.term = "key1:pr?fix AND key2:\"middle\"";
        this.result = this.searcher.search(this.term);
        Assert.assertEquals(1, this.result.size());
    }

    @Test
    public void testDamnd() throws Exception {
        this.term = "key1:16 AND key2:XM00.232E.XM00 AND key4:&???????";
        this.result = this.searcher.search(this.term);
        Assert.assertEquals(1, this.result.size());
        compareGC(SearcherTest.strangePart, this.result.get(0));
    }

    private static void compareGC(GenericContent expected, GenericContent actual) {
        assertEquals(expected.getProperty(SearcherTest.KEY1), actual.getProperty(SearcherTest.KEY1));
        assertEquals(expected.getProperty(SearcherTest.KEY2), actual.getProperty(SearcherTest.KEY2));
        assertEquals(expected.getProperty(SearcherTest.KEY3), actual.getProperty(SearcherTest.KEY3));
        assertEquals(expected.getProperty(SearcherTest.KEY4), actual.getProperty(SearcherTest.KEY4));
        assertEquals(expected.getProperty(SearcherTest.UUID), actual.getProperty(SearcherTest.UUID));
        assertEquals(expected.getPath(), actual.getPath());
    }

}
