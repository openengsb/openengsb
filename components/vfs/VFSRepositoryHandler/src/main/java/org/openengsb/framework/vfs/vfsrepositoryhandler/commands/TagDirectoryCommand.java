package org.openengsb.framework.vfs.vfsrepositoryhandler.commands;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.openengsb.framework.vfs.vfsrepositoryhandler.VFSRepositoryHandler;

import java.io.File;
import java.nio.file.Path;

@Command(scope = "repositoryhandler", name = "tagDirectory", description = "Tags a directory")
public class TagDirectoryCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "path", description = "The path of the Configuration that should be tagged",
            required = true, multiValued = false)
    String pathString;
    @Argument(index = 1, name = "tagName", description = "The name of the created tag",
            required = true, multiValued = false)
    String tagName;

    @Override
    protected Object doExecute() throws Exception {
        //TODO error handling
        //TODO proper path conversion
        Path path = (new File(pathString)).toPath();
        System.out.println(path.toAbsolutePath());
        VFSRepositoryHandler.getInstance().tagDirectory(path, tagName);
        return null;
    }
}
