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

package org.openengsb.itests.util;

import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.lang.reflect.Method;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.itests.exam.models.EOModel;
import org.openengsb.itests.exam.models.SourceModelA;
import org.openengsb.itests.exam.models.SourceModelB;
import org.openengsb.itests.exam.models.SubModel;
import org.openengsb.itests.exam.models.TestModel;
import org.openengsb.itests.exam.models.TestModelProvider;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

public class AbstractModelUsingExamTestHelper extends AbstractExamTestHelper {
    private boolean providerInstalled = false;
    private String providerVersion = "1.0.0";

    protected void registerModelProvider() throws Exception {
        if (providerInstalled) {
            return;
        }
        String delegationHeader =
            String.format("%s-%s", org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_HEADER,
                org.openengsb.core.api.Constants.DELEGATION_CONTEXT_MODELS);
        TinyBundle providerTinyBundle =
            bundle()
                .add(TestModel.class)
                .add(SubModel.class)
                .add(SourceModelA.class)
                .add(SourceModelB.class)
                .add(EOModel.class)
                .add(TestModelProvider.class)
                .set(Constants.BUNDLE_ACTIVATOR, TestModelProvider.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.model.provider")
                .set(Constants.BUNDLE_VERSION, providerVersion)
                .set(Constants.EXPORT_PACKAGE, "org.openengsb.itests.exam.models")
                .set(Constants.IMPORT_PACKAGE,
                    "org.openengsb.core.api.model, org.osgi.framework, org.slf4j, "
                            + "org.openengsb.labs.delegation.service")
                .set(delegationHeader, "org.openengsb.itests.exam.models.*")
                .set(org.openengsb.core.api.Constants.PROVIDE_MODELS_HEADER, "true");
        Bundle providerBundle =
            getBundleContext().installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        providerInstalled = true;
    }

    protected ModelDescription getTestModelDescription() {
        return new ModelDescription(TestModel.class.getName(), new Version(providerVersion));
    }

    protected Class<?> getTestModel() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadTestModel").invoke(provider);
    }

    protected ModelDescription getSubModelDescription() {
        return new ModelDescription(SubModel.class.getName(), new Version(providerVersion));
    }

    protected Class<?> getSubModel() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadSubModel").invoke(provider);
    }

    protected ModelDescription getSourceModelADescription() {
        return new ModelDescription(SourceModelA.class.getName(), new Version(providerVersion));
    }

    protected Class<?> getSourceModelA() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadSourceModelA").invoke(provider);
    }

    protected ModelDescription getSourceModelBDescription() {
        return new ModelDescription(SourceModelB.class.getName(), new Version(providerVersion));
    }

    protected Class<?> getSourceModelB() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadSourceModelB").invoke(provider);
    }

    protected ModelDescription getEOModelDescription() {
        return new ModelDescription(EOModel.class.getName(), new Version(providerVersion));
    }

    protected Class<?> getEOModel() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadEOModel").invoke(provider);
    }

    private Object loadTestModelProvider() throws Exception {
        String filter = String.format("(%s=%s)", Constants.OBJECTCLASS, TestModelProvider.class.getName());
        Filter osgiFilter = FrameworkUtil.createFilter(filter);
        ServiceTracker tracker = new ServiceTracker(getBundleContext(), osgiFilter, null);
        tracker.open(true);
        return tracker.waitForService(4000);
    }

    protected void setProperty(Object model, String methodName, Object... params) throws Exception {
        Class<?>[] classes = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getClass();
        }
        try {
            Method method = model.getClass().getMethod(methodName, classes);
            method.invoke(model, params);
        } catch (Exception e) {
            for (Method method : model.getClass().getMethods()) {
                if (method.getName().equals(methodName)) {
                    method.invoke(model, params);
                    break;
                }
            }
        }
    }

    protected Object getProperty(Object model, String methodName) throws Exception {
        try {
            Method method = model.getClass().getMethod(methodName);
            return method.invoke(model);
        } catch (Exception e) {
            throw new IllegalArgumentException("There is no method " + methodName
                    + " for the class " + model.getClass().getName());
        }
    }
}
