package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringToolsTest {

	@Test
	public void testExpectedInput() {
		assertEquals("Hello world", StringTools.capitalizeFirst("hello world"));
		assertEquals("H", StringTools.capitalizeFirst("h"));
	}

	@Test
	public void testUnexpectedInput() {
		assertNull(StringTools.capitalizeFirst(null));
		assertEquals("", StringTools.capitalizeFirst(""));
		assertEquals(" ", StringTools.capitalizeFirst(" "));
		assertEquals("  ", StringTools.capitalizeFirst("  "));
		assertEquals("?", StringTools.capitalizeFirst("?"));
		assertEquals("/&%(#", StringTools.capitalizeFirst("/&%(#"));
	}

}
