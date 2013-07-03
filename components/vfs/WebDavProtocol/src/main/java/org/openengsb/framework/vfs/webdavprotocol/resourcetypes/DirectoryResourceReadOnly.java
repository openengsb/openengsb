/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author Richard
 */
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
