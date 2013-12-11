/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
