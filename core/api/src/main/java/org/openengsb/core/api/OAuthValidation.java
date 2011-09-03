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

package org.openengsb.core.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * An oauth validation object capsulating all the required data to be transformed between the client (openengsb) and the
 * server.
 */
public class OAuthValidation {
    private Map<String, String> params;

    public OAuthValidation(Hashtable<String, String> parameters) {
        params = parameters;
    }

    public OAuthValidation() {
        params = new Hashtable<String, String>();
    }

    public void addParameter(String key, String val) {
        params.put(key, val);
    }

    public String performOAuthValidation(URL url, String params) throws Exception {
        return sendData(url, params);
    }

    public String performOAuthValidation(URL url) throws Exception {
        return performOAuthValidation(url, null);
    }

    private String sendData(URL myURL, String params) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection) myURL.openConnection();
        if (params != null) {
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            OutputStreamWriter ow = new OutputStreamWriter(con.getOutputStream());
            ow.write(params);
            ow.flush();
            ow.close();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer output = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            output.append(inputLine);
        }
        in.close();
        return output.toString();
    }
}
