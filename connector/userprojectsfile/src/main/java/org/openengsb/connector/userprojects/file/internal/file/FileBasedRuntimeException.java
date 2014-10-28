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
package org.openengsb.connector.userprojects.file.internal.file;

/**
 * Represents an unchecked exception that can occur within the context of file-based user data manager.
 *
 */
public class FileBasedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 6267165144631084590L;

    public FileBasedRuntimeException() {
        super();
    }

    public FileBasedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileBasedRuntimeException(String message) {
        super(message);
    }

    public FileBasedRuntimeException(Throwable cause) {
        super(cause);
    }

}
