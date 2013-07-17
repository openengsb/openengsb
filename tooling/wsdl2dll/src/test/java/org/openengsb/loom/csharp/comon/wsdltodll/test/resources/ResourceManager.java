package org.openengsb.loom.csharp.comon.wsdltodll.test.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public File getWindowsExampleDoamin0() throws FileNotFoundException, IOException {
        return createTmpFile(windowsExampleDoamin0);
    }

    public File getWindowsExampleDoamin1() throws FileNotFoundException, IOException {
        return createTmpFile(windowsExampleDoamin1);
    }

    public File getTestCsClass() throws FileNotFoundException, IOException {
        return createTmpFile(testCsClass);
    }

    public File getLinuxExampleDoamin0() throws FileNotFoundException, IOException {
        return createTmpFile(linuxExampleDoamin0);
    }

    public File getLinuxExampleDoamin1() throws FileNotFoundException, IOException {
        return createTmpFile(linuxExampleDoamin1);
    }

    public File createTmpFile(File file) throws FileNotFoundException, IOException {
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
