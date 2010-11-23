/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.maven.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StreamReader extends Thread {

    private Log log = LogFactory.getLog(getClass());

    private InputStream inputStream;

    private StringWriter writer;

    public StreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
        writer = new StringWriter();
    }

    @Override
    public void run() {
        try {
            IOUtils.copy(inputStream, writer);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public String getString() {
        String value = writer.toString();
        IOUtils.closeQuietly(writer);
        return value;
    }
}
