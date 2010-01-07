/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.util.test.unit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.util.Prelude;


public class PreludeTest {
    @Test
    public void linesWithDifferentSeparators_shouldSplitAll() {
        final String word = "foo";
        final String input = word + "\n" + word + "\r" + word + "\r\n" + word + "\n\r" + word;
        final String[] actual = Prelude.lines(input);
        assertThat(actual.length, is(5));
        for (final String line : actual) {
            assertThat(line, is(word));
        }
    }

    @Test
    public void linesWithEmptyLine_shouldReturnOneLine() {
        final String[] actual = Prelude.lines("");
        assertThat(actual.length, is(1));
        assertThat(actual[0], is(""));
    }

    @Test
    public void testDePathize() throws Exception {
        final String test = "/first/second/";
        final String[] dePathized = Prelude.dePathize(test);
        assertEquals("first", dePathized[0]);
        assertEquals("second", dePathized[1]);
    }
}
