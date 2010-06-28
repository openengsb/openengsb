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
package org.openengsb.drools.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.message.RuleBaseElementId;
import org.openengsb.drools.message.RuleBaseElementType;

@RunWith(Parameterized.class)
public abstract class RuleSourceCrudTest<SourceType extends RuleBaseSource> extends RuleSourceTest<SourceType> {

    @Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new HashSet<Object[]>();
        // functions:
        data.add(new Object[] { new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "test42"),
                "function void test42(){ System.out.println(\"sample-code\");}",
                new RuleBaseElementId(RuleBaseElementType.Function, "at.ac.tuwien", "test"),
                "function void test(){ System.out.println(\"bla42\");}", });

        // rules:
        data.add(new Object[] { new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test42"),
                "when\nthen\nSystem.out.println(\"sample-code\");",
                new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "test"),
                "when\nthen\nSystem.out.println(\"\");", });

        // imports:
        // data.add(new Object[] { new
        // RuleBaseElementId(RuleBaseElementType.Import, "ignored",
        // "java.util.Currency"),
        // "java.util.Currency", new
        // RuleBaseElementId(RuleBaseElementType.Import, "ignored",
        // "java.util.Random"),
        // "java.util.Random", });
        // TODO
        // globals:

        return data;
    }

    protected String code1;
    protected String code2;
    protected RuleBaseElementId id1;
    protected RuleBaseElementId id2;

    public RuleSourceCrudTest(RuleBaseElementId id1, String code1, RuleBaseElementId id2, String code2) {
        this.code1 = code1;
        this.code2 = code2;
        this.id1 = id1;
        this.id2 = id2;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreate() throws RuleBaseException {
        source.add(id1, code1);
        assertEquals(code1, source.get(id1));
    }

    @Test
    public void testUpdate() throws RuleBaseException {
        source.add(id1, code1);
        source.update(id1, code2);
        assertEquals(code2, source.get(id1));
    }

    @Test
    public void testList() throws RuleBaseException {
        source.add(id1, code1);
        source.add(id2, code2);
        Collection<RuleBaseElementId> result = source.list(id1.getType(), id1.getPackageName());
        assertTrue(result.contains(id1));
        assertFalse(result.contains(id2));
    }

    @Test
    public void testListAll() throws RuleBaseException {
        source.add(id1, code1);
        source.add(id2, code2);
        Collection<RuleBaseElementId> result = source.list(id1.getType());
        assertTrue(result.contains(id1));
        assertTrue(result.contains(id2));
    }

    @Test
    public void testDelete() throws RuleBaseException {
        source.add(id1, code1);
        source.delete(id1);
    }

}
