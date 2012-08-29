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

package org.openengsb.core.api.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * The FileWrapper class is needed for the proper sending of File objects (with Jason).
 */
public class FileWrapper {
    private File file;
    private byte[] content;
    private String filename;
    private int buffer = 2048;
    private String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "OpenEngSBModelTemp";

    public FileWrapper() {
    }

    public FileWrapper(File file) {
        this.file = file;
    }

    public FileWrapper(byte[] content, String filename) {
        this.content = content;
        this.filename = filename;
    }

    public File returnFile() throws IOException {
        if (file == null) {
            deserialize();
        }
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        content = null;
        filename = null;
    }

    public byte[] getContent() throws IOException {
        if (content == null) {
            serialize();
        }
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        if (filename == null) {
            return file.getName();
        }
        return filename;
    }

    public void serialize() throws IOException {
        File zipFile = zipFile(file);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(zipFile);
            byte[] data = new byte[(int) zipFile.length()];
            fileInputStream.read(data);

            this.filename = file.getName();
            this.content = data;
            FileUtils.forceDelete(zipFile);
        } finally {
            if (fileInputStream != null) {
                IOUtils.closeQuietly(fileInputStream);
            }
        }
        file = null;
    }

    public void deserialize() throws IOException {
        File f = new File(tempDir + File.separator + filename + ".zip");
        FileOutputStream stream = null;
        try {
            f.createNewFile();
            stream = new FileOutputStream(f);
            IOUtils.write(content, stream);
            stream.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }

        file = unzipFile(f, filename);
        content = null;
        filename = null;
    }

    private File zipFile(File file) throws IOException {
        File result = new File(tempDir + File.separator + file.getName() + ".zip");
        result.getParentFile().mkdir();
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(result)));
            byte[] data = new byte[buffer];

            File[] files;
            if (file.isDirectory()) {
                files = file.listFiles();
            } else {
                files = new File[1];
                files[0] = file;
            }

            for (File f : files) {
                FileInputStream fi = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fi, buffer);
                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = bis.read(data, 0, buffer)) != -1) {
                    out.write(data, 0, count);
                }
                bis.close();
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
        return result;
    }

    private File unzipFile(File file, String fileName) throws IOException {
        String parentPath = file.getParentFile().getAbsolutePath();
        File result = new File(parentPath + File.separator + fileName);

        BufferedOutputStream dest = null;
        BufferedInputStream is = null;

        ZipEntry entry;
        ZipFile zipfile = new ZipFile(file);
        int entryCount = zipfile.size();
        @SuppressWarnings("rawtypes")
        Enumeration e = zipfile.entries();
        String elementPath = "";
        if (entryCount == 1) {
            elementPath = parentPath;
        } else {
            result.mkdir();
            elementPath = parentPath + File.separator + fileName;
        }

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            is = new BufferedInputStream(zipfile.getInputStream(entry));
            int count;
            byte[] data = new byte[buffer];
            FileOutputStream fos = new FileOutputStream(elementPath + File.separator + entry.getName());
            dest = new BufferedOutputStream(fos, buffer);
            while ((count = is.read(data, 0, buffer)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            is.close();
        }
        FileUtils.forceDelete(file);
        return result;
    }
}
