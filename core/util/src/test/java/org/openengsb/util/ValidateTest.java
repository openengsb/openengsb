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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openengsb.util.Validate;
import org.openengsb.util.Validator;


public class ValidateTest {
    @Test
    public void lazyValidateWithoutCheck_shouldNotThrowEx() {
        Validate.lazy().that(null, notNullValue());
        Validate.lazy().that("", null, notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void lazyValidateWithFailingCheck_shouldThrowEx() {
        Validate.lazy().that(null, notNullValue()).check();
    }

    @Test(expected = IllegalArgumentException.class)
    public void forceValidateWithFailingAssert_shouldThrowEx() {
        Validate.force().that(null, notNullValue());
    }

    @Test
    public void lazyValidateWithTwoFailingAsserts_shouldThrowExAtCheck() {
        Validator v = Validate.lazy().that(null, notNullValue());
        v.that(null, notNullValue());
        try {
            v.check();
            fail("check should throw exception");
        } catch (IllegalArgumentException e) {
            // no op
        }
    }

    @Test
    public void failingValidate_shouldInsertAnErrorMessage() {
        String paramName = "foo";
        try {
            Validate.force().that(paramName, null, notNullValue());
            fail("no exception thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), notNullValue());
            assertThat(e.getMessage().length(), greaterThan(0));
            assertThat(e.getMessage(), containsString(paramName));
        }
    }
}
