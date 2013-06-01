package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.nio.file.Path;
import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

public interface FileOperator
{
	/**
	 * Copies the source directory recursively to the destination directory
	 * @param sourceDirectory
	 * @param destinationDirectory 
	 */
	void copyDirectory(Path sourceDirectory, Path destinationDirectory);
	
	/**
	 * Creates a directory at the given path if it does not exist yet.
	 * @param directoryPath 
	 */
	void createDirectoryIfNotExists(Path directoryPath);
	
	/**
	 * Returns a list of tags from the given path.
	 * @param tagspath
	 * @return a list of tags from the given path.
	 */
	List<Tag> getTagsFromDirectory(Path tagsPath);
}
