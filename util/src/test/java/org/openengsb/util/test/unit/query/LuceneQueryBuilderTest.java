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
package org.openengsb.util.test.unit.query;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.util.query.LuceneQueryBuilder;
import org.openengsb.util.query.LuceneQueryBuilderException;


/**
 * Testsuite to check if the queries produced by the {@link LuceneQueryBuilder}
 * does what is expected by them.
 */
public class LuceneQueryBuilderTest {

    @Test
    public void testSimpleQuery() throws Exception {
        Assert.assertEquals("(secondkey:secondvalue AND firstkey:firstvalue)", new LuceneQueryBuilder().and("firstkey",
                "firstvalue").and("secondkey", "secondvalue").buildQuery());
    }

    @Test
    public void testSpecialChars() throws Exception {
        final String expected = "(test:=90?ASDF?A?OO????III?asdf?ee?f?a?a?a?a?a?a?a?a?a?a?a?addeTZUIO#a#?a)";
        final String testValue = "=90+ASDF-A&OO&& ||III!asdf(ee)f{a}a}a}a[a]a^a\"a~a*a?a:addeTZUIO#a#\\a";
        Assert.assertEquals(expected, new LuceneQueryBuilder().and("test", testValue).buildQuery());
    }

    @Test
    public void testComplexQuery() throws Exception {
        final String expected = "((y:y AND x:x) OR (z:z) OR (b:b OR a:a))";
        Assert.assertEquals(expected, new LuceneQueryBuilder().or(new LuceneQueryBuilder().and("y", "y").and("x", "x"))
                .or(new LuceneQueryBuilder().or("z", "z")).or(new LuceneQueryBuilder().or("b", "b").or("a", "a"))
                .buildQuery());
    }

    @Test(expected = LuceneQueryBuilderException.class)
    public void testEverythingEmpty() throws Exception {
        new LuceneQueryBuilder().buildQuery();
    }

    @Test(expected = LuceneQueryBuilderException.class)
    public void testChangeFromAndToOtherOperator() throws Exception {
        new LuceneQueryBuilder().and("x", "x").or("y", "y");
    }

    @Test(expected = LuceneQueryBuilderException.class)
    public void testChangeFromOrToOtherOperator() throws Exception {
        new LuceneQueryBuilder().or("x", "x").and("y", "y");
    }

    @Test
    public void testNullDoesNotHarm() throws Exception {
        Assert.assertEquals("(null:*)", new LuceneQueryBuilder().and(null, null).buildQuery());
    }

    @Test
    public void testNullWithOtherDoesNotHarm() throws Exception {
        Assert.assertEquals("(null:* AND x:x)", new LuceneQueryBuilder().and(null, null).and("x", "x").buildQuery());
    }

    @Test
    public void testEmptyValue() throws Exception {
        final LuceneQueryBuilder queryBuilder = new LuceneQueryBuilder();
        queryBuilder.and("kks", "").and("x", "x");
        Assert.assertEquals("(kks:* AND x:x)", queryBuilder.buildQuery());
    }

    @Test
    public void testNullValue() throws Exception {
        final LuceneQueryBuilder queryBuilder = new LuceneQueryBuilder();
        queryBuilder.and("kks", null).and("x", "x");
        Assert.assertEquals("(kks:* AND x:x)", queryBuilder.buildQuery());
    }
}
