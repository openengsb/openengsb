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

package org.openengsb.core.common.context;

import java.util.Map;
import java.util.Set;

/**
 * A Context is a tree-like structure that contains key-value pairs (leafs) and child contexts (inner nodes of the
 * tree).
 */
public interface Context {

    /**
     * Returns an unmodifiable {@code Set} of keys where values exist at the current {@code Context} level.
     */
    Set<String> getKeys();

    /**
     * Returns the value for the given {@code key} or null if the {@code key} has not been set.
     */
    String get(String key);

    /**
     * Puts the {@code key-value} pair at the current {@code Context} level. The {@code key} must not contain any
     * slashes ({@code '/'}).
     *
     * @throws NullPointerException if {@code key} or {@code value} is null
     * @throws IllegalArgumentException if a child {@code Context} with the given {@code key} exists.
     */
    void put(String key, String value);

    /**
     * Returns an unmodifiable {@code Map} of child {@code Context}s.
     */
    Map<String, Context> getChildren();

    /**
     * Returns the child {@code Context} for the given {@code name} or null if the child does not exist.
     */
    Context getChild(String name);

    /**
     * Create a new child {@code Context}. The name must not contain any slashes ({@code '/'}).
     *
     * @throws IllegalArgumentException if a child or key-value pair with the given @{code name} exists.
     */
    Context createChild(String name);

    /**
     * Removes the child {@code Context} or key-value pair the given parameter identifies.
     */
    void remove(String nameOrKey);
}
