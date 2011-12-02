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

package org.openengsb.core.common.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.openengsb.core.api.model.FileWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The file converter step has the purpose to does the conversion work File <-> Filewrapper in the model proxy
 * environment.
 */
public final class FileConverterStep implements ModelEntryConverterStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConverterStep.class);
    private static FileConverterStep instance;
    private static final int BUFFER = 2048;
    private static String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "OpenEngSBModelTemp";

    public static FileConverterStep getInstance() {
        if (instance == null) {
            instance = new FileConverterStep();
        }
        return instance;
    }

    private FileConverterStep() {
    }

    @Override
    public boolean matchForGetModelEntries(Object object) {
        return object != null && object.getClass().equals(File.class);
    }

    @Override
    public Object convertForGetModelEntries(Object object) {
        File file = (File) object;
        File zipFile = zipFile(file);
        try {
            FileInputStream fileInputStream = new FileInputStream(zipFile);
            byte[] data = new byte[(int) zipFile.length()];
            fileInputStream.read(data);
            fileInputStream.close();

            FileWrapper wrapper = new FileWrapper();
            wrapper.setFilename(file.getName());
            wrapper.setContent(data);
            
            zipFile.delete();
            return wrapper;
        } catch (FileNotFoundException ex) {
            LOGGER.error("File " + file.getAbsolutePath() + " was not found", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while reading from file " + file.getAbsolutePath(), ex);
        }
        return null;
    }

    private File zipFile(File file) {
        new File(tempDir).mkdir();
        File result = new File(tempDir + File.separator + file.getName() + ".zip");
        try {
            FileOutputStream dest = new FileOutputStream(result);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[BUFFER];

            File[] files;
            if (file.isDirectory()) {
                files = file.listFiles();
            } else {
                files = new File[1];
                files[0] = file;
            }

            for (File f : files) {
                FileInputStream fi = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                bis.close();
            }
            out.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error("File " + file.getAbsolutePath() + " was not found", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while zipping file " + file.getAbsolutePath(), ex);
        }
        return result;
    }

    @Override
    public boolean matchForGetter(Object object) {
        return object != null && object.getClass().equals(FileWrapper.class);
    }

    @Override
    public Object convertForGetter(Object object) {
        FileWrapper wrapper = (FileWrapper) object;
        File f = new File(tempDir + File.separator + wrapper.getFilename() + ".zip"); 
        try {
            f.createNewFile();
            FileOutputStream stream = new FileOutputStream(f);
            stream.write(wrapper.getContent());
            stream.flush();
            stream.close();
        } catch (FileNotFoundException ex) {
            LOGGER.error("File " + f.getAbsolutePath() + " could not be created", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while writing the byte array of the wrapper to file " + f.getAbsolutePath(), ex);
        }

        return unzipFile(f, wrapper.getFilename());
    }

    private File unzipFile(File file, String fileName) {
        String parentPath = file.getParentFile().getAbsolutePath();
        File result = new File(parentPath + File.separator + fileName);
        try {
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
                byte[] data = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(elementPath + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            file.delete();
        } catch (FileNotFoundException ex) {
            LOGGER.error("File " + file.getAbsolutePath() + " was not found", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while unzipping file " + file.getAbsolutePath(), ex);
        }
        return result;
    }

}
