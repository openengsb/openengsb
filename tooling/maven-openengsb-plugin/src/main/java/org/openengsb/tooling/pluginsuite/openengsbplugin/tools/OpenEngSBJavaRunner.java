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

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import java.io.IOException;

import org.ops4j.io.Pipe;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

public class OpenEngSBJavaRunner {

    private Process process;
    private Thread shutdownHook;
    private Pipe errorStreamMapper;
    private Pipe outStreamMapper;
    private Pipe inStreamMapper;
    private Runnable shutdownRunnable;
    private String[] commandLine;

    public OpenEngSBJavaRunner(CommandLineBuilder command) {
        commandLine = command.toArray();
    }

    public synchronized void exec() throws PlatformException {
        startProcess();
        createShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        waitForExit();
    }

    private void startProcess() throws PlatformException {
        try {
            process = Runtime.getRuntime().exec(commandLine, null, null);
        } catch (IOException e) {
            throw new PlatformException("Could not start up the process", e);
        }
    }

    public void shutdown() {
        try {
            if (shutdownHook != null) {
                synchronized (shutdownHook) {
                    if (shutdownHook != null) {
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        process = null;
                        shutdownHook.run();
                        shutdownHook = null;
                    }
                }
            }
        } catch (IllegalStateException ignore) {
        }
    }

    public void waitForExit() {
        synchronized (process) {
            try {
                process.waitFor();
                shutdown();
            } catch (Throwable e) {
                shutdown();
            }
        }
    }

    private void createShutdownHook() {
        createPipes();
        createShutdownRunnable();
        shutdownHook = new Thread(shutdownRunnable, "OpenEngSB Runner Shutdown Hook");
    }

    private void createPipes() {
        errorStreamMapper = new Pipe(process.getErrorStream(), System.err).start("Error pipe");
        outStreamMapper = new Pipe(process.getInputStream(), System.out).start("Out pipe");
        inStreamMapper = new Pipe(process.getOutputStream(), System.in).start("In pipe");
    }

    private void createShutdownRunnable() {
        shutdownRunnable = new Runnable() {
            @Override
            public void run() {
                stopPipes();
                destroyProcess();
            }
        };
    }

    private void stopPipes() {
        inStreamMapper.stop();
        outStreamMapper.stop();
        errorStreamMapper.stop();
    }

    private void destroyProcess() {
        try {
            process.destroy();
        } catch (Exception ignore) {
        }
    }

}
