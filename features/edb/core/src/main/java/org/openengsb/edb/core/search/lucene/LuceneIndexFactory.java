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

import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.search.IndexFactory;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.Searcher;


/**
 * Factory to create a new {@link LuceneSearcher}.
 */
public class LuceneIndexFactory implements IndexFactory {

    private String indexBase;

    /*
     * (non-Javadoc)
     * 
     * @see
     * at.ac.tuwien.ifs.engsb.edb.lucene.IIndexFactory#createNewSearcher(at.
     * ac.tuwien.ifs.engsb.edb.repository.IRepository)
     */
    public Searcher createNewSearcher(Repository repo) throws IOException {
        return new LuceneSearcher(repo.getRepositoryBase().getAbsolutePath() + File.separator + this.indexBase);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * at.ac.tuwien.ifs.engsb.edb.lucene.IIndexFactory#createNewIndexer(at.ac
     * .tuwien.ifs.engsb.edb.repository.IRepository)
     */
    public Indexer createNewIndexer(Repository repo) throws IOException {
        return new LuceneIndexer(repo.getRepositoryBase().getAbsolutePath() + File.separator + this.indexBase);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * at.ac.tuwien.ifs.engsb.edb.lucene.IIndexFactory#setIndexBase(java.lang
     * .String)
     */
    public void setIndexBase(String indexBase) {
        this.indexBase = indexBase;
    }

}
