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
package org.openengsb.core.api.xlink.exceptions;

/**
 * This Exception is thrown if an Error occurs during the validation of an incoming XLink-Request.
 */
public class OpenXLinkException extends Exception {

    /**
     * Creates a new instance of <code>OpenXLinkException</code> without detail message.
     */
    public OpenXLinkException() {
    }

    /**
     * Constructs an instance of <code>OpenXLinkException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public OpenXLinkException(String msg) {
        super(msg);
    }
}
