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

package org.openengsb.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;

public class FTPFileUpload implements FileUpload {
    private Log log = LogFactory.getLog(getClass());
    
    private String hostname;
    private String username;
    private String password;

    @Override
    public URL uploadFile(byte[] file, String extension) {
        URL url = null;
        FTPClient client = new FTPClient();
        
        try {
            client.connect(hostname);
            client.login(username, password);
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            
            String name = new Date().getTime() + "." + extension;
            client.storeFile(name, new ByteArrayInputStream(file));
            url = new URL("ftp", hostname, "/" + name);
            
            client.logout();
            client.disconnect();
            log.info("Successfully uploaded " + extension + "-file to " + hostname + ".");
        } catch (SocketException e) {
            log.error("Error while communicating with FTP-server. SocketException: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error while communicating with FTP-server. IOException: " + e.getMessage());
        }

        return url;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
