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

package org.openengsb.core.common.model;

/**
 * The default converter step has the purpose to just forward the objects as it get it from the parameter. This
 * converter is needed as the last item in the converter list because it matches if all other converter doesn't match.
 */
public final class DefaultConverterStep implements ModelEntryConverterStep {
    private static DefaultConverterStep instance;

    public static DefaultConverterStep getInstance() {
        if (instance == null) {
            instance = new DefaultConverterStep();
        }
        return instance;
    }

    private DefaultConverterStep() {
    }

    @Override
    public boolean matchForGetModelEntries(Object object) {
        return true;
    }

    @Override
    public Object convertForGetModelEntries(Object object) {
        return object;
    }

    @Override
    public boolean matchForGetter(Object object) {
        return true;
    }

    @Override
    public Object convertForGetter(Object object) {
        return object;
    }

}
