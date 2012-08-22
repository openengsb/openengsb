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

package org.openengsb.ports.jms;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

public class DestinationUrlTest {

    @Test
    public void testCreateDestinationUrlWithValidParams_shouldCreateValidDestination() throws Exception {
        DestinationUrl destinationUrl = DestinationUrl.createDestinationUrl("host?queue");
        assertThat(destinationUrl.getHost(), Matchers.equalTo("host"));
        assertThat(destinationUrl.getJmsDestination(), Matchers.equalTo("queue"));
    }

    @Test
    public void testCreateDestinationUrlWithValidParamsAndPort_shouldCreateValidDestination() throws Exception {
        DestinationUrl destinationUrl = DestinationUrl.createDestinationUrl("tcp://host:8080?queue");
        assertThat(destinationUrl.getHost(), Matchers.equalTo("tcp://host:8080"));
        assertThat(destinationUrl.getJmsDestination(), Matchers.equalTo("queue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern_shouldThrowError() throws Exception {
        DestinationUrl.createDestinationUrl("blub");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatingDestinationWithMoreThanOneQuestionMark_shouldThrowError() throws Exception {
        DestinationUrl.createDestinationUrl("bli?bla?blub");
    }
}
