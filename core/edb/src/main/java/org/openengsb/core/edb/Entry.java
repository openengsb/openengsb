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

package org.openengsb.core.edb;

/**
 * The Entry object is a pair of values, essentially the "from" and "to" values for a change. A value that does not
 * exist is null.
 */
public class Entry {
    private Object from;
    private Object to;

    public Entry(Object from, Object to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Get the initial value of this field.
     * 
     * @return The initial value, or null if it did not exist before.
     */
    public final Object getFrom() {
        return from;
    }

    /**
     * Get the final value of this field.
     * 
     * @return The final value, or null if was removed.
     */
    public final Object getTo() {
        return to;
    }
}