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

import org.openengsb.core.weaver.service.internal.model.ManipulationUtils;
import org.openengsb.labs.delegation.service.DelegationClassLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;

public class ModelWeaver implements WeavingHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelWeaver.class);

    public ModelWeaver(BundleContext context) {
        ManipulationUtils.appendClassLoader(new DelegationClassLoader(context));
        ManipulationUtils.appendClassLoader(ModelWeaver.class.getClassLoader());
    }

    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();
        if (className.equals("org.openengsb.core.api.model.annotation.Model")
                || className.contains("javassist") || className.contains("JavassistHelper")) {
            return;
        }
        try {
            LOGGER.debug("try to enhance {}", className);
            byte[] result = doActualWeaving(wovenClass.getBytes(), wovenClass.getBundleWiring().getClassLoader());
            if (result != null) {
                wovenClass.getDynamicImports().add("org.openengsb.core.api.model");
                wovenClass.setBytes(result);
            }
            LOGGER.debug("finished enhancing {}", className);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    private byte[] doActualWeaving(byte[] wovenClass, ClassLoader... loaders) throws IOException,
        CannotCompileException {
        return ManipulationUtils.enhanceModel(wovenClass, loaders);
    }

}
