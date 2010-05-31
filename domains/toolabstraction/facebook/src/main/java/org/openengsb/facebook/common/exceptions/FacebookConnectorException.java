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
package org.openengsb.facebook.common.exceptions;


public class FacebookConnectorException extends Exception{
    private int statusCode;

    public FacebookConnectorException() {
        super();
    }
    public FacebookConnectorException(int code) {
        super();
        statusCode = code;
    }
    public FacebookConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public FacebookConnectorException(String message) {
        super(message);
    }
    public FacebookConnectorException(String message, int code) {
        super(message);
        statusCode = code;
    }
    public FacebookConnectorException(Throwable cause) {
        super(cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

}
