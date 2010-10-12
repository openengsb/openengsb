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

package org.openengsb.core.common.l10n;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class BundleStringsTest {

    public static void mockHeaders(Bundle bundle) {
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put(Constants.BUNDLE_LOCALIZATION, "bundle_locales/OSGI-INF/l10n/bundle");
        when(bundle.getHeaders()).thenReturn(dict);
    }

    public static void mockFindEntries(Bundle bundle) {
        when(bundle.findEntries(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean())).thenAnswer(
            new Answer<Enumeration<URL>>() {
                @Override
                public Enumeration<URL> answer(InvocationOnMock invocation) {
                    List<URL> messages = new ArrayList<URL>();
                    messages.add(ClassLoader.getSystemResource("bundle_locales/OSGI-INF/l10n/bundle.testsprops"));
                    messages.add(ClassLoader.getSystemResource("bundle_locales/OSGI-INF/l10n/bundle_a.testsprops"));
                    messages.add(ClassLoader.getSystemResource("bundle_locales/OSGI-INF/l10n/bundle_a_B.testsprops"));
                    messages.add(ClassLoader.getSystemResource("bundle_locales/OSGI-INF/l10n/bundle_a_B_c.testsprops"));
                    return Collections.enumeration(messages);
                }
            });
    }

    public static Bundle createBundle() {
        Bundle bundle = mock(Bundle.class);
        mockHeaders(bundle);
        mockFindEntries(bundle);
        return bundle;
    }

    public static BundleContext createBundleContextMockWithBundleStrings() {
        Bundle bundle = createBundle();
        BundleContext bundleContextMock = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContextMock.getBundle()).thenReturn(bundle);
        return bundleContextMock;
    }

    @Test
    public void noSpecificEntryForLocale_shouldLookupStringFromDefaultEntry() throws Exception {
        BundleStrings bl = new BundleStrings(createBundle());
        assertThat(bl.getString("unique_key", new Locale("a", "b", "c")), is("default"));
    }

    @Test
    public void searchingAllEntriesInBundle_shouldSearchWithRightDirectoryAndFilePattern() throws Exception {
        Bundle bundle = mock(Bundle.class);
        mockHeaders(bundle);
        BundleStrings bl = new BundleStrings(bundle);
        bl.getString("a");
        verify(bundle).findEntries("bundle_locales/OSGI-INF/l10n", "bundle*.properties", false);
    }

    @Test
    public void entryExistsForLocale_shouldReturnKeyFromEntry() throws Exception {
        BundleStrings bl = new BundleStrings(createBundle());
        assertThat(bl.getString("abc", new Locale("a", "b", "c")), is("c"));
    }

    @Test
    public void noEntryForLocale_shouldReturnKeyFromNextSpecificEntry() throws Exception {
        BundleStrings bl = new BundleStrings(createBundle());
        assertThat(bl.getString("ab", new Locale("a", "b", "c")), is("b"));
        assertThat(bl.getString("a", new Locale("a", "b", "c")), is("a"));
        assertThat(bl.getString("a", new Locale("a", "b")), is("a"));
    }

    @Test
    public void passThroughLocalizer_shouldReturnKey() {
        assertThat(new PassThroughStringLocalizer().getString("a", Locale.GERMAN), is("a"));
    }
}
