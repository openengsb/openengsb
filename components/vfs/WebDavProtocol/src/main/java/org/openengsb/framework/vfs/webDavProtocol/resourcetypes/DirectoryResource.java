/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webDavProtocol.resourcetypes;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openengsb.framework.vfs.webDavProtocol.common.ChildUtils;

/**
 *
 * @author Richard
 */
public class DirectoryResource extends AbstractResource implements MakeCollectionableResource, ReplaceableResource, DeletableResource, MoveableResource, IResourceFileType, PutableResource
{

	private File file;
	private ArrayList<Resource> children;
	private Logger log = LoggerFactory.getLogger(DirectoryResource.class);
	private Date createDate;

	public DirectoryResource(File directory)
	{
		createDate = new Date();
		this.file = directory;
	}

	public String getName()
	{
		return file.getName();
	}

	public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException
	{
		File newfile = new File(file, newName);
		DirectoryResource r = new DirectoryResource(newfile);
		newfile.mkdir();
		return r;
	}

	@Override
	public Resource child(String childName) throws BadRequestException, NotAuthorizedException
	{
		return ChildUtils.child(childName, getChildren());
	}

	@Override
	public String getUniqueId()
	{
		return file.getAbsolutePath();
	}

	@Override
	public Date getModifiedDate()
	{
		return new Date(file.lastModified());
	}

	public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException
	{
		if (children == null)
		{
			children = new ArrayList<Resource>();
		}

		children.clear();


		for (File f : file.listFiles())
		{
			if (f.isDirectory())
			{
				if (f.getName().equals("Tags"))
				{
					children.add(new DirectoryResourceReadOnly(f));
				}
				else
				{
					children.add(new DirectoryResource(f));
				}
			}
			else
			{
				children.add(new FileResource(f));
			}
		}

		return children;
	}

	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException
	{
		try
		{
			Files.deleteIfExists(file.toPath());
			Files.copy(in, file.toPath());
		}
		catch (IOException ex)
		{
			log.error("replace content error " + ex.getMessage());
		}
	}

	public void delete() throws NotAuthorizedException, ConflictException, BadRequestException
	{
		try
		{
			Files.delete(file.toPath());
		}
		catch (IOException ex)
		{
			log.error("delete problem " + ex.getMessage());
		}
	}

	public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException
	{
		if (rDest instanceof IResourceFileType)
		{
			try
			{
				File f = ((IResourceFileType) rDest).getFile();
				File target = new File(f, name);

				Files.move(file.toPath(), target.toPath());
			}
			catch (IOException ex)
			{
				log.error("Move Directory Error " + ex.getMessage());
			}
		}
	}

	public File getFile()
	{
		return file;
	}

	public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException
	{
		log.info("Create new File");
		File f = new File(file, newName);

		Files.copy(inputStream, f.toPath());

		FileResource fileRes = new FileResource(f);
		return fileRes;
	}
}
