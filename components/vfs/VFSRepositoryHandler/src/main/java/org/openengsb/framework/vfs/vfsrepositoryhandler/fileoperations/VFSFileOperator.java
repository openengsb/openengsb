package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VFSFileOperator implements FileOperator
{
	private final Logger logger = LoggerFactory.getLogger(VFSFileOperator.class);
	
	public void copyDirectory(Path sourceDirectory, Path destinationDirectory)
	{
		try
		{
			FileUtils.copyDirectory(sourceDirectory.toFile(), destinationDirectory.toFile());
		}
		catch (IOException e)
		{
			logger.error("Could not copy directory: " + e.getMessage());
		}
	}
	
	public void createDirectoryIfNotExists(Path directoryPath)
	{
		if(!Files.exists(directoryPath))
		{
			try
			{
				Files.createDirectories(directoryPath);
			}
			catch(IOException e)
			{
				logger.error("Could not create directory: " + e.getMessage());
			}
		}
	}

	public List<Tag> getTagsFromDirectory(Path tagsPath)
	{
		List<Tag> tags = new ArrayList<Tag>();
		//TODO handling of files that are not tags
		if(Files.exists(tagsPath))
		{
			for (File file : tagsPath.toFile().listFiles())
			{
				if (file.isDirectory())
				{
					try
					{
						Tag tag = new ConfigurationTag(file);
						tags.add(tag);
					}
					catch(ParseException e)
					{
						logger.error("Could not parse tag path: " + file.getPath());
					}
				}
			}
		}
		
		return tags;
	}
}
