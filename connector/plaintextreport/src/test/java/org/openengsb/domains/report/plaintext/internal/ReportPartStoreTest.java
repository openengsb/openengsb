/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.report.plaintext.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.domains.report.model.ReportPart;
import org.openengsb.domains.report.model.SimpleReportPart;

public abstract class ReportPartStoreTest {

    private ReportPartStore store;

    protected abstract ReportPartStore createStore();

    protected abstract void deleteStore();

    private String alreadyAddedKey;

    @Before
    public void setUp() {
        this.store = createStore();
        alreadyAddedKey = UUID.randomUUID().toString();
        store.storePart(alreadyAddedKey, new SimpleReportPart("alreadyAdded1", "text/plain", null));
        store.storePart(alreadyAddedKey, new SimpleReportPart("alreadyAdded2", "text/plain", null));
    }

    @After
    public void tearDown() {
        deleteStore();
    }

    @Test
    public void storePartNewKey_shouldStorePart() {
        String newKey = "foo";
        SimpleReportPart reportPart = new SimpleReportPart("42", "text/plain", null);
        store.storePart(newKey, reportPart);
        assertThat(store.getParts(newKey).size(), is(1));
        assertThat(store.getParts(newKey).get(0).getPartName(), is("42"));
    }

    @Test
    public void storePartExistingKey_shouldStorePart() {
        SimpleReportPart reportPart = new SimpleReportPart("42", "text/plain", null);
        store.storePart(alreadyAddedKey, reportPart);
        List<ReportPart> parts = store.getParts(alreadyAddedKey);
        List<String> expected = Arrays.asList(new String[]{ "42", "alreadyAdded1", "alreadyAdded2" });
        assertThat(parts.size(), is(expected.size()));
        for (int i = 0; i < parts.size(); i++) {
            assertThat(expected.contains(parts.get(i).getPartName()), is(true));
        }
    }

    @Test
    public void clearParts_shouldDeleteAllRespectiveParts() {
        store.clearParts(alreadyAddedKey);
        assertThat(store.getParts(alreadyAddedKey).isEmpty(), is(true));
    }

    @Test
    public void getPartsExsitingKey_shouldReturnAllParts() {
        List<ReportPart> parts = store.getParts(alreadyAddedKey);
        assertThat(parts.size(), is(2));
    }

    @Test
    public void getPartsNewKey_shouldReturnEmptyList() {
        String newKey = "foo";
        List<ReportPart> parts = store.getParts(newKey);
        assertThat(parts.isEmpty(), is(true));
    }

    @Test
    public void getLastPartExsitingKey_shouldReturnLastPart() {
        ReportPart lastPart = store.getLastPart(alreadyAddedKey);
        assertThat(lastPart.getPartName(), is("alreadyAdded2"));
    }

    @Test
    public void getLastPartNewKey_shouldReturnNull() {
        String newKey = "foo";
        assertThat(store.getLastPart(newKey), nullValue());
    }
}
