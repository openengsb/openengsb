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

package org.openengsb.facebook.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.drools.model.Notification;
import org.openengsb.facebook.FacebookNotifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-bean.xml" })
public class FacebookNotifierIT {


    @Resource
    private FacebookNotifier facebookNotifier;


    @Before
    public void setup() {
    }

    @After
    public void after() {
    }

    @Test
    public void sendNotificationtoUpdateUserStatus() {
        Notification notification = new Notification();
        notification.setMessage("test on: " + new Date());
        facebookNotifier.notify(notification);


    }


}