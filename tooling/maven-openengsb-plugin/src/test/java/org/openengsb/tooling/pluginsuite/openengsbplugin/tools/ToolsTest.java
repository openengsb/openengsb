package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class ToolsTest {

	@Test
	public void testExpectedInput() {
		assertEquals("Hello world", Tools.capitalizeFirst("hello world"));
		assertEquals("H", Tools.capitalizeFirst("h"));
	}

	@Test
	public void testUnexpectedInput() {
		assertNull(Tools.capitalizeFirst(null));
		assertEquals("", Tools.capitalizeFirst(""));
		assertEquals(" ", Tools.capitalizeFirst(" "));
		assertEquals("  ", Tools.capitalizeFirst("  "));
		assertEquals("?", Tools.capitalizeFirst("?"));
		assertEquals("/&%(#", Tools.capitalizeFirst("/&%(#"));
	}

}
