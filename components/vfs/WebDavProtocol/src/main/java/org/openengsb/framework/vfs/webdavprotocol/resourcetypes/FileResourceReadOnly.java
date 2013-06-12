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
public class FileResourceReadOnly extends FileResource
{

	public FileResourceReadOnly(File file)
	{
		super(file);
	}

	@Override
	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException
	{
		throw new NotAuthorizedException("Tag folder is read only", this);
	}

	@Override
	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException
	{
		throw new NotAuthorizedException("Tag folder is read only", this);
	}

	@Override
	public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException
	{
		throw new NotAuthorizedException("Tag folder is read only", this);
	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException
	{
		throw new NotAuthorizedException("Tag folder is read only", this);
	}
}
