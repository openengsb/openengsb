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
package org.openengsb.twitter.common.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.drools.model.Attachment;
import org.openengsb.twitter.common.util.ZipUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class ZipUtilUseTest {
    @Resource
    private ZipUtil zipUtil;
    
    @Test
    public void testZipAttachments() throws IOException {
        String[] files = new String[] { "testfile1.jpg", "testfile2.jpg", "testfile3.jpg" };
        Attachment[] attachments = new Attachment[files.length];
        
        for (int i = 0; i < files.length; i++) {
            File src = new File("target\\test-classes\\" + files[i]);
            FileInputStream fileInputStream = new FileInputStream(src);
            byte[] data = new byte[(int) src.length()];
            fileInputStream.read(data);
            fileInputStream.close();
            attachments[i] = new Attachment(data, "image", files[i]);
        }

        byte[] zip = zipUtil.zipAttachments(attachments);

        assertNotNull(zip);
        assertTrue(zip.length > 0);
    }
}
