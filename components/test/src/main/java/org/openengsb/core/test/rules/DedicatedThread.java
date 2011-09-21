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

package org.openengsb.core.test.rules;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Use this rule to run every test in a separate Thread. This is useful when testing code using
 * {@link java.lang.ThreadLocal}
 *
 * Usage: create a public field in your testclass and annotate it with <code>@Rule</code>
 * <code>public DedicatedThread threadRule = new DedicatedThread();</code>
 */
public final class DedicatedThread implements MethodRule {

    private class ThreadStatement extends Statement {
        private Throwable thrown;
        private Statement parent;

        public ThreadStatement(Statement parent) {
            this.parent = parent;
        }

        @Override
        public void evaluate() throws Throwable {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        parent.evaluate();
                    } catch (Throwable e) {
                        thrown = e;
                    }
                }
            };
            thread.start();
            thread.join();
            if (thrown != null) {
                throw thrown;
            }
        }
    }

    @Override
    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new ThreadStatement(statement);
    }
}

