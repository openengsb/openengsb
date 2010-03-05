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
package org.openengsb.twitter.common.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openengsb.drools.model.Attachment;

public class ZipUtil {
    public static void zipAttachments(Attachment[] attachments, String filePath) throws IOException {
        FileOutputStream dest = new FileOutputStream(filePath);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        // Highest compression level
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
}
