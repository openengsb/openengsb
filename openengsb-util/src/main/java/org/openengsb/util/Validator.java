/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.util;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

public class Validator {
    private final boolean forceValidation;
    private StringBuilder errorMessage;

    Validator(boolean forceValidation) {
        this.forceValidation = forceValidation;
    }

    /**
     * Checks the given validation and, if forced, throws an {@code
     * IllegalArgumentException}.
     * 
     * @see Validate
     */
    public <T> Validator that(String actualName, T actual, Matcher<T> matcher) {
        if (!matcher.matches(actual)) {
            appendError(actualName, actual, matcher);
        }
        return this;
    }

    /**
     * Checks the given validation and, if forced, throws an {@code
     * IllegalArgumentException}.
     * 
     * @see Validate
     */
    public <T> Validator that(T actual, Matcher<T> matcher) {
        return that(null, actual, matcher);
    }

    /**
     * If validated lazily, this checks for failed validations and throws an
     * {@code IllegalArgumentException} accordingly. The error message contains
     * information about the failed validation.
     * 
     * @see Validate
     */
    public Validator check() {
        if (this.errorMessage != null) {
            throw new IllegalArgumentException(this.errorMessage.toString());
        }
        return this;
    }

    private <T> void appendError(String actualName, T actual, Matcher<T> matcher) {
        Description desc = new StringDescription();
        if (actualName != null && actualName.length() > 0) {
            desc.appendText(actualName).appendText("\n    ");
        }
        desc.appendText("Expected: ");
        matcher.describeTo(desc);
        desc.appendText("\n    Got: ").appendValue(actual).appendText("\n\n");
        if (this.forceValidation) {
            throw new IllegalArgumentException(desc.toString());
        }
        if (this.errorMessage == null) {
            this.errorMessage = new StringBuilder();
        }
        this.errorMessage.append(desc.toString());
    }
}