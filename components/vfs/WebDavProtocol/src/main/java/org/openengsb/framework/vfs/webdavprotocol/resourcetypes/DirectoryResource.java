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

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.ReplaceableResource;
import io.milton.resource.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openengsb.framework.vfs.webdavprotocol.common.ChildUtils;

public class DirectoryResource extends AbstractResource implements
        MakeCollectionableResource, ReplaceableResource, DeletableResource,
        MoveableResource, IResourceFileType, PutableResource {

    private File file;
    private ArrayList<Resource> children;
    private Logger log = LoggerFactory.getLogger(DirectoryResource.class);
    private Date createDate;

    public DirectoryResource(File directory) {
        createDate = new Date();
        this.file = directory;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException {
        File newfile = new File(file, newName);
        DirectoryResource r = new DirectoryResource(newfile);
        newfile.mkdir();
        return r;
    }

    @Override
    public Resource child(String childName) throws BadRequestException {
        try {
            return ChildUtils.child(childName, getChildren());
        } catch (NotAuthorizedException ex) {
            java.util.logging.Logger.getLogger(DirectoryResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getUniqueId() {
        return file.getAbsolutePath();
    }

    @Override
    public Date getModifiedDate() {
        return new Date(file.lastModified());
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException {
        if (children == null) {
            children = new ArrayList<Resource>();
        }

        children.clear();


        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                if (f.getName().equals("Tags")) {
                    children.add(new DirectoryResourceReadOnly(f));
                } else {
                    children.add(new DirectoryResource(f));
                }
            } else {
                children.add(new FileResource(f));
            }
        }

        return children;
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws NotAuthorizedException {
        try {
            Files.deleteIfExists(file.toPath());
            Files.copy(in, file.toPath());
        } catch (IOException ex) {
            log.error("replace content error " + ex.getMessage());
        }
    }

    @Override
    public void delete() throws NotAuthorizedException {
        try {
            Files.delete(file.toPath());
        } catch (IOException ex) {
            log.error("delete problem " + ex.getMessage());
        }
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws NotAuthorizedException {
        if (rDest instanceof IResourceFileType) {
            try {
                File f = ((IResourceFileType) rDest).getFile();
                File target = new File(f, name);

                Files.move(file.toPath(), target.toPath());
            } catch (IOException ex) {
                log.error("Move Directory Error " + ex.getMessage());
            }
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws 
            IOException {
        log.info("Create new File");
        File f = new File(file, newName);

        Files.copy(inputStream, f.toPath());

        FileResource fileRes = new FileResource(f);
        return fileRes;
    }
}
