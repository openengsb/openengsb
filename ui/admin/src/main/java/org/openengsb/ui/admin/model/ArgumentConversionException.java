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

package org.openengsb.ui.admin.model;

/**
 * The ArgumentConversionException is thrown if an argument could not be converted from the given string into the
 * correct object type.
 */
@SuppressWarnings("serial")
public class ArgumentConversionException extends Exception {
    private Argument argument;

    public ArgumentConversionException(String message, Argument argument) {
        super(message);
        this.argument = argument;
    }

    public ArgumentConversionException(Throwable throwable, Argument argument) {
        super(throwable);
        this.argument = argument;
    }

    public ArgumentConversionException(String message, Throwable throwable, Argument argument) {
        super(message, throwable);
        this.argument = argument;
    }

    public Argument getArgument() {
        return argument;
    }
}
