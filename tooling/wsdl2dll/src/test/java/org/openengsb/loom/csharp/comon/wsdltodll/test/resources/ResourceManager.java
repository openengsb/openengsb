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

package org.openengsb.loom.csharp.comon.wsdltodll.test.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceManager {
    private File windowsExampleDoamin0;
    private File windowsExampleDoamin1;
    private File linuxExampleDoamin0;
    private File linuxExampleDoamin1;
    private File testCsClass;

    public ResourceManager() {
        testCsClass = new File(getClass().getResource("TestCSClass.cs").getFile());
        windowsExampleDoamin0 = new File(getClass().getResource("WindowsExampleDomain0.cs").getFile());
        windowsExampleDoamin1 = new File(getClass().getResource("WindwosExampleDomain1.cs").getFile());
        linuxExampleDoamin0 = new File(getClass().getResource("LinuxExampleDomain0.cs").getFile());
        linuxExampleDoamin1 = new File(getClass().getResource("LinuxExampleDomain1.cs").getFile());

    }

    public File getWindowsExampleDoamin0() throws IOException {
        return createTmpFile(windowsExampleDoamin0);
    }

    public File getWindowsExampleDoamin1() throws IOException {
        return createTmpFile(windowsExampleDoamin1);
    }

    public File getTestCsClass() throws IOException {
        return createTmpFile(testCsClass);
    }

    public File getLinuxExampleDoamin0() throws IOException {
        return createTmpFile(linuxExampleDoamin0);
    }

    public File getLinuxExampleDoamin1() throws IOException {
        return createTmpFile(linuxExampleDoamin1);
    }

    public File createTmpFile(File file) throws IOException {
        File result = File.createTempFile(file.getName(), "");
        InputStream in = new FileInputStream(file);
        OutputStream out = new FileOutputStream(result);
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        return result;
    }
}
