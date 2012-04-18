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
package org.openengsb.ui.common.resources.js;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public final class CommonJsLocator {

    public static ResourceReference getJqueryJs() {
        return new PackageResourceReference(CommonJsLocator.class, "jquery-1.7.1.min.js");
    }

    public static ResourceReference getJqueryUi() {
        return new PackageResourceReference(CommonJsLocator.class, "jquery-ui-1.8.17.custom.min.js");
    }

    public static ResourceReference getJqueryHelper() {
        return new PackageResourceReference(CommonJsLocator.class, "jqueryHelper.js");
    }
    
    private CommonJsLocator() {
    }
}
