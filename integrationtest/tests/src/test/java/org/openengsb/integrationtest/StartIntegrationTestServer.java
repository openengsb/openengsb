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

package org.openengsb.integrationtest;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.integrationtest.util.BaseExamConfiguration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.options.VMOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.TimeoutOption;

@RunWith(JUnit4TestRunner.class)
public class StartIntegrationTestServer {

    @Configuration
    public static Option[] configuration() {
        List<Option> baseConfiguration = BaseExamConfiguration.getBaseExamOptions("../../");
        configurePlatform(baseConfiguration);
        Option[] options = BaseExamConfiguration.convertOptionListToArray(baseConfiguration);
        return CoreOptions.options(options);
    }

    private static void configurePlatform(List<Option> baseConfiguration) {
        BaseExamConfiguration.addEntireOpenEngSBPlatform(baseConfiguration);
        baseConfiguration.add(new VMOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"));
        baseConfiguration.add(new TimeoutOption(0));
    }

    @Test
    public void testRunServer() throws Exception {
        while (true) {
            Thread.sleep(10000);
        }
    }
}
