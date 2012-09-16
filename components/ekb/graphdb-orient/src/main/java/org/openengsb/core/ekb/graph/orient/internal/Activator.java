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

package org.openengsb.core.ekb.graph.orient.internal;

import java.util.Hashtable;

import org.openengsb.core.ekb.api.ModelGraph;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator for the EKB model graph bundle
 */
public class Activator implements BundleActivator {
    private OrientModelGraph modelGraph;

    @Override
    public void start(BundleContext context) throws Exception {
        modelGraph = new OrientModelGraph();
        context.registerService(ModelGraph.class, modelGraph, new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        modelGraph.shutdown();
    }
}
