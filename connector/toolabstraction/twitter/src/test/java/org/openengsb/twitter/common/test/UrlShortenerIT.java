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

package org.openengsb.twitter.common.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.httpclient.HttpException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.twitter.common.util.UrlShortenerUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class UrlShortenerIT {
    @Resource
    private UrlShortenerUtil urlShortener;

    @Test
    public void testTinyUrl() throws HttpException, IOException {
        String s = "http://maps.google.at/maps/place?cid=2469784843158832493&q=tu+wien&hl=de&cd=1&cad=src:pplink&ei=yKOPS-jIA4mH_Qb5pPA7";
        String tiny = urlShortener.getTinyUrl(s);
        assertNotNull(tiny);
        assertTrue(s.length() > tiny.length());
    }
}
