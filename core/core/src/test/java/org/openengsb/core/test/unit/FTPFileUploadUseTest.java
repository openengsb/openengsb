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
package org.openengsb.core.test.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.util.FileUpload;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class FTPFileUploadUseTest {
    @Resource
    private String hostname;
    @Resource
    private String username;
    @Resource
    private String password;
    @Resource
    private FileUpload fileUpload;

    @Test
    public void testUploadFile() throws MalformedURLException, IOException {
        File src = new File("target\\test-classes\\text.txt");
        FileInputStream fileInputStream = new FileInputStream(src);
        byte[] data = new byte[(int) src.length()];
        fileInputStream.read(data);
        fileInputStream.close();
        
        URL url = fileUpload.uploadFile(data, "txt");
        assertTrue(url.getProtocol().equals("ftp"));
        assertTrue(url.getHost().equals(hostname));
        assertTrue(url.getFile().endsWith("txt"));
        
        URLConnection con = new URL("ftp://" + username + ":" + (password.equals("") ? ":" : password) + "@" + url.getHost() + url.getFile()).openConnection();
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        byte[] data2 = new byte[(int) src.length()];
        in.read(data2);
        in.close();
        
        assertEquals(Arrays.hashCode(data), Arrays.hashCode(data2));
    }
}
