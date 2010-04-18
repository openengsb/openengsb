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

/**
 * 
 */
package org.openengsb.edb.core.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.util.Prelude;

/**
 * Thin wrapper around {@link java.util.Properties} offering a UUID and a path
 * (elements separated by "/"). This class hides most operations of the
 * underlying data structure to simplify and standardize its usage.
 */
public class GenericContent {

    private Log log = LogFactory.getLog(getClass());

    public static final String PATH_NAME = "path";
    public static final String UUID_NAME = "uuid";

    private File fileLocation;

    private Properties content;

    public GenericContent() {
        this.content = new Properties();
    }

    public GenericContent(String repositoryBase, String[] abstractPath, String[] realPath) {
        this(repositoryBase, abstractPath, realPath, UUID.randomUUID());
    }

    public GenericContent(String repositoryBase, String[] abstractPath, String[] realPath, UUID uuid) {
        File folder = new File(repositoryBase + File.separator + Prelude.pathize(realPath));
        prepareDirectoryStructure(folder);

        this.fileLocation = new File(repositoryBase + File.separator + Prelude.pathize(realPath) + File.separator
                + uuid.toString());

        this.log.trace("Creating property object for generic content...");
        this.content = new Properties();
        addPathEntriesAsSingleProperties(abstractPath, realPath);

        setProperty(GenericContent.PATH_NAME, Prelude.pathize(abstractPath));
        setProperty(GenericContent.UUID_NAME, uuid.toString());
    }

    private void prepareDirectoryStructure(File folder) {
        if (!folder.exists()) {
            this.log.debug("Folders does not exist creating strucutre [" + folder.getAbsolutePath() + "]...");
            folder.mkdirs();
        }
    }

    private void addPathEntriesAsSingleProperties(String[] abstractPath, String[] realPath) {
        for (int j = 0; j < abstractPath.length; j++) {
            if (realPath.length > j) {
                setProperty(abstractPath[j], realPath[j]);
            }
        }
    }

    /**
     * Calls {@link java.util.Properties#store(Writer, String)} with the UUID as
     * comment.
     * 
     * @param out - Writer, destination to store into
     * @throws IOException see Properties javadoc
     */
    public void store() {
        this.log.debug("Writing property list with path [" + this.fileLocation.getAbsolutePath() + "]...");
        try {
            FileOutputStream ouputStr = new FileOutputStream(this.fileLocation);
            this.content.store(ouputStr, "GenericContent");
            ouputStr.close();
        } catch (FileNotFoundException e) {
            this.log.fatal("File location can not be accessed [" + this.fileLocation.getAbsolutePath() + "]...", e);
            throw new RuntimeException("File can not be accessed [" + this.fileLocation.getAbsolutePath() + "]...", e);
        } catch (IOException e) {
            this.log.fatal("File location can not be accessed [" + this.fileLocation.getAbsolutePath() + "]...", e);
            throw new RuntimeException("File can not be accessed [" + this.fileLocation.getAbsolutePath() + "]...", e);
        }
    }

    /**
     * Retrieve a value associated with the given key
     * 
     * @param key - identifier for one property
     */
    public String getProperty(String key) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Get from [" + getPath() + "] with [" + getUUID() + "] key [" + key + "] value ["
                    + this.content.getProperty(key) + "]...");
        }
        return this.content.getProperty(key);
    }

    /**
     * Add a given property (key-value pair) or overwrite an existing one. It is
     * allowed to store null values, but not recommended.
     * 
     * @param key - identifier for one property
     * @param value - value held by the given key
     */
    public void setProperty(String key, String value) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("Set into [" + getPath() + "] with [" + getUUID() + "] key [" + key + "] value [" + value
                    + "]...");
        }
        this.content.setProperty(key, value);
    }

    public void setUUID(String uuid) {
        setProperty(GenericContent.UUID_NAME, uuid);
    }

    /**
     * Returns the unique identifier of the {@link GenericContent} object as a
     * string.
     */
    public String getUUID() {
        return this.content.getProperty(GenericContent.UUID_NAME);
    }

    public void setPath(String path) {
        setProperty(GenericContent.PATH_NAME, path);
    }

    /**
     * Returns the specific path of the object (this is not the full path
     * starting with C: (in windows for example) but the path of the object
     * which means it may start with project/user and so on.
     */
    public String getPath() {
        return this.content.getProperty(GenericContent.PATH_NAME);
    }

    /**
     * Method to retrieve the full location of the property file.
     */
    public File getFileLocation() {
        return this.fileLocation;
    }

    /**
     * Returns the entire content stored in the propery map at once.
     */
    public Set<Entry<Object, Object>> getEntireContent() {
        return this.content.entrySet();
    }

    @Override
    public String toString() {
        return getEntireContent().toString();
    }
}
