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

package org.openengsb.framework.vfs.webdavprotocol.factories;

import io.milton.common.Path;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;
import java.io.File;
import java.util.logging.Level;

import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.webdavprotocol.resourcetypes.DirectoryResource;
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFactoryImpl implements ResourceFactory {

    private WebDavHandler webDavHandler;
    private RepositoryHandler repositoryHandler;
    private Logger log = LoggerFactory.getLogger(ResourceFactoryImpl.class);

    public ResourceFactoryImpl() {
        webDavHandler = WebDavHandler.getInstance();
        repositoryHandler = webDavHandler.getRepositoryHandler();
    }

    @Override
    public Resource getResource(String host, String url) throws NotAuthorizedException {
        log.debug("getResource: url: " + url);
        Path path = Path.path(url);
        Resource r = find(path);
        log.debug("_found: " + r + " for url: " + url + " and path: " + path);
        return r;
    }

    private Resource find(Path path) throws NotAuthorizedException {
        if (path.isRoot()) {
            log.debug("_path is root");

            DirectoryResource r = (DirectoryResource) HttpManager.request().getAttributes().get("rootResource");

            if (r == null) {
                File rootFile = null;


                rootFile = repositoryHandler.getRepositoryPath().toFile();


                log.debug("_new rootResource to path: " + rootFile.getPath());

                r = new DirectoryResource(rootFile);
                HttpManager.request().getAttributes().put("rootResource", r);
            }

            return r;
        }

        Resource rParent = find(path.getParent());

        if (rParent == null) {
            log.debug("_rParent = null");
            return null;
        }

        if (rParent instanceof CollectionResource) {
            try {
                log.debug("_rParent  instanceof CollectionResource");
                CollectionResource folder = (CollectionResource) rParent;
                return folder.child(path.getName());
            } catch (BadRequestException ex) {
                java.util.logging.Logger.getLogger(ResourceFactoryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
