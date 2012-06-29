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

package org.openengsb.core.weaver.service.internal;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.weaver.service.internal.model.ManipulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;

/**
 * The model agent is a java agent implementation, which initiate the model weaving of models.
 */
public class ModelAgent implements ClassFileTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ModelAgent());
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain domain,
            byte[] bytecode)
        throws IllegalClassFormatException {

        if (!shouldBeEnhanced(className)) {
            return bytecode;
        }
        try {
            byte[] result = ManipulationUtils.enhanceModel(bytecode);
            return result != null ? result : bytecode;
        } catch (IOException e) {
            LOGGER.error("IOException while enhancing model", e);
        } catch (CannotCompileException e) {
            LOGGER.error("CannotCompileException while enhancing model", e);
        }
        return bytecode;
    }

    private boolean shouldBeEnhanced(String className) {
        List<String> filter = Arrays.asList("java", "$", "sun", "org/junit");
        for (String element : filter) {
            if (className.startsWith(element)) {
                return false;
            }
        }
        return true;
    }
}
