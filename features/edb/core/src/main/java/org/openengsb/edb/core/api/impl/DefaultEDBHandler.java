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

package org.openengsb.edb.core.api.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.repository.Commit;
import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.RepositoryStateException;
import org.openengsb.edb.core.search.IndexFactory;
import org.openengsb.edb.core.search.Indexer;
import org.openengsb.edb.core.search.Searcher;
import org.openengsb.util.IO;

/**
 * EDBHandler implementation that runs supposedly with git-based repositories
 * and lucene-based searcher/indexer. Details of the handler implementations may
 * depend upon internal details of these repos, searcher and/or indexers.
 * 
 */
public class DefaultEDBHandler implements EDBHandler {

    private static final String REPO_NAME = "data";
    private static final String SEARCHER_NAME = "lucene";
    private static final String HEAD_NAME = "HEAD";
    private static final String MODE_HARD = "hard";
    private static final String DEFAULT_MSG = "commit via EDB-API";

    private static final String GIT_CONFIG = ".git";

    private static final String ELEM_NAME = "name";

    protected Repository repoData;
    protected Repository repoSearch;

    private RepositoryFactory factoryDataRepo;
    private RepositoryFactory factoryIndexRepo;
    private IndexFactory factoryIndex;

    private Commit commitData;
    private Commit commitSearch;

    private final Log log = LogFactory.getLog(DefaultEDBHandler.class);

    public DefaultEDBHandler(String repositoryId, boolean isAbsolute, RepositoryFactory factoryDataRepo,
            RepositoryFactory factoryIndexRepo, IndexFactory factoryIndex) {

        String pathMain;
        String pathSearch;
        String pathData;

        // build main path, abs or user dir
        if (isAbsolute) {
            pathMain = new StringBuilder().append(repositoryId).append(File.separator).toString();
        } else {
            pathMain = new StringBuilder().append(System.getProperty("user.dir")).append(File.separator).append(
                    repositoryId).append(File.separator).toString();
        }
        // build index / data paths
        pathSearch = new StringBuilder().append(pathMain).append(File.separator)
                .append(DefaultEDBHandler.SEARCHER_NAME).toString();
        pathData = new StringBuilder().append(pathMain).append(File.separator).append(DefaultEDBHandler.REPO_NAME)
                .toString();

        this.factoryDataRepo = factoryDataRepo;
        this.factoryIndexRepo = factoryIndexRepo;
        this.factoryIndex = factoryIndex;

        // load repos
        this.repoData = this.factoryDataRepo.loadRepository(pathData);
        this.repoSearch = this.factoryIndexRepo.loadRepository(pathSearch);
    }

    public synchronized List<GenericContent> query(String query, boolean headRevision) throws EDBException {
        List<GenericContent> foundSignals;
        Searcher searcher = null;
        try {
            this.log.info("Preparing search with query: " + query);
            try {
                searcher = this.factoryIndex.createNewSearcher(this.repoSearch);
                foundSignals = searcher.search(query);
                searcher.cleanup();
                if (headRevision) {
                    GenericContent head = new GenericContent(this.repoData.getRepositoryBase().getAbsolutePath(),
                            new String[] {}, new String[] {});
                    head.setProperty(DefaultEDBHandler.HEAD_NAME, this.repoData.getHeadRevision());
                    foundSignals.add(0, head);
                }
                this.log.info("Search call completed.");
            } catch (IOException e) {
                this.log.info("Search request failed: " + e.getMessage());
                foundSignals = new ArrayList<GenericContent>();
            }
            return foundSignals;
        } catch (RepositoryStateException e) {
            throw new EDBException(e.getMessage(), e);
        }
    }

    // FIXME dependent on the real file tree and git folder structure
    public List<GenericContent> queryNodes(List<String> query) throws EDBException {

        File file = this.repoData.getRepositoryBase();
        boolean found = false;
        // iterate to end of path
        for (String elem : query) {
            found = false;
            File[] files = file.listFiles();
            for (File candidate : files) {
                if (candidate.isDirectory() && candidate.getName().equalsIgnoreCase(elem)) {
                    file = candidate;
                    found = true;
                    break;
                }
            }

            if (!found)
                return Collections.emptyList();
        }

        List<GenericContent> result = new ArrayList<GenericContent>();
        // save directories at the end of the path
        File[] files = file.listFiles();
        for (File candidate : files) {
            if (candidate.isDirectory() && !candidate.getName().equals(GIT_CONFIG)) {
                GenericContent gc = new GenericContent();
                gc.setProperty(ELEM_NAME, candidate.getName());
                result.add(gc);
            }
        }
        return result;
    }

    public synchronized EDBHandler add(List<GenericContent> content) throws EDBException {
        try {
            this.log.trace("Adding uuid to elements having none...");
            Iterator<GenericContent> iterGC = content.iterator();
            GenericContent tmpGC;
            while (iterGC.hasNext()) {
                tmpGC = iterGC.next();
                if (tmpGC.getUUID() == null) {
                    tmpGC.setUUID(UUID.randomUUID().toString());
                }
            }
            this.log.trace("Storing data commit for index store...");
            this.factoryIndex.createNewIndexer(this.repoSearch).addDocuments(content).cleanup();

            this.log.trace("Storing data commit for data store...");
            for (GenericContent con : content) {
                con.store();
            }
            this.log.trace("Creating commit for data store...");
            this.commitData = this.repoData.prepareCommit();
            this.commitData.add(content.toArray(new GenericContent[] {}));

            this.log.trace("Creating commit for index store...");
            this.commitSearch = this.repoSearch.prepareCommit();
            this.commitSearch.add(IO.recursiveScanDirectoryForFiles(
                    this.factoryIndex.createNewSearcher(this.repoSearch).getBaseIndex()).toArray(new File[] {}));

            this.log.trace("Commit preparations done.");

        } catch (IOException e) {
            resetToCurrent();
            throw new EDBException("Activity rolled back." + e.getMessage(), e);
        }

        return this;
    }

    public synchronized EDBHandler remove(List<GenericContent> content) throws EDBException {
        try {
            this.log.trace("Storing data commit for index store...");
            Indexer indexer = this.factoryIndex.createNewIndexer(this.repoSearch);

            this.log.trace("Filtering out elements with no UUID...");
            Iterator<GenericContent> iterGC = content.iterator();
            while (iterGC.hasNext()) {
                if (iterGC.next().getUUID() == null) {
                    iterGC.remove();
                }
            }
            this.log.trace("Creating commit for data store...");
            this.commitData = this.repoData.prepareCommit();
            this.commitData.add(content.toArray(new GenericContent[] {}));

            for (GenericContent con : content) {
                indexer.removeDocument(con);

                if (!con.getFileLocation().delete()) {
                    // TODO possible concurrency issue (removal of a resource
                    // causes abort)
                    throw new IOException("Deleting failed for '" + con.getFileLocation() + "'");
                }
            }

            indexer.commit();

            this.log.trace("Creating commit for index store...");
            this.commitSearch = this.repoSearch.prepareCommit();
            this.commitSearch.add(IO.recursiveScanDirectoryForFiles(
                    this.factoryIndex.createNewSearcher(this.repoSearch).getBaseIndex()).toArray(new File[] {}));

            this.log.trace("Commit preparations done.");

        } catch (IOException e) {
            resetToCurrent();
            throw new EDBException("Activity rolled back.", e);
        }

        return this;
    }

    public synchronized String reset(String headInfo, int steps) throws EDBException {
        try {
            if (this.repoData.getHeadRevision().equals(headInfo)) {
                this.log.trace("Reseting index store by " + steps + " steps...");
                this.repoSearch.prepareReset().setDepth(steps).setMode(DefaultEDBHandler.MODE_HARD).reset();

                this.log.trace("Reseting data store by " + steps + " steps...");
                String headId = this.repoData.prepareReset().setDepth(steps).setMode(DefaultEDBHandler.MODE_HARD)
                        .reset();

                this.log.info("Reset successful.");
                return headId;
            } else {
                throw new EDBException("Reset aborted. (HeadId does not match repository final head revision)");
            }
        } catch (RepositoryStateException e) {
            throw new EDBException(e.getMessage(), e);
        }
    }

    public synchronized EDBHandler resetToCurrent() throws EDBException {
        try {
            this.log.trace("Cleaning index store from pending changes...");
            this.repoSearch.prepareReset().setDepth(0).setMode(DefaultEDBHandler.MODE_HARD).reset();

            this.log.trace("Cleaning data store from pending changes...");
            this.repoData.prepareReset().setDepth(0).setMode(DefaultEDBHandler.MODE_HARD).reset();

            this.commitData = null;
            this.commitSearch = null;
            this.log.trace("Cleanup of pending changes done.");
            return this;
        } catch (RepositoryStateException e) {
            throw new EDBException(e.getMessage(), e);
        }
    }

    public synchronized String commit(String user, String email) throws EDBException {
        if (this.commitData == null && this.commitSearch == null) {
            this.commitData = this.repoData.prepareCommit();
            this.commitSearch = this.repoSearch.prepareCommit();
        }

        if ((this.commitData == null && this.commitSearch != null)
                || (this.commitData != null && this.commitSearch == null)) {
            throw new EDBException("Inconsistent index state.");
        }

        try {
            this.commitSearch.setAuthor(user, email).setMessage(DefaultEDBHandler.DEFAULT_MSG).commit();
            String headId;

            try {
                headId = this.commitData.setAuthor(user, email).setMessage(DefaultEDBHandler.DEFAULT_MSG).commit();

                this.log.info("Commit " + headId + " performed successfully.");

                this.commitSearch = null;
                this.commitData = null;

                return headId;

                // catch exception of data commit
            } catch (RepositoryStateException e) {

                try {
                    this.repoSearch.prepareReset().setDepth(1).setMode(DefaultEDBHandler.MODE_HARD).reset();
                    // catch exception of search index reset
                } catch (RepositoryStateException e1) {
                    // now we screwed up
                    this.log.fatal(e1);
                    throw new EDBException("Rollback failed, data integrity damaged.", e1);
                }
                throw new EDBException(e.getMessage(), e);
            }

            // catch exception of search index commit
        } catch (RepositoryStateException e) {
            throw new EDBException(e.getMessage(), e);
        }
    }

    public synchronized EDBHandler setFactoryDataRepo(RepositoryFactory factoryDataRepo) {
        this.factoryDataRepo = factoryDataRepo;
        return this;
    }

    public synchronized EDBHandler setFactoryIndex(IndexFactory factoryIndex) {
        this.factoryIndex = factoryIndex;
        return this;
    }

    public synchronized EDBHandler setFactoryIndexRepo(RepositoryFactory factoryIndexRepo) {
        this.factoryIndexRepo = factoryIndexRepo;
        return this;
    }

    public synchronized String getHeadInfo() {
        return this.repoData.getHeadRevision();
    }

    public synchronized File getRepositoryBase() {
        return this.repoData.getRepositoryBase();
    }

    public void removeRepository() {
        File edbBaseDir = repoData.getRepositoryBase().getParentFile();
        this.repoData.removeRepository();
        this.repoSearch.removeRepository();
        edbBaseDir.delete();
    }
}
