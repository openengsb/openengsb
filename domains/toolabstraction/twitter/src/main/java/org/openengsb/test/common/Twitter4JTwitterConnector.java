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
package org.openengsb.test.common;

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
import org.openengsb.drools.model.Attachment;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class Twitter4JTwitterConnector implements TwitterConnector {

    @Override
    public void updateStatus(String username, String password, String message) throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance(username, password);
        try {
            twitter.updateStatus(message);
        } catch (twitter4j.TwitterException e) {
            throw new TwitterException();
        }
    }

    @Override
    public void sendMessage(String username, String password, String target, String message) throws TwitterException {
        Twitter twitter = new TwitterFactory().getInstance(username, password);
        try {
            twitter.sendDirectMessage(target, message);
        } catch (twitter4j.TwitterException e) {
            throw new TwitterException();
        }
    }

    public static void zipAttachments(Attachment[] attachments, String filePath) throws IOException {
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

    public static String getTinyUrl(String fullUrl) throws HttpException, IOException {
        HttpClient httpclient = new HttpClient();
        HttpMethod method = new GetMethod("http://tinyurl.com/api-create.php");
        method.setQueryString(new NameValuePair[] { new NameValuePair("url", fullUrl) });
        httpclient.executeMethod(method);
        String tinyUrl = method.getResponseBodyAsString();
        method.releaseConnection();
        return tinyUrl;
    }
}
