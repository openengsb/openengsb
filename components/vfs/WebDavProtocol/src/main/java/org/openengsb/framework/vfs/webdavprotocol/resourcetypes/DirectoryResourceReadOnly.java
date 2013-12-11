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

package org.openengsb.framework.vfs.webdavprotocol.resourcetypes;

import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryResourceReadOnly extends DirectoryResource {

    private File file;
    private ArrayList<Resource> children;
    private Logger log = LoggerFactory.getLogger(DirectoryResource.class);

    public DirectoryResourceReadOnly(File directory) {
        super(directory);
        file = directory;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException {
        return null;
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws NotAuthorizedException {
    }

    @Override
    public void delete() throws NotAuthorizedException {
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws NotAuthorizedException {
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws 
            IOException {
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException {
        if (children == null) {
            children = new ArrayList<Resource>();
        }

        children.clear();


        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                children.add(new DirectoryResourceReadOnly(f));
            } else {
                children.add(new FileResourceReadOnly(f));
            }
        }

        return children;
    }
}
