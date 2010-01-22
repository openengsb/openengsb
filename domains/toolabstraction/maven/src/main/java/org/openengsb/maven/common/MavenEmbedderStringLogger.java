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

    private StringWriter writer;

    public MavenEmbedderStringLogger(StringWriter writer) {
        this.writer = writer;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void debug(String message, Throwable throwable) {
        if (!isDebugEnabled()) {
            return;
        }
        writer.append("DEBUG: ");
        append(message, throwable);
    }

    private void append(String message, Throwable throwable) {
        if (throwable != null) {
            writer.append("\n");
            writer.append(getStackTrace(throwable));
            writer.append("\n");
        } else {
            writer.append(message);
            writer.append("\n");
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        if (!isErrorEnabled()) {
            return;
        }
        writer.append("DEBUG: ");
        append(message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        if (!isFatalErrorEnabled()) {
            return;
        }
        writer.append("FATAL: ");
        append(message, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        if (!isInfoEnabled()) {
            return;
        }
        writer.append("INFO: ");
        append(message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        if (!isWarnEnabled()) {
            return;
        }
        writer.append("WARN: ");
        append(message, throwable);
    }

    public String getContent() {
        return writer.toString();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
