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
 * A model entry converter step have the purpose to support the model proxy handler with its task to create the list of
 * OpenEngSBModelEntries and also with the getter calls. It is needed because there are some objects (like
 * OpenEngSBModels) which have to be converted into OpenEngSBModelWrapper while creating OpenEngSBModelEntries and which
 * have to be transformed back to OpenEngSBModels when the user wants to retrieve the OpenEngSBModel from the proxy.
 */
public interface ModelEntryConverterStep {

    /**
     * Checks if the given object is suitable for converting work when "getOpenEngSBModelObjects" of the proxy is
     * called. (e.g. an OpenEngSBModel)
     */
    boolean matchForGetModelEntries(Object object);

    /**
     * Does the converting work for the proxy when "getOpenEngSBModelObjects" is called. (e.g. OpenEngSBModel ->
     * OpenEngSBModelWrapper)
     */
    Object convertForGetModelEntries(Object object);

    /**
     * Checks if the given object is suitable for converting work when a getter of the proxy is called. (e.g. an
     * OpenEngSBModelWrapper)
     */
    boolean matchForGetter(Object object);

    /**
     * Does the converting work for the proxy when a getter is called. (e.g. OpenEngSBModelWrapper ->
     * OpenEngSBModel)
     */
    Object convertForGetter(Object object);
}
