/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.openengsb.framework.vfs.api.common.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationTag implements Tag {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationTag.class);
    private ResourceBundle configurationServiceProperties = ResourceBundle.getBundle("repositoryhandler");
    DateFormat dateFormat = new SimpleDateFormat(configurationServiceProperties.getString("tags_date_format"));
    private String name;
    private Path path;
    private Date date;

    public ConfigurationTag(String name, Path path, Date date) {
        this.name = name;
        this.path = path;
        this.date = date;
    }

    public ConfigurationTag(Path path) throws ParseException {
        this.path = path;
        parseNameAndDate(path.toFile());
    }

    public ConfigurationTag(File file) throws ParseException {
        path = file.toPath();
        parseNameAndDate(file);
    }

    public String getName() {
        return name;
    }

    public Path getTagPath() {
        return path;
    }

    public Date getCreationDate() {
        return date;
    }

    public int compareTo(Tag that) {
        return date.compareTo(that.getCreationDate());
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }

        if (!(that instanceof ConfigurationTag)) {
            return false;
        }

        ConfigurationTag tag = (ConfigurationTag) that;
        return name.equals(tag.name) && date.equals(tag.date);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void parseNameAndDate(File file) throws ParseException {
        String fileName = file.getName();
        int splitIndex = fileName.indexOf("_", fileName.indexOf("_") + 1);
        name = fileName.substring(splitIndex + 1, fileName.length());

        date = dateFormat.parse(fileName);
    }
}
