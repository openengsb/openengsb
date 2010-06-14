/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IO {

    private static Log log = LogFactory.getLog(IO.class);

    /**
     * Constructs a new {@code BufferedReader} that is reading from the
     * specified file.
     */
    public static BufferedReader newBufferedReader(final String filename) throws FileNotFoundException {
        return newBufferedReader(new FileInputStream(filename));
    }

    /**
     * Constructs a new {@code BufferedReader} that is readinf from the
     * specified {@code InputStream}
     */
    public static BufferedReader newBufferedReader(final InputStream in) {
        return new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Constructs an relative path with an absolute base path and an absolute
     * path that is deeper inside the base path.
     */
    public static String relativize(final String base, final String path) {
        return new File(base).toURI().relativize(new File(path).toURI()).getPath();
    }

    /**
     * Writes a String into a file on a specific file path.
     */
    public static void write(final String toWrite, final String filePath) throws IOException {
        write(toWrite, new File(filePath));
    }

    /**
     * Writes a String into a specific file uri.
     */
    public static void write(final String toWrite, final URI fileUri) throws IOException {
        write(toWrite, new File(fileUri));
    }

    /**
     * Writes a String into a specific file.
     */
    public static void write(final String toWrite, final File file) throws IOException {
        final FileWriter fw = new FileWriter(file);
        fw.write(toWrite);
        fw.flush();
        fw.close();
    }

    /**
     * @see ClassLoader#getResource(String)
     */
    public static URL getResource(final String resourceName) {
        return ClassLoader.getSystemClassLoader().getResource(resourceName);
    }

    /**
     * @see ClassLoader#getResourceAsStream(String)
     * @param resourceName
     * @return
     */
    public static InputStream getResourceAsStream(final String resourceName) {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
    }

    /**
     * Wait for an {@code Enter} at the stdin.
     */
    public static void waitForKeyPress() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (final IOException e) {
            // no op
        }
    }

    /**
     * Copies a file from an stream to an file.
     */
    public static void copyFile(final InputStream f1, final File f2) throws IOException {
        IO.log.debug("Copy [" + f1.toString() + "] to [" + f2.getAbsolutePath() + "]...");
        final InputStream in = f1;
        final OutputStream out = new FileOutputStream(f2);

        final byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Copies one file into another one.
     */
    public static void copyFile(final File f1, final File f2) throws IOException {
        IO.log.debug("Copy [" + f1.getAbsolutePath() + "] to [" + f2.getAbsolutePath() + "]...");
        final InputStream in = new FileInputStream(f1);
        final OutputStream out = new FileOutputStream(f2);

        final byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Close quietly
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
        }
    }

    /**
     * Scans a directory recursively for files and return all files found in an
     * {@link List}.
     */
    public static List<File> recursiveScanDirectoryForFiles(final File directory) {
        final List<File> retVal = new ArrayList<File>();
        recursiveScan(directory, retVal);
        return retVal;
    }

    /**
     * Internal helper method to recursively scan a directory for files.
     */
    private static void recursiveScan(final File directory, final List<File> actualList) {
        if (!directory.isDirectory()) {
            return;
        }
        for (final File file : directory.listFiles()) {
            if (file.isDirectory()) {
                recursiveScan(file, actualList);
            } else {
                actualList.add(file);
            }
        }
    }

}
