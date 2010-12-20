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

package org.openengsb.core.common;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class SingletonServiceFactory implements ServiceFactory {

    private Object service;

    public SingletonServiceFactory(Object service) {
        this.service = service;
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration registration) {
        return service;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
    }

}
