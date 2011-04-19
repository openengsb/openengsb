/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.util;

import java.util.Comparator;

import org.openengsb.core.api.DomainProvider;

/**
 * provides comparators for sorting some OpenEngSB internals that are not comparable.
 */
public final class Comparators {

    /**
     * returns a comparator that compares domain providers by providing their id-values {@link DomainProvider#getId()}.
     */
    public static Comparator<? super DomainProvider> forDomainProvider() {
        return new DomainProviderComparator();
    }

    private static final class DomainProviderComparator implements Comparator<DomainProvider> {
        @Override
        public int compare(DomainProvider o1, DomainProvider o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }

    private Comparators() {
    }
}
