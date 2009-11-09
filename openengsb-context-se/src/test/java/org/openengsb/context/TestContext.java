package org.openengsb.context;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class TestContext {

	private ContextStore store;

	@Before
	public void before() {
		store = new ContextStore();
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
}
