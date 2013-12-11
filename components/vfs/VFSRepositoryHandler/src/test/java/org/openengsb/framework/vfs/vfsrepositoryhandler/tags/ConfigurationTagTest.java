package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import org.junit.Test;
import org.openengsb.framework.vfs.api.common.Tag;

public class ConfigurationTagTest {

    @Test(expected = ParseException.class)
    public void testConstructorWithUnparseablePath() throws IOException, ParseException {
        Path unparseablePath = (new File(".")).toPath().resolve("UnparseableTagFolderName");

        Tag tag = new ConfigurationTag(unparseablePath);
    }
}
