package org.openengsb.edb.jbi.command;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openengsb.edb.jbi.endpoints.commands.EDBQuery;
import org.openengsb.util.IO;
import org.openengsb.util.Prelude;

public class EDBQueryTest {

	private static final String VALID_BEST_ORDER = "path:a/b/c AND a:foo AND b:bar";
	private static final String VALID_UNORDERED = "x:42 AND path:x/y/z AND y:1337";
	private static final String VALID_PATH_ONLY = "path:x/y/z";
	private static final String VALID_ONE_PATH_ELEMENT = "path:x/y/z AND x:42";
	private static final String INVALID_IS_FULL_PATH = "path:a/b/c AND a:foo AND b:bar AND c:test";
	private static final String INVALID_HAS_NO_PATH = "a:foo AND x:42";
	private static final String INVALID_HAS_PATH_ELEMENT = "path:x/y/z AND w:hello";
	private static final String INVALID_HAS_PATH_AND_NON_PATH_ELEMENTS = "path:a/b/c AND a:foo AND b:bar AND w:42";
	private static final String INVALID_IS_EMPTY = "";
	private static final String INVALID_HAS_OR = "path:a/b/c AND a:foo OR b:bar";
	private static final String INVALID_NO_ROOT_ELEM = "path:a/b/c OR b:bar AND c:test";
	

	@Test
	public void testIsNodeQuery() {
		assertTrue(EDBQuery.isNodeQuery(VALID_BEST_ORDER));
		assertTrue(EDBQuery.isNodeQuery(VALID_UNORDERED));
		assertTrue(EDBQuery.isNodeQuery(VALID_PATH_ONLY));
		assertTrue(EDBQuery.isNodeQuery(VALID_ONE_PATH_ELEMENT));
		assertFalse(EDBQuery.isNodeQuery(INVALID_IS_FULL_PATH));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_NO_PATH));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_PATH_ELEMENT));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_PATH_AND_NON_PATH_ELEMENTS));
		assertFalse(EDBQuery.isNodeQuery(INVALID_IS_EMPTY));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_OR));
		assertFalse(EDBQuery.isNodeQuery(INVALID_NO_ROOT_ELEM));
	}

}
