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

package org.openengsb.issues.common.model;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class IssueTest {

    @Test
    public void equalsShouldReturnTrueWhenComparingObjectsWithSameAttributeValues() {
        Issue i1 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        Issue i2 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");

        assertTrue(i1.equals(i2));
    }

    @Test
    public void equalsShouldReturnFalseWhenComparingObjectsWithAtLeastOneDifferingAttributeValue() {
        Issue i1 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i1.setId("i1");
        Issue i2 = new Issue("sx", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i2.setId("i1");
        Issue i3 = new Issue("s1", "dx", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i3.setId("i1");
        Issue i4 = new Issue("s1", "d1", "rx", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i4.setId("i1");
        Issue i5 = new Issue("s1", "d1", "r1", "ox", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i5.setId("i1");
        Issue i6 = new Issue("s1", "d1", "r1", "o1", IssueType.IMPROVEMENT, IssuePriority.HIGH, IssueSeverity.BLOCK,
                "1.0");
        i6.setId("i1");
        Issue i7 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.LOW, IssueSeverity.BLOCK, "1.0");
        i7.setId("i1");
        Issue i8 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.TRIVIAL, "1.0");
        i8.setId("i1");
        Issue i9 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.x");
        i9.setId("i1");
        Issue i10 = new Issue("s1", "d1", "r1", "o1", IssueType.BUG, IssuePriority.HIGH, IssueSeverity.BLOCK, "1.0");
        i10.setId("ix");
        Issue i11 = new Issue(null, null, null, null, null, null, null, null);
        i11.setId(null);

        assertFalse("Problem with summary unequality.", i1.equals(i2));
        assertFalse("Problem with description unequality.", i1.equals(i3));
        assertFalse("Problem with reporter unequality.", i1.equals(i4));
        assertFalse("Problem with owner unequality.", i1.equals(i5));
        assertFalse("Problem with type unequality.", i1.equals(i6));
        assertFalse("Problem with priority unequality.", i1.equals(i7));
        assertFalse("Problem with severity unequality.", i1.equals(i8));
        assertFalse("Problem with affected version unequality.", i1.equals(i9));
        assertFalse("Problem with id unequality.", i1.equals(i10));
        assertFalse("Problem with null unequality.", i1.equals(i11));
    }
}
