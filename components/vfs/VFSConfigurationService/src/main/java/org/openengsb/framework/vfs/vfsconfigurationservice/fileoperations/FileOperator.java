package org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations;

import java.io.File;
import java.util.List;

public interface FileOperator
{
	List<String> compareFolders(File originalPath, File newPath);
	void fileDelete(File srcFile);
	
	/**
	 * Copies the original directory recursively to the new directory
	 * @param originalPath
	 * @param newPath 
	 */
	void copyDirectory(File originalPath, File newPath);
}
