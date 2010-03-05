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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.drools.model.Attachment;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Twitter4JTwitterConnector implements TwitterConnector {
    
    private Log log = LogFactory.getLog(getClass());
    
    private String username;
    private String password;

    @Override
    public void updateStatus(String message){
        Twitter twitter = new TwitterFactory().getInstance(username, password);
        try {
            twitter.updateStatus(message);
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    @Override
    public void sendMessage(String receiver, String message) {
        Twitter twitter = new TwitterFactory().getInstance(username, password);
        try {
            twitter.sendDirectMessage(receiver, message);
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    public void zipAttachments(Attachment[] attachments, String filePath) throws IOException {
        FileOutputStream dest = new FileOutputStream(filePath);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        //Highest compression level
        out.setLevel(9);
        for (Attachment attachment : attachments) {
            ZipEntry entry = new ZipEntry(attachment.getName());
            out.putNextEntry(entry);
            out.write(attachment.getData());
            out.closeEntry();
        }
        out.close();
        dest.close();
    }

    public String getTinyUrl(String fullUrl) throws HttpException, IOException {
        HttpClient httpclient = new HttpClient();
        HttpMethod method = new GetMethod("http://tinyurl.com/api-create.php");
        method.setQueryString(new NameValuePair[] { new NameValuePair("url", fullUrl) });
        httpclient.executeMethod(method);
        String tinyUrl = method.getResponseBodyAsString();
        method.releaseConnection();
        return tinyUrl;
    }
    
    private void handleTwitterException(TwitterException e)
    {
        if(e.getStatusCode() == 401)
        {
            log.error("Incorrect or missing username or password. Authentication failed.");
        }
        else if(e.getStatusCode() == 404)
        {
            log.error("Unknown receiver for the message. Transmission failed.");
        }
        else
        {
            log.error("Action failed. Cause: " + e.getMessage());
        }
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
