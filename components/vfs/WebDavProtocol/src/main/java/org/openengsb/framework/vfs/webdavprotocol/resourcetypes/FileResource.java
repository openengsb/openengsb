/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.resourcetypes;

import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.ReplaceableResource;
import io.milton.resource.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class FileResource extends AbstractResource implements 
        GetableResource, ReplaceableResource, MoveableResource, CopyableResource,
        DeletableResource, IResourceFileType {

    private Logger log = LoggerFactory.getLogger(FileResource.class);
    private File file;
    private ArrayList<Resource> children;

    public FileResource(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    public CollectionResource createCollection(String newName) {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range,
            Map<String, String> params, String contentType) throws IOException {
        Files.copy(file.toPath(), out);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "file";
    }

    @Override
    public Long getContentLength() {
        return file.length();
    }

    
    @Override
    public void replaceContent(InputStream in, Long length) throws
        BadRequestException {
        try {
            Files.deleteIfExists(file.toPath());
            Files.copy(in, file.toPath());
        } catch (IOException ex) {
            log.error("replace content error " + ex.getMessage());
        }
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws
            ConflictException {
        log.debug("Move File");
        if (rDest instanceof IResourceFileType) {
            File des = ((IResourceFileType) rDest).getFile();
            des = new File(des, name);
            try {
                Files.move(file.toPath(), des.toPath());
                //Files.move(((IResourceFileType) rDest).getFile().toPath(), new File(name).toPath());
            } catch (IOException ex) {
                log.error("file move problem " + ex.getMessage());
            }
        }
    }

    @Override
    public void copyTo(CollectionResource toCollection, String name) throws
            NotAuthorizedException {
        log.debug("Copy File");
        if (toCollection instanceof IResourceFileType) {
            File des = ((IResourceFileType) toCollection).getFile();
            des = new File(des, name);
            try {
                Files.copy(file.toPath(), des.toPath());
                //Files.move(((IResourceFileType) rDest).getFile().toPath(), new File(name).toPath());
            } catch (IOException ex) {
                log.error("file move problem " + ex.getMessage());
            }
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
    public File getFile() {
        return file;
    }

    @Override
    public String getUniqueId() {
        return file.getAbsolutePath();
    }

    @Override
    public Date getModifiedDate() {
        return new Date(file.lastModified());
    }
}
