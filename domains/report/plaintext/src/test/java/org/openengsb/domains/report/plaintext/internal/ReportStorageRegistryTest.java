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
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.domains.report.IdType;
import org.openengsb.domains.report.NoSuchReportException;

public class ReportStorageRegistryTest {

    private ReportStorageRegistry registry;

    private StorageKey alreadyAddedKey;

    @Before
    public void setUp() {
        registry = new ReportStorageRegistry();
        alreadyAddedKey = new StorageKey(UUID.randomUUID().toString(), IdType.CONTEXT_ID, UUID.randomUUID().toString());
        registry.storeDataFor(alreadyAddedKey);
    }

    @Test
    public void storeDataFor_shouldAddKeyToRegistry() throws NoSuchReportException {
        StorageKey key =
            new StorageKey(UUID.randomUUID().toString(), IdType.CORRELATION_ID, UUID.randomUUID().toString());
        registry.storeDataFor(key);
        StorageKey result = registry.getKeyFor(key.getReportId());
        assertThat(result, is(key));
    }

    @Test
    public void storeDataForAlradyIncludedKey_shouldDoNothing() {
        registry.storeDataFor(alreadyAddedKey);
    }

    @Test(expected = NoSuchReportException.class)
    public void stopStoringDataForKey_shouldRemoveKeyFromRegistry() throws NoSuchReportException {
        registry.stopStoringDataFor(alreadyAddedKey);
        registry.getKeyFor(alreadyAddedKey.getId());
    }

    @Test
    public void stopStoringDataForUnavailableKey_shouldDoNothing() throws NoSuchReportException {
        StorageKey key =
            new StorageKey(UUID.randomUUID().toString(), IdType.CORRELATION_ID, UUID.randomUUID().toString());
        registry.stopStoringDataFor(key);
        StorageKey result = registry.getKeyFor(alreadyAddedKey.getReportId());
        assertThat(result, is(alreadyAddedKey));
    }

    @Test
    public void getStorageKeys_shouldReturnRespectiveKeys() {
        StorageKey key = new StorageKey(UUID.randomUUID().toString(), IdType.CONTEXT_ID, alreadyAddedKey.getId());
        registry.storeDataFor(key);
        Set<StorageKey> keys = registry.getStorageKeysFor(IdType.CONTEXT_ID, alreadyAddedKey.getId());
        assertThat(keys.size(), is(2));
        assertThat(keys.contains(key), is(true));
        assertThat(keys.contains(alreadyAddedKey), is(true));
    }

    @Test
    public void getStorageKeysNoHit_shouldReturnEmptySet() {
        Set<StorageKey> keys = registry.getStorageKeysFor(IdType.CORRELATION_ID, UUID.randomUUID().toString());
        assertThat(keys.isEmpty(), is(true));
    }

    @Test(expected = NoSuchReportException.class)
    public void getKeyForNoHit_shouldThrowException() throws NoSuchReportException {
        registry.getKeyFor("foo");
    }

    @Test
    public void getKeyFor_shouldReturnKey() throws NoSuchReportException {
        StorageKey key = registry.getKeyFor(alreadyAddedKey.getReportId());
        assertThat(key, is(alreadyAddedKey));
    }

}
