package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import org.junit.Test;
import org.junit.Assert;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

public class ConfigurationTagTest {

    @Test
    public void testConstructorWithUnparseablePath() throws IOException {
        Path unparseablePath = (new File(".")).toPath().resolve("UnparseableTagFolderName");

        try {
            Tag tag = new ConfigurationTag(unparseablePath);
        } catch (ParseException e) {
            //This exception is expected to be thrown;
            Assert.assertTrue(true);
            return;
        }

        Assert.assertTrue("Error: ParseException has not been thrown!", false);
    }
}
