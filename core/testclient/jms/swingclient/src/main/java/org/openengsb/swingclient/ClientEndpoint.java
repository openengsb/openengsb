/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.swingclient;

public class ClientEndpoint {

    private String destinationName;

    private String targetService;

    public ClientEndpoint(String destinationName, String targetService) {
        this.destinationName = destinationName;
        this.targetService = targetService;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    // displays target service without namespace prefix
    @Override
    public String toString() {
        int col = targetService.indexOf(':');
        if (col == -1) {
            return targetService;
        }
        return targetService.substring(col + 1);
    }

}
