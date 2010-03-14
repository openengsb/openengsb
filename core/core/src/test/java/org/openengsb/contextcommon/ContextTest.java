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

package org.openengsb.contextcommon;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.contextcommon.Context;
import org.openengsb.contextcommon.ContextNameClashException;
import org.openengsb.contextcommon.ContextNotFoundException;
import org.openengsb.contextcommon.ContextStore;

public class ContextTest {

    private ContextStore store;

    @BeforeClass
    public static void beforeClass() {
        new File("testsettings.xml").deleteOnExit();
    }

    @Before
    public void before() {
        store = new ContextStore(new File("testsettings.xml"));
    }

    @After
    public void after() {
        new File("testsettings.xml").delete();
    }

    @Test
    public void testReadWriteRoot() {
        store.setValue("foo", "42");
        Context context = store.getContext("/");

        String value = context.get("foo");

        assertEquals("42", value);
    }

    @Test
    public void testReadWriteNonRoot() {
        store.setValue("/foo/bar/buz", "42");
        Context context = store.getContext("/foo/bar");

        String value = context.get("buz");

        assertEquals("42", value);
    }

    @Test
    public void testPathPreprocess() {
        store.setValue("/foo/bar/buz", "42");
        Context ctx1 = store.getContext("/foo/bar");
        Context ctx2 = store.getContext("/foo//bar");
        Context ctx3 = store.getContext("foo/bar");
        Context ctx4 = store.getContext("foo//bar");
        Context ctx5 = store.getContext("foo//bar/");
        Context ctx6 = store.getContext("/foo/bar/");
        Context ctx7 = store.getContext("/foo//bar/");

        assertEquals("42", ctx1.get("buz"));
        assertEquals("42", ctx2.get("buz"));
        assertEquals("42", ctx3.get("buz"));
        assertEquals("42", ctx4.get("buz"));
        assertEquals("42", ctx5.get("buz"));
        assertEquals("42", ctx6.get("buz"));
        assertEquals("42", ctx7.get("buz"));
    }

    @Test(expected = ContextNotFoundException.class)
    public void testInvalidContext() {
        store.getContext("/not/found");
    }

    @Test
    public void testPath() {
        store.setValue("/foo/bar/buz", "42");

        Context ctx1 = store.getContext("/");
        Context ctx2 = store.getContext("/foo");
        Context ctx3 = store.getContext("/foo/bar");

        assertNull(ctx1.get("buz"));
        assertNull(ctx2.get("buz"));
        assertEquals("42", ctx3.get("buz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDepth0() {
        store.getContext("/", 0);
    }

    @Test
    public void testGetContextDepth() {
        store.setValue("/foo/bar/buz", "42");

        Context ctx1 = store.getContext("/", 1);
        Context ctx2 = store.getContext("/", 2);
        Context ctx3 = store.getContext("/", 3);

        assertNull(ctx1.getChild("foo"));

        assertNotNull(ctx2.getChild("foo"));
        assertNull(ctx2.getChild("foo").getChild("bar"));

        assertEquals("42", ctx3.getChild("foo").getChild("bar").get("buz"));
    }

    @Test
    public void testChildPathResolution() {
        store.setValue("/foo/bar/buz", "42");
        Context ctx = store.getContext("/");
        assertEquals("42", ctx.getChild("foo/bar").get("buz"));
    }

    @Test
    public void testParentLookup() {
        store.setValue("x", "1");
        store.setValue("y", "42");
        store.setValue("foo/bar/x", "10");

        Context ctx = store.getContext("foo/bar");
        assertEquals("42", ctx.get("y"));
        assertEquals("10", ctx.get("x"));
    }

    @Test
    public void testSaveLoad() {
        File datafile = new File("test.xml");
        ContextStore s2 = new ContextStore(datafile);
        s2.setValue("foo/bar/buz", "42");

        ContextStore s3 = new ContextStore(datafile);
        assertEquals("42", s3.getValue("foo/bar/buz"));

        datafile.delete();
    }

    @Test(expected = ContextNameClashException.class)
    public void testCreateEntryThatIsAContext() {
        store.setValue("foo/bar/buz", "42");
        store.setValue("foo/bar", "x");
    }

    @Test(expected = ContextNameClashException.class)
    public void testCreateContextThatIsAEntry() {
        store.setValue("foo/bar", "x");
        store.setValue("foo/bar/buz", "42");
    }

    @Test
    public void testSimpleRemove() {
        store.setValue("foo/bar/buz", "42");
        store.removeValue("foo/bar/buz");
        assertNull(store.getValue("foo/bar/buz"));
    }

    @Test(expected = ContextNotFoundException.class)
    public void testTreeRemove() {
        store.setValue("foo/bar/buz", "42");
        store.removeValue("foo");
        store.getContext("foo");
    }

    @Test
    public void testAddEmptyContext() {
        store.addContext("foo/bar");
        assertNotNull(store.getContext("foo/bar"));
    }

    @Test(expected = ContextNameClashException.class)
    public void testEmptyContextCollision() {
        store.addContext("foo/bar");
        store.setValue("foo/bar", "42");
    }

    @Test(expected = ContextNotFoundException.class)
    public void testRemoveEmptyContext() {
        store.addContext("foo/bar");
        store.removeValue("foo/bar");
        store.getContext("foo/bar");
    }

    @Test
    public void testKeepEmptySuperContext() {
        store.addContext("foo/bar");
        store.removeValue("foo/bar");
        store.getContext("foo");
    }

}
