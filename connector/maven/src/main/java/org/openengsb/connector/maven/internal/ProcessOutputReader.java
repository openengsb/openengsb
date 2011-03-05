/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.connector.maven.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class writing an inputStream (mostly from an process) always into a string and optionally into a log file, if
 * you use the {@link #ProcessOutputReader(InputStream, File)} constructor.
 */
public class ProcessOutputReader implements Callable<String> {

    private Log log = LogFactory.getLog(getClass());

    private InputStream inputStream;

    private StringWriter writer;

    private OutputStreamWriter logFileWriter;

    public ProcessOutputReader(InputStream inputStream) {
        this.inputStream = inputStream;
        writer = new StringWriter();
    }

    public ProcessOutputReader(InputStream inputStream, File logFile) throws FileNotFoundException {
        this(inputStream);
        FileOutputStream logFileOutputStream = new FileOutputStream(logFile);
        logFileWriter = new OutputStreamWriter(logFileOutputStream);
    }

    @Override
    public String call() throws IOException {
        log.debug("starting reading inputstream");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        readInputStream(bufferedReader);
        closeResources();
        log.debug("inputstream has ended. returning result");
        return writer.toString();
    }

    private void readInputStream(BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            writer.append(line + "\n");
            readToLogFile(line);
        }
    }

    private void readToLogFile(String line) throws IOException {
        if (logFileWriter != null) {
            logFileWriter.write(line + "\n");
            logFileWriter.flush();
        }
    }

    private void closeResources() throws IOException {
        log.debug("Input stream has ended. cleanup resources");
        writer.close();
        if (logFileWriter != null) {
            logFileWriter.close();
        }
    }

}
