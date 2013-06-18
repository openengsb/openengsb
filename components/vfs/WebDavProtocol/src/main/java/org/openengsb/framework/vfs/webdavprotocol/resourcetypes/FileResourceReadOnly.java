/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.resourcetypes;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import java.io.File;
import java.io.InputStream;

/**
 *
 * @author Richard
 */
public class FileResourceReadOnly extends FileResource {

    public FileResourceReadOnly(File file) {
        super(file);
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws 
            BadRequestException {
        throw new BadRequestException("Tag folder is read only");
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws
            ConflictException {
        throw new ConflictException("Tag folder is read only");
    }

    @Override
    public void delete() throws NotAuthorizedException {
        throw new NotAuthorizedException("Tag folder is read only", this);
    }
}
