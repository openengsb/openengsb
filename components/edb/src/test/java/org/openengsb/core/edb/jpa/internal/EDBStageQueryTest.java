/*
 * Copyright 2013 vauve_000.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.edb.jpa.internal;

import org.junit.Test;

/**
 *
 * @author vauve_000
 */
public class EDBStageQueryTest extends AbstractEDBQueryTest
{
	@SuppressWarnings("serial")
    @Test
    public void testQueryWithSomeAspects_shouldWork() throws Exception {
		super.testQueryWithSomeAspects_shouldWork(getStage());
	}
	
	@Test
    public void testQueryOfOldVersion_shouldWork() throws Exception {
		super.testQueryOfOldVersion_shouldWork(getStage());
	}
	
	@Test
    public void testQueryWithTimestamp_shouldWork() throws Exception {
		super.testQueryWithTimestamp_shouldWork(getStage());
	}
	
	@Test
    public void testQueryWithTimestampAndEmptyMap_shouldWork() throws Exception {
		super.testQueryWithTimestampAndEmptyMap_shouldWork(getStage());
	}
	
	@Test
    public void testQueryOfLastKnownVersion_shouldWork() throws Exception {
		super.testQueryOfLastKnownVersion_shouldWork(getStage());
	}
	
	@Test
    public void testIfQueryingWithLikeWorks_shouldWork() throws Exception {
		super.testIfQueryingWithLikeWorks_shouldWork(getStage());
	}
}
