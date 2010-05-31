/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/
package org.openengsb.facebook.common;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJaxbRestClient;
import org.openengsb.facebook.common.exceptions.FacebookConnectorException;


public interface FacebookLogin {
    boolean loginUser(String email, String password, String api) throws FacebookConnectorException;

    void closeHttpClient();

    FacebookJaxbRestClient createLoggedInFacebookClient(String api_key, String secret) throws FacebookException, FacebookConnectorException;
}
