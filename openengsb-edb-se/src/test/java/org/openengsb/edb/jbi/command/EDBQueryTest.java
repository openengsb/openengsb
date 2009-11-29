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
package org.openengsb.edb.jbi.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openengsb.edb.jbi.endpoints.commands.EDBQuery;

import edu.emory.mathcs.backport.java.util.Arrays;

public class EDBQueryTest {

	private static final String VALID_BEST_ORDER = "path:a/b/c AND a:foo AND b:bar";
	private static final String VALID_UNORDERED = "x:42 AND path:x/y/z AND y:1337";
	private static final String VALID_PATH_ONLY = "path:x/y/z";
	private static final String VALID_ONE_PATH_ELEMENT = "path:x/y/z AND x:42";
	
	@SuppressWarnings("unchecked")
	private static final List<String> QUERY_LIST_1 = new ArrayList<String>(Arrays.asList(new String[]{"foo","bar",}));
	@SuppressWarnings("unchecked")
	private static final List<String> QUERY_LIST_2 = new ArrayList<String>(Arrays.asList(new String[]{"42","1337",}));
	@SuppressWarnings("unchecked")
	private static final List<String> QUERY_LIST_3 = new ArrayList<String>(Arrays.asList(new String[]{}));
	@SuppressWarnings("unchecked")
	private static final List<String> QUERY_LIST_4 = new ArrayList<String>(Arrays.asList(new String[]{"42",}));
	
	private static final String INVALID_IS_FULL_PATH = "path:a/b/c AND a:foo AND b:bar AND c:test";
	private static final String INVALID_HAS_NO_PATH = "a:foo AND x:42";
	private static final String INVALID_HAS_PATH_ELEMENT = "path:x/y/z AND w:hello";
	private static final String INVALID_HAS_PATH_AND_NON_PATH_ELEMENTS = "path:a/b/c AND a:foo AND b:bar AND w:42";
	private static final String INVALID_IS_EMPTY = "";
	private static final String INVALID_HAS_OR = "path:a/b/c AND a:foo OR b:bar";
	private static final String INVALID_NO_ROOT_ELEM = "path:a/b/c OR b:bar AND c:test";
	

	@Test
	public void testIsNodeQuery() throws Exception{
		assertTrue(EDBQuery.isNodeQuery(VALID_BEST_ORDER));
		assertTrue(EDBQuery.isNodeQuery(VALID_UNORDERED));
		assertTrue(EDBQuery.isNodeQuery(VALID_PATH_ONLY));
		assertTrue(EDBQuery.isNodeQuery(VALID_ONE_PATH_ELEMENT));
		
	}
	
	@Test
	public void testPrepareQuery() throws Exception{
		assertEquals(QUERY_LIST_1, EDBQuery.prepareForNodeQuery(VALID_BEST_ORDER));
		assertEquals(QUERY_LIST_2, EDBQuery.prepareForNodeQuery(VALID_UNORDERED));
		assertEquals(QUERY_LIST_3, EDBQuery.prepareForNodeQuery(VALID_PATH_ONLY));
		assertEquals(QUERY_LIST_4, EDBQuery.prepareForNodeQuery(VALID_ONE_PATH_ELEMENT));
	}
	
	@Test
	public void testInValidSamples() throws Exception{
		assertFalse(EDBQuery.isNodeQuery(INVALID_IS_FULL_PATH));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_NO_PATH));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_PATH_ELEMENT));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_PATH_AND_NON_PATH_ELEMENTS));
		assertFalse(EDBQuery.isNodeQuery(INVALID_IS_EMPTY));
		assertFalse(EDBQuery.isNodeQuery(INVALID_HAS_OR));
		assertFalse(EDBQuery.isNodeQuery(INVALID_NO_ROOT_ELEM));
	}

}
