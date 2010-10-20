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

package org.openengsb.domains.jms;

@SuppressWarnings("serial")
public class OpenENGSbProxyException extends RuntimeException {

    public OpenENGSbProxyException() {
        super();
    }

    public OpenENGSbProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenENGSbProxyException(String message) {
        super(message);
    }

    public OpenENGSbProxyException(Throwable cause) {
        super(cause);
    }

}
