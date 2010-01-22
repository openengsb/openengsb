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
package org.openengsb.maven.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.maven.embedder.AbstractMavenEmbedderLogger;

public class MavenEmbedderStringLogger extends AbstractMavenEmbedderLogger {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void close() {
        // do nothing
    }

    public void clear() {
        sb.setLength(0);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        sb.append("DEBUG: ");
        append(message, throwable);
    }

    private void append(String message, Throwable throwable) {
        if (throwable != null) {
            sb.append("\n");
            sb.append(getStackTrace(throwable));
            sb.append("\n");
        } else {
            sb.append(message);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        sb.append("DEBUG: ");
        append(message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        sb.append("FATAL: ");
        append(message, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        sb.append("INFO: ");
        append(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        sb.append("WARN: ");
        append(message, throwable);
    }

    public String getContent() {
        return sb.toString();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
