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

package org.openengsb.core.test;

import java.io.File;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;

public abstract class AbstractOpenEngSBTest {

    private static final String LOG4J_PROPERTIES_LOCAL = "log4j.local.properties";

    @BeforeClass
    public static void adaptLoggerPreferences() throws Exception {
        URL log4jLocalFile = ClassLoader.getSystemResource(LOG4J_PROPERTIES_LOCAL);
        if (log4jLocalFile == null) {
            return;
        }
        if (new File(log4jLocalFile.toURI()).exists()) {
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(log4jLocalFile);
        }
    }

    protected AbstractOpenEngSBTest() {
    }
}
