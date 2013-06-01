package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.VFSFileOperator;

public class ConfigurationTagTest
{
	@Test
	public void testConstructorWithUnparseablePath() throws IOException
	{
		Path unparseablePath = (new File(".")).toPath().resolve("UnparseableTagFolderName");
		
		try
		{
			Tag tag = new ConfigurationTag(unparseablePath);
		}
		catch(ParseException e)
		{
			//This exception is expected to be thrown;
			assertTrue(true);
			return;
		}
		
		assertTrue("Error: ParseException has not been thrown!", false);
	}
}
