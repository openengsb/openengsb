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

package org.openengsb.twitter.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlShortenerUtil {
    private Log log = LogFactory.getLog(getClass());

    public String getTinyUrl(URL fullUrl) throws HttpException, IOException {
        return getTinyUrl(fullUrl.toString());
    }

    public String getTinyUrl(String fullUrl) throws HttpException, IOException {
        HttpClient httpclient = new HttpClient();
        HttpMethod method = new GetMethod("http://tinyurl.com/api-create.php");
        method.setQueryString(new NameValuePair[] { new NameValuePair("url", fullUrl) });
        httpclient.executeMethod(method);

        InputStream is = method.getResponseBodyAsStream();
        StringBuilder sb = new StringBuilder();
        for (int i = is.read(); i != -1; i = is.read()) {
            sb.append((char) i);
        }
        String tinyUrl = sb.toString();
        method.releaseConnection();

        log.info("Successfully shortened URL " + fullUrl + " via tinyurl to " + tinyUrl + ".");
        return tinyUrl;
    }
}
