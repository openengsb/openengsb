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

package org.openengsb.twitter;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.LinkingEndpoint;
import org.openengsb.drools.NotificationDomain;

/**
 * @org.apache.xbean.XBean element="twitterEndpoint"
 *                         description="Twitter Notification Endpoint"
 */
public class TwitterEndpoint extends LinkingEndpoint<NotificationDomain> {

    private TwitterNotifier twitterNotifier;

    @Override
    protected NotificationDomain getImplementation(ContextHelper contextHelper, MessageProperties msgProperties) {
        return twitterNotifier;
    }

	public void setTwitterNotifier(TwitterNotifier twitterNotifier) {
		this.twitterNotifier = twitterNotifier;
	}
}
