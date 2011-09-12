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

package org.openengsb.core.api.remote;

/**
 * Custom mapper interface used in case of json argument marshalling. The problem is that java does not have static type
 * information which finally requires that you add custom marshaller. For example the default marshalling process has no
 * idea about e.g. of a generic list of type X.
 *
 * To configure the custom marshaller for an argument use the {@link UseCustomJasonMarshaller} annotation at the param
 * with the classname. The two conditions which have to be fullfilled are that the class has a default constructure and
 * that it implements this interface.
 */
public interface CustomJsonMarshaller<OutputType> {

    /**
     * Map an object argument with (optional) use of the mapper into the specific object of type OutputType.
     */
    OutputType transformArg(Object arg);

}
