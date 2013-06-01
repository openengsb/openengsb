package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.VFSFileOperator;
import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.FileOperator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

public class VFSFileOperatorTest
{
	private ResourceBundle repositoryHandlerProperties = ResourceBundle.getBundle("repositoryhandler");
	private DateFormat tagsDateFormat = new SimpleDateFormat(repositoryHandlerProperties.getString("tags_date_format"));
	
	private String testDirectoryName = "TestDirectory";
	private String testSubDirectoryName = "TestSubDirectory";
	private String testDirectoryFileName = "testFile.txt";
	private String testSubDirectoryFile1Name = "testSubFile1.txt";
	private String testSubDirectoryFile2Name = "testSubFile2.txt";
	private Path testDirectory = (new File(".")).toPath().resolve(testDirectoryName);
	private Path testSubDirectory = testDirectory.resolve(testSubDirectoryName);
	private Path testDirectoryFile = testDirectory.resolve(testDirectoryFileName);
	private Path testSubDirectoryFile1 = testSubDirectory.resolve(testSubDirectoryFile1Name);
	private Path testSubDirectoryFile2 = testSubDirectory.resolve(testSubDirectoryFile2Name);
	
	private FileOperator fileOperator = new VFSFileOperator();
	
	@Test
	public void testCopyDirectorySourceValid() throws IOException
	{
		createTestDirectory();
		
		String targetTestDirectoryName = "CopiedTestDirectory";
		Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
		Path targetTestSubDirectory = targetTestDirectory.resolve(testSubDirectoryName);
		Path targetTestestDirectoryFile = targetTestDirectory.resolve(testDirectoryFileName);
		Path targetTestSubDirectoryFile1 = targetTestSubDirectory.resolve(testSubDirectoryFile1Name);
		Path targetTestSubDirectoryFile2 = targetTestSubDirectory.resolve(testSubDirectoryFile2Name);
		
		fileOperator.copyDirectory(testDirectory, targetTestDirectory);
		
		assertTrue(Files.exists(targetTestDirectory));
		assertTrue(Files.exists(targetTestSubDirectory));
		assertTrue(Files.exists(targetTestestDirectoryFile));
		assertTrue(Files.exists(targetTestSubDirectoryFile1));
		assertTrue(Files.exists(targetTestSubDirectoryFile2));
		
		FileUtils.deleteDirectory(targetTestDirectory.toFile());
		
		deleteTestDirectory();
	}
	
	@Test
	public void testCopyDirectorySourceInvalid() throws IOException
	{
		String targetTestDirectoryName = "CopiedTestDirectory";
		Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
		Path targetTestSubDirectory = targetTestDirectory.resolve(testSubDirectoryName);
		Path targetTestestDirectoryFile = targetTestDirectory.resolve(testDirectoryFileName);
		Path targetTestSubDirectoryFile1 = targetTestSubDirectory.resolve(testSubDirectoryFile1Name);
		Path targetTestSubDirectoryFile2 = targetTestSubDirectory.resolve(testSubDirectoryFile2Name);
		
		assertFalse(Files.exists(testDirectory));
		fileOperator.copyDirectory(testDirectory, targetTestDirectory);
		
		assertFalse(Files.exists(targetTestDirectory));
		assertFalse(Files.exists(targetTestSubDirectory));
		assertFalse(Files.exists(targetTestestDirectoryFile));
		assertFalse(Files.exists(targetTestSubDirectoryFile1));
		assertFalse(Files.exists(targetTestSubDirectoryFile2));
	}
	
	@Test
	public void testCreateDirectoryIfNotExistsDestinationDoesNotExist() throws IOException
	{
		String targetTestDirectoryName = "CopiedTestDirectory";
		Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
		
		assertFalse(Files.exists(targetTestDirectory));
		
		fileOperator.createDirectoryIfNotExists(targetTestDirectory);
		assertTrue(Files.exists(targetTestDirectory));
		
		FileUtils.deleteDirectory(targetTestDirectory.toFile());
	}
	
	@Test
	public void testCreateDirectoryIfNotExistsDestinationDoesExist() throws IOException
	{
		String targetTestDirectoryName = "CopiedTestDirectory";
		Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
		Files.createDirectories(targetTestDirectory);
		
		assertTrue(Files.exists(targetTestDirectory));
		
		fileOperator.createDirectoryIfNotExists(targetTestDirectory);
		assertTrue(Files.exists(targetTestDirectory));
		
		FileUtils.deleteDirectory(targetTestDirectory.toFile());
	}
	
	@Test
	public void testGetTagsFromDirectoryEmptyDirectory() throws IOException
	{
		String tagsDirectoryName = "TagsTestDirectory";
		Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);
		
		if(Files.exists(tagsDirectory))
		{
			FileUtils.deleteDirectory(tagsDirectory.toFile());
		}
		
		assertFalse(Files.exists(tagsDirectory));
		Files.createDirectories(tagsDirectory);
		
		List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);
		
		assertNotNull(tags);
		assertEquals(0, tags.size());
		
		FileUtils.deleteDirectory(tagsDirectory.toFile());
	}
	
	@Test
	public void testGetTagsFromDirectoryTagsInDirectory() throws IOException, ParseException
	{
		String tagsDirectoryName = "TagsTestDirectory";
		Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);
		
		if(Files.exists(tagsDirectory))
		{
			FileUtils.deleteDirectory(tagsDirectory.toFile());
		}
		
		String tag1DateString = "20130102_121514";
		Date tag1Date = tagsDateFormat.parse(tag1DateString);
		String tag1Name = "tag1";
		String tag1DirectoryName = tag1DateString + "_" + tag1Name;
		Path tag1Directory = tagsDirectory.resolve(tag1DirectoryName);
		
		String tag2DateString = "20130103_121514";
		Date tag2Date = tagsDateFormat.parse(tag2DateString);
		String tag2Name = "tag1";
		String tag2DirectoryName = tag2DateString + "_" + tag2Name;
		Path tag2Directory = tagsDirectory.resolve(tag2DirectoryName);
		
		assertFalse(Files.exists(tagsDirectory));
		Files.createDirectories(tagsDirectory);
		Files.createDirectories(tag1Directory);
		Files.createDirectories(tag2Directory);
		
		List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);
		
		assertNotNull(tags);
		assertEquals(2, tags.size());
		assertEquals(tag1Date, tags.get(0).getDate());
		assertEquals(tag1Name, tags.get(0).getName());
		assertEquals(tag1Directory, tags.get(0).getPath());
		assertEquals(tag2Date, tags.get(1).getDate());
		assertEquals(tag2Name, tags.get(1).getName());
		assertEquals(tag2Directory, tags.get(1).getPath());
		
		FileUtils.deleteDirectory(tagsDirectory.toFile());
	}
	
	@Test
	public void testGetTagsFromDirectoryUnparseableTags() throws IOException
	{
		String tagsDirectoryName = "TagsTestDirectory";
		Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);
		
		String unparseableTagDirectoryName = "UnparseableTagString";
		Path unparseableTagDirectory = tagsDirectory.resolve(unparseableTagDirectoryName);
		
		if(Files.exists(tagsDirectory))
		{
			FileUtils.deleteDirectory(tagsDirectory.toFile());
		}
		
		assertFalse(Files.exists(tagsDirectory));
		Files.createDirectories(tagsDirectory);
		Files.createDirectories(unparseableTagDirectory);
		
		List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);
		
		assertNotNull(tags);
		assertEquals(0, tags.size());
		
		FileUtils.deleteDirectory(tagsDirectory.toFile());
	}
	
	private void createTestDirectory() throws IOException
	{
		if(Files.exists(testDirectory))
		{
			deleteTestDirectory();
		}
		
		Files.createDirectories(testDirectory);
		Files.createDirectories(testSubDirectory);
		createTestFile(testDirectoryFile, testDirectoryFile.toString());
		createTestFile(testSubDirectoryFile1, testSubDirectoryFile1.toString());
		createTestFile(testSubDirectoryFile2, testSubDirectoryFile2.toString());
		
		assertTrue(Files.exists(testDirectory));
		assertTrue(Files.exists(testSubDirectory));
		assertTrue(Files.exists(testDirectoryFile));
		assertTrue(Files.exists(testSubDirectoryFile1));
		assertTrue(Files.exists(testSubDirectoryFile2));
	}
	
	private void deleteTestDirectory() throws IOException
	{
		if(Files.exists(testDirectory))
		{
			FileUtils.deleteDirectory(testDirectory.toFile());
		}
		
		assertFalse(Files.exists(testDirectory));
		assertFalse(Files.exists(testSubDirectory));
		assertFalse(Files.exists(testDirectoryFile));
		assertFalse(Files.exists(testSubDirectoryFile1));
		assertFalse(Files.exists(testSubDirectoryFile2));
	}
	
	private void createTestFile(Path filePath, String content) throws IOException
	{
		FileUtils.writeStringToFile(filePath.toFile(), content);
	}
}
