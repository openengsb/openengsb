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

import org.hamcrest.Matcher;

/**
 * Validate is used to check preconditions of method parameters. On failed
 * validations a {@code IllegalArgumentException} is thrown.
 * <p>
 * Forced Validation Sample:
 * 
 * <pre>
 * public static void doSomethingWithFile(File file) {
 *     Validate.force().that(file, exists()).that(file, isFile());
 *     // method body follows here ...
 * }
 * </pre>
 * <p>
 * Lazy Validation Sample:
 * 
 * <pre>
 * public static &lt;T&gt; void copy(T[] dst, long dstOffset, T[] src, long srcOffset, long length) {
 *     Validate.that(&quot;dst&quot;, dst, notNullValue()).that(&quot;src&quot;, src, notNullValue()).that(&quot;length&quot;, length,
 *             greaterThanOrEqualTo(0L)).check().that(&quot;dst offset&quot;, dst, arrayIndexInRange(dstOffset)).that(
 *             &quot;dst offset + length&quot;, dst, arrayIndexInRange(dstOffset + length)).check();
 *     // method body follows here ...
 * }
 * </pre>
 */
public class Validate {
    /**
     * Uses lazy validation.
     * 
     * @see Validator#that(String, Object, Matcher)
     */
    public static <T> Validator that(String actualName, T actual, Matcher<T> matcher) {
        return lazy().that(actualName, actual, matcher);
    }

    /**
     * Uses lazy validation.
     * 
     * @see Validator#that(Object, Matcher)
     */
    public static <T> Validator that(T actual, Matcher<T> matcher) {
        return lazy().that(actual, matcher);
    }

    /**
     * Sets the validator to throw as early as the first validation failed.
     */
    public static Validator force() {
        return new Validator(true);
    }

    /**
     * Sets the validator to throw exceptions only at calls to {@code
     * Validator#check()}.
     */
    public static Validator lazy() {
        return new Validator(false);
    }

    private Validate() {
        throw new AssertionError();
    }
}
