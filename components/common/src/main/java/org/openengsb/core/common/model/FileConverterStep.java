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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
        // TODO: create a zip file and get the stream from there

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fileInputStream.read(data);
            fileInputStream.close();

            FileWrapper wrapper = new FileWrapper();
            wrapper.setFilename(file.getName());
            wrapper.setContent(data);
            return wrapper;
        } catch (FileNotFoundException ex) {
            LOGGER.error("File " + file.getAbsolutePath() + " was not found", ex);
        } catch (IOException ex) {
            LOGGER.error("IOException while reading from filee " + file.getAbsolutePath(), ex);
        }
        return null;
    }

    @Override
    public boolean matchForGetter(Object object) {
        return object != null && object.getClass().equals(FileWrapper.class);
    }

    @Override
    public Object convertForGetter(Object object) {
        FileWrapper wrapper = (FileWrapper) object;
        File f = new File(wrapper.getFilename());
        // TODO do conversion Work
        return f;
    }

}
