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

package org.openengsb.edb.core.search.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.search.Searcher;

/**
 * Class to allow simple search on a given Lucene search index.
 */
public class LuceneSearcher implements Searcher {

    private Directory index;
    private File base;
    private static final int MAX_NUMBER_OF_HITS = 1000000;

    private Log log = LogFactory.getLog(LuceneSearcher.class);

    /**
     * Create a Searcher on a given index (senseless otherwise)
     * 
     * @param path - The path of the index to search in.
     * @throws IOException In case opening the index fails.
     */
    public LuceneSearcher(String path) throws IOException {
        this.base = new File(path);
        this.index = FSDirectory.getDirectory(path);
        BooleanQuery.setMaxClauseCount(LuceneSearcher.MAX_NUMBER_OF_HITS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.ac.tuwien.ifs.engsb.edb.lucene.ISearcher#search(java.lang.String)
     */
    public List<GenericContent> search(String search) {
        List<GenericContent> result = new ArrayList<GenericContent>();

        this.log.debug("query (before parser): " + search);
        try {
            IndexSearcher searcher = new IndexSearcher(this.index);
            QueryParser parser = new QueryParser(GenericContent.UUID_NAME, new ReallySimpleAnalyzer());
            parser.setAllowLeadingWildcard(true);
            parser.setLowercaseExpandedTerms(false);
            Query query = parser.parse(search);
            this.log.debug("query (after parser): " + search);
            ScoreDoc[] results = searcher.search(query, LuceneSearcher.MAX_NUMBER_OF_HITS).scoreDocs;

            for (int i = 0; i < results.length; ++i) {
                int docId = results[i].doc;
                Document document = searcher.doc(docId);
                result.add(documentToGenericContent(document));
            }
            searcher.close();
            this.log.debug("found  " + result.size() + " entries");
        } catch (ParseException e) {
            this.log.warn(e);
            result = new ArrayList<GenericContent>();
        } catch (IOException e) {
            this.log.warn(e);
            result = new ArrayList<GenericContent>();
        }

        return result;
    }

    /**
     * Utility method for converting Lucene Documents into our custom
     * java.util.Properties-like format
     * 
     * @param document - the Document to convert
     * @return A GenericContent instance holding all fields of the given
     *         Document as properties.
     */
    private static GenericContent documentToGenericContent(Document document) {
        GenericContent result = new GenericContent();

        Field f;
        for (Object obj : document.getFields()) {
            f = (Field) obj;
            result.setProperty(f.name(), f.stringValue());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.ac.tuwien.ifs.engsb.edb.lucene.ISearcher#cleanup()
     */
    public void cleanup() {
        try {
            this.index.close();
            this.base = null;
        } catch (CorruptIndexException e) {
            // FIXME delete the entire index and recreate
        } catch (IOException e) {
            // TODO most certainly File-I/O probs but something should be done
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see at.ac.tuwien.ifs.engsb.edb.lucene.ISearcher#getBaseIndex()
     */
    public File getBaseIndex() {
        return this.base;
    }
}
