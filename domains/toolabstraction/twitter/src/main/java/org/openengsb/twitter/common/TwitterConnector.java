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
package org.openengsb.twitter.common;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.openengsb.drools.model.Attachment;

public interface TwitterConnector {
    
<<<<<<< HEAD:domains/toolabstraction/twitter/src/main/java/org/openengsb/test/common/TwitterConnector.java
<<<<<<< HEAD
    public static void updateStatus(String username, String password, String message) throws TwitterException;
=======
    public void updateStatus(String username, String password, String message) throws OpenEngSBTwitterException;
>>>>>>> TwitterException renamed
=======
    public void updateStatus(String message);
    
    public void sendMessage(String receiver, String message);
    
    public void zipAttachments(Attachment[] attachments, String filePath) throws IOException;
>>>>>>> Refactoring and correction due to comments:domains/toolabstraction/twitter/src/main/java/org/openengsb/twitter/common/TwitterConnector.java
    
    public String getTinyUrl(String fullUrl) throws HttpException, IOException;
}
