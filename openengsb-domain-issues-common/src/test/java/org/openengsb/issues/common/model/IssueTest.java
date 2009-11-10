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

package org.openengsb.issues.common.model;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import org.junit.Test;

public class IssueTest {

    @Test
    public void equalsShouldReturnTrueWhenComparingObjectsWithSameAttributeValues() {
        Issue i1 = new Issue("s1", "d1", "r1", "o1", "t1", "p1");
        Issue i2 = new Issue("s1", "d1", "r1", "o1", "t1", "p1");

        assertTrue(i1.equals(i2));
    }

    @Test
    public void equalsShouldReturnFalseWhenComparingObjectsWithAtLeastOneDifferingAttributeValue() {
        Issue i1 = new Issue("s1", "d1", "r1", "o1", "t1", "p1");
        Issue i2 = new Issue("sx", "d1", "r1", "o1", "t1", "p1");
        Issue i3 = new Issue("s1", "dx", "r1", "o1", "t1", "p1");
        Issue i4 = new Issue("s1", "d1", "rx", "o1", "t1", "p1");
        Issue i5 = new Issue("s1", "d1", "r1", "ox", "t1", "p1");
        Issue i6 = new Issue("s1", "d1", "r1", "o1", "tx", "p1");
        Issue i7 = new Issue("s1", "d1", "r1", "o1", "t1", "px");

        assertFalse(i1.equals(i2));
        assertFalse(i1.equals(i3));
        assertFalse(i1.equals(i4));
        assertFalse(i1.equals(i5));
        assertFalse(i1.equals(i6));
        assertFalse(i1.equals(i7));
    }
}
