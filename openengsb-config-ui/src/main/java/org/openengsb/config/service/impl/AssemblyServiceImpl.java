/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.config.jbi.ServiceUnitInfo;
import org.openengsb.config.service.AssemblyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyServiceImpl implements AssemblyService {
    private static final Logger log = LoggerFactory.getLogger(AssemblyServiceImpl.class);
    private ArrayList<ServiceUnitInfo> serviceUnits = new ArrayList<ServiceUnitInfo>();
    private String deployPath;

    @Override
    public void createNewAssembly() {
        serviceUnits = new ArrayList<ServiceUnitInfo>();
    }

    @Override
    public List<ServiceUnitInfo> getServiceUnits() {
        return serviceUnits;
    }

    @Override
    public boolean deploy(File saFile, String filename) {
        if (deployPath != null) {
            File to = new File(deployPath, filename);
            if (!saFile.renameTo(to))
                return copyFile(saFile, to);
            return true;
        }
        return false;
    }

    private boolean copyFile(File from, File to) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);
            byte[] buffer = new byte[2048];
            while (true) {
                int len = in.read(buffer);
                if (len != -1) {
                    out.write(buffer, 0, len);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("deploying service assembly failed", e);
            return false;
        } finally {
            try { if (in != null) {
                in.close();
            } } catch (IOException e) { }
            try { if (out != null) {
                out.close();
            } } catch (IOException e) { }
        }
        return to.exists();
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }
}
